/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.core.persistent.adapter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.copperengine.core.batcher.AbstractBatchCommand;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.BatchExecutor;
import org.copperengine.core.batcher.CommandCallback;
import org.copperengine.core.batcher.NullCallback;
import org.copperengine.core.persistent.DefaultEntityPersister;
import org.copperengine.core.persistent.DefaultEntityPersisterFactory;
import org.copperengine.core.persistent.DefaultPersistenceWorker;
import org.copperengine.core.persistent.DefaultPersisterSharedRessources;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.persistent.Serializer;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AdapterCallPersisterFactory implements DefaultEntityPersisterFactory<AdapterCall, DefaultEntityPersister<AdapterCall>> {

    final Serializer serializer;

    public AdapterCallPersisterFactory(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public Class<AdapterCall> getEntityClass() {
        return AdapterCall.class;
    }

    @Override
    public Class<?> getPersisterClass() {
        return DefaultEntityPersister.class;
    }

    @Override
    public DefaultEntityPersister<AdapterCall> createPersister(PersistentWorkflow<?> workflow,
            DefaultPersisterSharedRessources<AdapterCall, DefaultEntityPersister<AdapterCall>> sharedRessources) {
        return new DefaultEntityPersister<AdapterCall>(workflow,
                null, ((SharedRessources<AdapterCall, DefaultEntityPersister<AdapterCall>>) sharedRessources).insertWorker, null, null);
    }

    static class EntityIdentity implements Comparable<EntityIdentity> {
        DefaultPersistenceWorker.WorkflowAndEntity<AdapterCall> entry;
        String workflowId;
        String entityId;

        public EntityIdentity(DefaultPersistenceWorker.WorkflowAndEntity<AdapterCall> entry) {
            this.entry = entry;
            this.workflowId = entry.workflow.getId();
            this.entityId = entry.entity.getEntityId();
        }

        public EntityIdentity(String workflowId, String entityId) {
            this.workflowId = workflowId;
            this.entityId = entityId;
        }

        @Override
        public int compareTo(EntityIdentity o) {
            return compare(this, o);
        }

        static int compare(
                EntityIdentity o1,
                EntityIdentity o2) {
            int cmp = o1.workflowId.compareTo(o2.workflowId);
            if (cmp != 0)
                return cmp;
            String entityId1 = o1.entityId;
            String entityId2 = o2.entityId;
            if (entityId1 == null) {
                if (entityId2 != null)
                    return -1;
                return 1;
            }
            return entityId1.compareTo(entityId2);
        }

        public int findLeftmostOccurrence(EntityIdentity[] sortedEntities) {
            int idx = Arrays.binarySearch(sortedEntities, this);
            if (idx < 0)
                return idx;
            while (idx > 0 && compare(sortedEntities[idx - 1], this) == 0) {
                --idx;
            }
            return idx;
        }

    }

    public abstract Selector createSelector();

    static final class Selector {

        private static final Logger logger = LoggerFactory.getLogger(Selector.class);

        final Serializer serializer;
        final boolean oracle;

        public Selector(Serializer serializer, boolean oracle) {
            this.oracle = oracle;
            this.serializer = serializer;
        }

        public List<AdapterCall> dequeue(Connection connection, Collection<String> adapterIds, int maxRows) throws SQLException {

            StringBuilder asterisks = new StringBuilder();
            for (int i = 0; i < adapterIds.size(); ++i) {
                asterisks.append("?,");
            }
            asterisks.setLength(asterisks.length() - 1);
            String sql =
                    "SELECT \"WORKFLOWID\", \"ENTITYID\", \"ADAPTERID\", \"PRIORITY\", \"METHODDECLARINGCLASS\", \"METHODNAME\", \"METHODSIGNATURE\", \"ARGS\" " +
                            "FROM \"COP_ADAPTERCALL\"  " +
                            "WHERE DEQUEUE_TS is null AND ADAPTERID in (" + asterisks + ") AND DEFUNCT <> '1' " +
                            "ORDER BY PRIORITY";
            if (oracle) {
                sql = "SELECT * FROM (" + sql + ") WHERE ROWNUM <= " + maxRows;
            }

            final String sqlUpdate =
                    "UPDATE \"COP_ADAPTERCALL\"  " +
                            "SET DEQUEUE_TS = ? " +
                            "WHERE \"WORKFLOWID\" = ? and \"ENTITYID\" = ? and \"ADAPTERID\" = ?";
            final String sqlUpdateDefunct =
                    "UPDATE \"COP_ADAPTERCALL\"  " +
                            "SET DEFUNCT = '1' " +
                            "WHERE \"WORKFLOWID\" = ? and \"ENTITYID\" = ? and \"ADAPTERID\" = ?";

            PreparedStatement stmt = null;
            PreparedStatement stmtUpdate = null;
            PreparedStatement stmtUpdateDefunct = null;
            ArrayList<AdapterCall> ret = new ArrayList<AdapterCall>(Math.min(maxRows, 100));
            try {
                stmt = connection.prepareStatement(sql.toString());
                stmtUpdate = connection.prepareStatement(sqlUpdate.toString());
                stmtUpdateDefunct = connection.prepareStatement(sqlUpdateDefunct.toString());
                Iterator<String> it = adapterIds.iterator();
                int i = 0;
                while (it.hasNext()) {
                    stmt.setString(++i, it.next());
                }
                ResultSet rs = stmt.executeQuery();
                while (rs.next() && ret.size() < maxRows) {
                    String workflowId = rs.getString(1);
                    String entityId = rs.getString(2);
                    String adapterId = rs.getString(3);
                    long priority = rs.getLong(4);
                    String methodDeclaringClass = rs.getString(5);
                    String methodName = rs.getString(6);
                    String methodSignature = rs.getString(7);
                    Method m = resolveMethod(methodDeclaringClass, methodName, methodSignature);
                    Object[] args;
                    try {
                        args = (Object[]) serializer.deserializeObject(rs.getString(8));
                        AdapterCall entity = new AdapterCall(adapterId, entityId, m, args);
                        entity.setWorkflowData(workflowId, priority);
                        stmtUpdate.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                        stmtUpdate.setString(2, workflowId);
                        stmtUpdate.setString(3, entityId);
                        stmtUpdate.setString(4, adapterId);
                        stmtUpdate.addBatch();
                        ret.add(entity);
                    } catch (Exception e) {
                        logger.error("Cannot dequeue adapterCall, adapterId/workflowId/entityId='" + adapterId + "'/'" + workflowId + "'/'" + entityId, e);
                        stmtUpdateDefunct.setString(1, workflowId);
                        stmtUpdateDefunct.setString(2, entityId);
                        stmtUpdateDefunct.setString(3, adapterId);
                        stmtUpdateDefunct.addBatch();
                    }
                }
                rs.close();
                stmtUpdate.executeBatch();
                stmtUpdateDefunct.executeBatch();
                return ret;
            } finally {
                if (stmt != null)
                    stmt.close();
                if (stmtUpdate != null)
                    stmtUpdate.close();
                if (stmtUpdateDefunct != null)
                    stmtUpdateDefunct.close();
            }
        }
    }

    static final class InsertionWorker extends DefaultPersistenceWorker<AdapterCall, DefaultEntityPersister<AdapterCall>> {

        final Serializer serializer;

        public InsertionWorker(Serializer serializer) {
            super(OperationType.INSERT);
            this.serializer = serializer;
        }

        static final String sql =
                "INSERT INTO \"COP_ADAPTERCALL\"" +
                        "	(\"WORKFLOWID\", \"ENTITYID\", \"ADAPTERID\", \"PRIORITY\", \"METHODDECLARINGCLASS\", \"METHODNAME\", \"METHODSIGNATURE\", \"ARGS\")" +
                        "	VALUES(?,?,?,?,?,?,?,?)";

        @Override
        public void doExec(Connection connection, List<WorkflowAndEntity<AdapterCall>> theWork) throws SQLException {

            if (theWork.isEmpty())
                return;

            final PreparedStatement stmt = connection.prepareStatement(sql);
            try {
                for (WorkflowAndEntity<AdapterCall> en : theWork) {
                    AdapterCall entity = en.entity;
                    stmt.setString(1, en.workflow.getId());
                    stmt.setString(2, entity.getEntityId());

                    stmt.setString(1, en.workflow.getId());
                    stmt.setString(2, entity.getEntityId());
                    stmt.setString(3, entity.getAdapterId());
                    stmt.setLong(4, entity.getPriority());
                    stmt.setString(5, entity.getMethod().getDeclaringClass().getName());
                    stmt.setString(6, entity.getMethod().getName());
                    stmt.setString(7, Type.getMethodDescriptor(entity.getMethod()));
                    try {
                        stmt.setString(8, serializer.serializeObject(entity.getArgs()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } finally {
                stmt.close();
            }
        }
    }

    static final class Deletor {

        static final String sql =
                "DELETE FROM \"COP_ADAPTERCALL\"" +
                        "	WHERE \"WORKFLOWID\"=? AND \"ENTITYID\" = ? AND \"ADAPTERID\"=?";

        public void doExec(Connection connection, List<AdapterCall> theWork) throws SQLException {

            if (theWork.isEmpty())
                return;

            final PreparedStatement stmt = connection.prepareStatement(sql);
            try {
                for (AdapterCall en : theWork) {
                    stmt.setString(1, en.getWorkflowId());
                    stmt.setString(2, en.getEntityId());
                    stmt.setString(3, en.getAdapterId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } finally {
                stmt.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Class<?>> getEntityClassesDependingOn() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public DefaultPersisterSharedRessources<AdapterCall, DefaultEntityPersister<AdapterCall>> createSharedRessources() {
        return new SharedRessources<AdapterCall, DefaultEntityPersister<AdapterCall>>(
                new InsertionWorker(serializer));
    }

    public AbstractBatchCommand<?, ?> createDeleteCommand(AdapterCall c) {
        return new Command(c, new NullCallback<AdapterCallPersisterFactory.Command>());
    }

    static final class Command extends AbstractBatchCommand<Executor, Command> {

        final AdapterCall data;

        public Command(AdapterCall data, CommandCallback<Command> callback) {
            super(callback, System.currentTimeMillis());
            if (data == null)
                throw new NullPointerException();
            this.data = data;
        }

        @Override
        public Executor executor() {
            return Executor.INSTANCE;
        }

    }

    static final class Executor extends BatchExecutor<Executor, Command> {
        static Executor INSTANCE = new Executor();

        @Override
        public int preferredBatchSize() {
            return 50;
        }

        @Override
        public int maximumBatchSize() {
            return 100;
        }

        @Override
        public void doExec(
                Collection<BatchCommand<Executor, Command>> commands,
                Connection connection) throws Exception {
            Deletor deletor = new Deletor();
            ArrayList<AdapterCall> calls = new ArrayList<AdapterCall>();
            for (BatchCommand<Executor, Command> cmd : commands) {
                calls.add(((Command) cmd).data);
            }
            deletor.doExec(connection, calls);
        }
    }

    public static class Oracle extends AdapterCallPersisterFactory {

        public Oracle(Serializer serializer) {
            super(serializer);
        }

        @Override
        public Selector createSelector() {
            return new Selector(serializer, true);
        }

    }

    public static class Common extends AdapterCallPersisterFactory {

        public Common(Serializer serializer) {
            super(serializer);
        }

        @Override
        public Selector createSelector() {
            return new Selector(serializer, false);
        }

    }

    public class SharedRessources<E, P extends DefaultEntityPersister<E>> extends
            DefaultPersisterSharedRessources<E, P> {

        final DefaultPersistenceWorker<E, P> insertWorker;

        public SharedRessources(
                DefaultPersistenceWorker<E, P> insertWorker) {
            this.insertWorker = insertWorker;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Iterable<DefaultPersistenceWorker<E, P>> getWorkers() {
            return Arrays.<DefaultPersistenceWorker<E, P>>asList(new DefaultPersistenceWorker[] {
                    insertWorker
            });
        }

    }

    static HashMap<String, HashMap<String, HashMap<String, Method>>> methodCache = new HashMap<String, HashMap<String, HashMap<String, Method>>>();

    static Method resolveMethod(String declaringClass, String methodName, String signature) throws NoSuchMethodError {
        synchronized (methodCache) {
            Method mr = null;
            try {
                HashMap<String, HashMap<String, Method>> subCache1 = methodCache.get(declaringClass);
                if (subCache1 == null) {
                    subCache1 = new HashMap<String, HashMap<String, Method>>();
                    methodCache.put(declaringClass, subCache1);
                    Class<?> c = Class.forName(declaringClass);
                    for (Method m : c.getDeclaredMethods()) {
                        if (Modifier.isStatic(m.getModifiers()))
                            continue;
                        HashMap<String, Method> subCache2 = subCache1.get(m.getName());
                        if (subCache2 == null) {
                            subCache2 = new HashMap<String, Method>();
                            subCache1.put(m.getName(), subCache2);
                        }
                        subCache2.put(Type.getMethodDescriptor(m), m);
                    }
                }
                HashMap<String, Method> subCache2 = subCache1.get(methodName);
                mr = subCache2 == null ? null : subCache2.get(signature);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (mr == null)
                throw new NoSuchMethodError("Method '" + methodName + "'  with signature '" + signature + "' of class '" + declaringClass + " not found ");
            return mr;
        }
    }

}
