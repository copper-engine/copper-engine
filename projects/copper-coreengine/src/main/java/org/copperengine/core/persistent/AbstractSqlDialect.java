/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.core.persistent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.ProcessingState;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.db.utility.JdbcUtils;
import org.copperengine.core.internal.WorkflowAccessor;
import org.copperengine.core.monitoring.NullRuntimeStatisticsCollector;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.monitoring.StmtStatistic;
import org.copperengine.management.DatabaseDialectMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation of the {@link DatabaseDialect} for SQL databases
 * 
 * @author austermann
 */
public abstract class AbstractSqlDialect implements DatabaseDialect, DatabaseDialectMXBean {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSqlDialect.class);

    private WorkflowRepository wfRepository;
    private RuntimeStatisticsCollector runtimeStatisticsCollector = new NullRuntimeStatisticsCollector();
    private boolean removeWhenFinished = true;
    /**
     * if multiple engines could be running together, you MUST turn it on
     */
    protected boolean multiEngineMode;
    protected final boolean supportsMultipleEngines;
    protected long defaultStaleResponseRemovalTimeout = 60 * 60 * 1000;
    protected final int ACQUIRE_BLOCKING_WAIT_SEC = 10;
    protected Serializer serializer = new StandardJavaSerializer();
    protected int dbBatchingLatencyMSec = 20;
    private WorkflowPersistencePlugin workflowPersistencePlugin = WorkflowPersistencePlugin.NULL_PLUGIN;
    protected String queryUpdateQueueState = getResourceAsString("/sql-query-ready-bpids.sql");
    private String engineId;

    private StmtStatistic dequeueStmtStatistic;
    private StmtStatistic queueDeleteStmtStatistic;
    private StmtStatistic enqueueUpdateStateStmtStatistic;
    private StmtStatistic insertStmtStatistic;
    private StmtStatistic deleteStaleResponsesStmtStatistic;
    protected StmtStatistic selectQueueSizeStmtStatistic;

    public AbstractSqlDialect() {
        this(false, false);
    }

    public AbstractSqlDialect(final boolean supportsMultipleEngines, final boolean defaultMultiEngineMode) {
        this.supportsMultipleEngines = supportsMultipleEngines;
        setMultiEngineMode(defaultMultiEngineMode);
    }

    @Override
    public void startup() {
        if (multiEngineMode && engineId == null) {
            throw new NullPointerException("EngineId is NULL! Change your " + getClass().getSimpleName() + " configuration.");
        }
        if (engineId == null) {
            engineId = "default";
            logger.info("Setting engineId to {}", engineId);
        }
        initStats();
    }

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    public void setEngineIdProvider(EngineIdProvider engineIdProvider) {
        engineId = engineIdProvider.getEngineId();
    }

    @Override
    public void setDbBatchingLatencyMSec(int dbBatchingLatencyMSec) {
        logger.info("setDbBatchingLatencyMSec({})", dbBatchingLatencyMSec);
        this.dbBatchingLatencyMSec = dbBatchingLatencyMSec;
    }

    @Override
    public int getDbBatchingLatencyMSec() {
        return dbBatchingLatencyMSec;
    }

    /**
     * Sets the default removal timeout for stale responses in the underlying database. A response is stale/timed out
     * when there is no workflow instance waiting for it within the specified amount of time.
     * 
     * @param defaultStaleResponseRemovalTimeout
     *        removal timeout
     */
    @Override
    public void setDefaultStaleResponseRemovalTimeout(long defaultStaleResponseRemovalTimeout) {
        logger.info("setDefaultStaleResponseRemovalTimeout({})", defaultStaleResponseRemovalTimeout);
        this.defaultStaleResponseRemovalTimeout = defaultStaleResponseRemovalTimeout;
    }

    @Override
    public long getDefaultStaleResponseRemovalTimeout() {
        return defaultStaleResponseRemovalTimeout;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public void setRuntimeStatisticsCollector(RuntimeStatisticsCollector runtimeStatisticsCollector) {
        this.runtimeStatisticsCollector = runtimeStatisticsCollector;
    }

    private void initStats() {
        dequeueStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery", runtimeStatisticsCollector);
        queueDeleteStmtStatistic = new StmtStatistic("DBStorage.queue.delete", runtimeStatisticsCollector);
        enqueueUpdateStateStmtStatistic = new StmtStatistic("DBStorage.enqueue.updateState", runtimeStatisticsCollector);
        insertStmtStatistic = new StmtStatistic("DBStorage.insert", runtimeStatisticsCollector);
        deleteStaleResponsesStmtStatistic = new StmtStatistic("DBStorage.deleteStaleResponses", runtimeStatisticsCollector);
        selectQueueSizeStmtStatistic = new StmtStatistic("DBStorage.selectQueueSize", runtimeStatisticsCollector);
    }

    /**
     * returns an int value between 0 and 1073741823 (exclusive)
     */
    protected static int computeLockId(String s) {
        // This method handles the following fact: Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE
        int hashCode = s.hashCode();
        if (hashCode == Integer.MIN_VALUE) {
            hashCode = 13;
        }
        return Math.abs(hashCode) % 1073741823;
    }

    protected String getResourceAsString(String name) {
        return getResourceAsString(getClass(), name);
    }

    protected static String getResourceAsString(Class<?> baseClass, String name) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(baseClass.getResourceAsStream(name)));
            try {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } finally {
                br.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setWfRepository(WorkflowRepository wfRepository) {
        this.wfRepository = wfRepository;
    }

    @Override
    public void setRemoveWhenFinished(boolean removeWhenFinished) {
        logger.info("setRemoveWhenFinished({})", removeWhenFinished);
        this.removeWhenFinished = removeWhenFinished;
    }

    @Override
    public boolean isRemoveWhenFinished() {
        return removeWhenFinished;
    }

    @Override
    public void resumeBrokenBusinessProcesses(Connection con) throws Exception {
        logger.info("resumeBrokenBusinessProcesses");

        logger.info("Reactivating queue entries...");
        final PreparedStatement stmt = con.prepareStatement("UPDATE COP_QUEUE SET engine_id = null WHERE engine_id=?");
        try {
            stmt.setString(1, engineId);
            stmt.execute();
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
        logger.info("done!");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<Workflow<?>> dequeue(String ppoolId, int max, Connection con) throws Exception {
        logger.trace("dequeue({},{})", ppoolId, max);

        PreparedStatement dequeueStmt = null;
        PreparedStatement updateQueueStmt = null;
        PreparedStatement selectResponsesStmt = null;
        PreparedStatement updateBpStmt = null;
        final String lockContext = "dequeue#" + ppoolId;
        try {
            final long startTS = System.currentTimeMillis();
            lock(con, lockContext);
            final List<Workflow<?>> rv = new ArrayList<Workflow<?>>(max);
            final List<BatchCommand> invalidWorkflowInstances = new ArrayList<BatchCommand>();

            dequeueStmt = createDequeueStmt(con, ppoolId, max);
            updateQueueStmt = con.prepareStatement("update COP_QUEUE set ENGINE_ID=? where WORKFLOW_INSTANCE_ID=?");
            dequeueStmtStatistic.start();
            final ResultSet rs = dequeueStmt.executeQuery();
            final Map<String, Workflow<?>> map = new HashMap<String, Workflow<?>>(max * 3);
            while (rs.next()) {
                final String id = rs.getString(1);
                final int prio = rs.getInt(2);

                updateQueueStmt.setString(1, engineId);
                updateQueueStmt.setString(2, id);
                updateQueueStmt.addBatch();

                try {
                    SerializedWorkflow sw = new SerializedWorkflow();
                    sw.setData(rs.getString(3));
                    sw.setObjectState(rs.getString(4));
                    PersistentWorkflow<?> wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
                    wf.setId(id);
                    wf.setProcessorPoolId(ppoolId);
                    wf.setPriority(prio);
                    WorkflowAccessor.setCreationTS(wf, new Date(rs.getTimestamp(5).getTime()));
                    map.put(wf.getId(), wf);
                } catch (Exception e) {
                    logger.error("decoding of '" + id + "' failed: " + e.toString(), e);
                    invalidWorkflowInstances.add(createBatchCommand4error(new DummyPersistentWorkflow(id, ppoolId, null, prio), e, DBProcessingState.INVALID, new Acknowledge.BestEffortAcknowledge()));
                }
            }
            rs.close();
            dequeueStmt.close();
            dequeueStmtStatistic.stop(map.size());

            if (!map.isEmpty()) {
                selectResponsesStmt = con.prepareStatement("select w.WORKFLOW_INSTANCE_ID, w.correlation_id, w.timeout_ts, r.response from (select WORKFLOW_INSTANCE_ID, correlation_id, timeout_ts from COP_WAIT where WORKFLOW_INSTANCE_ID in (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)) w LEFT OUTER JOIN COP_RESPONSE r ON w.correlation_id = r.correlation_id order by r.correlation_id, r.response_id");
                List<List<String>> ids = splitt(map.keySet(), 25);
                for (List<String> id : ids) {
                    selectResponsesStmt.clearParameters();
                    for (int i = 0; i < 25; i++) {
                        selectResponsesStmt.setString(i + 1, id.size() >= i + 1 ? id.get(i) : null);
                    }
                    ResultSet rsResponses = selectResponsesStmt.executeQuery();
                    while (rsResponses.next()) {
                        String bpId = rsResponses.getString(1);
                        String cid = rsResponses.getString(2);
                        final Timestamp timeoutTS = rsResponses.getTimestamp(3);
                        boolean isTimeout = timeoutTS != null ? timeoutTS.getTime() <= System.currentTimeMillis() : false;
                        String response = rsResponses.getString(4);
                        PersistentWorkflow<?> wf = (PersistentWorkflow<?>) map.get(bpId);
                        Response<?> r = null;
                        if (response != null) {
                            r = serializer.deserializeResponse(response);
                            wf.addResponseId(r.getResponseId());
                        } else if (isTimeout) {
                            // timeout
                            r = new Response<Object>(cid);
                        }
                        if (r != null) {
                            wf.putResponse(r);
                        }
                        wf.addWaitCorrelationId(cid);
                    }
                    rsResponses.close();
                }

                queueDeleteStmtStatistic.start();
                updateQueueStmt.executeBatch();
                queueDeleteStmtStatistic.stop(map.size());

                @SuppressWarnings("unchecked")
                Collection<PersistentWorkflow<?>> workflows = (Collection) map.values();
                workflowPersistencePlugin.onWorkflowsLoaded(con, workflows);
                rv.addAll(workflows);
            }

            handleInvalidWorkflowInstances(con, invalidWorkflowInstances);

            logger.trace("dequeue for pool {} returns {} element(s)", ppoolId, rv.size());
            logger.debug("{} in {} msec", rv.size(), (System.currentTimeMillis() - startTS));
            return rv;
        } finally {
            JdbcUtils.closeStatement(updateBpStmt);
            JdbcUtils.closeStatement(dequeueStmt);
            JdbcUtils.closeStatement(updateQueueStmt);
            JdbcUtils.closeStatement(selectResponsesStmt);
            releaseLock(con, lockContext);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void handleInvalidWorkflowInstances(Connection con, final List<BatchCommand> invalidWorkflowInstances) throws Exception {
        logger.debug("invalidWorkflowInstances.size()={}", invalidWorkflowInstances.size());
        if (invalidWorkflowInstances.isEmpty()) {
            return;
        }
        invalidWorkflowInstances.get(0).executor().doExec(invalidWorkflowInstances, con);
    }

    @Override
    public int updateQueueState(int max, Connection con) throws SQLException {
        PreparedStatement queryStmt = null;
        PreparedStatement updStmt = null;
        PreparedStatement insStmt = null;
        final String lockContext = "updateQueueState";

        try {
            int rowcount = 0;
            final long startTS = System.currentTimeMillis();
            lock(con, lockContext);

            final Timestamp NOW = new Timestamp(System.currentTimeMillis());
            enqueueUpdateStateStmtStatistic.start();
            queryStmt = createUpdateStateStmt(con, max);
            ResultSet rs = queryStmt.executeQuery();
            updStmt = con.prepareStatement("update COP_WAIT set state=1, timeout_ts=timeout_ts where WORKFLOW_INSTANCE_ID=?");
            insStmt = con.prepareStatement("INSERT INTO COP_QUEUE (PPOOL_ID, PRIORITY, LAST_MOD_TS, WORKFLOW_INSTANCE_ID) VALUES (?,?,?,?)");
            while (rs.next()) {
                rowcount++;

                final String wfiId = rs.getString(1);
                final String ppoolId = rs.getString(2);
                final int prio = rs.getInt(3);

                updStmt.setString(1, wfiId);
                updStmt.addBatch();

                insStmt.setString(1, ppoolId);
                insStmt.setInt(2, prio);
                insStmt.setTimestamp(3, NOW);
                insStmt.setString(4, wfiId);
                insStmt.addBatch();

                logger.debug("Inserting {} into COP_QUEUE", wfiId);
            }
            rs.close();
            if (rowcount > 0) {
                insStmt.executeBatch();
                updStmt.executeBatch();
            }
            enqueueUpdateStateStmtStatistic.stop(rowcount == 0 ? 1 : rowcount);
            logger.debug("Queue update in {} msec", (System.currentTimeMillis() - startTS));
            return rowcount;
        } catch (SQLException e) {
            ResultSet rs = con.createStatement().executeQuery("SELECT WORKFLOW_INSTANCE_ID FROM COP_QUEUE");
            while (rs.next()) {
                logger.info("WORKFLOW_INSTANCE_ID={}", rs.getString(1));
            }
            throw e;
        } finally {
            JdbcUtils.closeStatement(insStmt);
            JdbcUtils.closeStatement(updStmt);
            JdbcUtils.closeStatement(queryStmt);
            releaseLock(con, lockContext);
        }
    }

    @Override
    public int deleteStaleResponse(Connection con, int maxRows) throws Exception {
        if (logger.isTraceEnabled())
            logger.trace("deleteStaleResponse()");

        final PreparedStatement stmt = createDeleteStaleResponsesStmt(con, maxRows);
        final String lockContext = "deleteStaleResponse";
        try {
            lock(con, lockContext);
            deleteStaleResponsesStmtStatistic.start();
            final int rowCount = stmt.executeUpdate();
            deleteStaleResponsesStmtStatistic.stop(rowCount);
            logger.trace("deleted {} stale response(s).", rowCount);
            return rowCount;
        } finally {
            JdbcUtils.closeStatement(stmt);
            releaseLock(con, lockContext);
        }
    }

    protected void lock(Connection con, String lockContext) throws SQLException {
        if (!multiEngineMode)
            return;
        doLock(con, lockContext);
    }

    protected void releaseLock(Connection con, String lockContext) {
        if (!multiEngineMode)
            return;
        doReleaseLock(con, lockContext);
    }

    /**
     * Locks on the lockContext, implement this to provide multiple engine support
     * It will block wait until the lock was successfully hold, must be used together with {@link #releaseLock}
     * 
     * @param con
     * @param lockContext
     * @throws SQLException
     */
    protected void doLock(Connection con, String lockContext) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * Releases the lock on the lockContext, use together with {@link #lock(Connection, String)}
     * 
     * @param con
     * @param lockContext
     */
    protected void doReleaseLock(Connection con, String lockContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restart(String workflowInstanceId, Connection c) throws Exception {
        PreparedStatement stmtQueue = null;
        PreparedStatement stmtInstance = null;
        try {
            final Timestamp NOW = new Timestamp(System.currentTimeMillis());
            stmtQueue = c.prepareStatement("INSERT INTO COP_QUEUE (PPOOL_ID, PRIORITY, LAST_MOD_TS, WORKFLOW_INSTANCE_ID) (SELECT PPOOL_ID, PRIORITY, ?, ID FROM COP_WORKFLOW_INSTANCE WHERE ID=? AND (STATE=? OR STATE=?))");
            stmtQueue.setTimestamp(1, NOW);
            stmtQueue.setString(2, workflowInstanceId);
            stmtQueue.setInt(3, DBProcessingState.ERROR.ordinal());
            stmtQueue.setInt(4, DBProcessingState.INVALID.ordinal());
            final int rowCount = stmtQueue.executeUpdate();
            if (rowCount > 0) {
                stmtInstance = c.prepareStatement("UPDATE COP_WORKFLOW_INSTANCE SET STATE=?, LAST_MOD_TS=? WHERE ID=? AND (STATE=? OR STATE=?)");
                stmtInstance.setInt(1, DBProcessingState.ENQUEUED.ordinal());
                stmtInstance.setTimestamp(2, NOW);
                stmtInstance.setString(3, workflowInstanceId);
                stmtInstance.setInt(4, DBProcessingState.ERROR.ordinal());
                stmtInstance.setInt(5, DBProcessingState.INVALID.ordinal());
                stmtInstance.execute();
            }
        } finally {
            JdbcUtils.closeStatement(stmtInstance);
            JdbcUtils.closeStatement(stmtQueue);
        }
    }

    @Override
    public void restartAll(Connection c) throws Exception {
        PreparedStatement insertStmt = null;
        PreparedStatement stmtInstance = null;
        try {
            insertStmt = c.prepareStatement("insert into COP_QUEUE (ppool_id, priority, last_mod_ts, WORKFLOW_INSTANCE_ID) (select ppool_id, priority, last_mod_ts, id from COP_WORKFLOW_INSTANCE where state=? or state=?)");
            insertStmt.setInt(1, DBProcessingState.ERROR.ordinal());
            insertStmt.setInt(2, DBProcessingState.INVALID.ordinal());
            logger.info("Adding all BPs in state INVALID & ERROR to queue...");
            int rowCount = insertStmt.executeUpdate();
            if (rowCount > 0) {
                final Timestamp NOW = new Timestamp(System.currentTimeMillis());
                stmtInstance = c.prepareStatement("UPDATE COP_WORKFLOW_INSTANCE SET STATE=?, LAST_MOD_TS=? WHERE STATE=? OR STATE=?");
                stmtInstance.setInt(1, DBProcessingState.ENQUEUED.ordinal());
                stmtInstance.setTimestamp(2, NOW);
                stmtInstance.setInt(3, DBProcessingState.ERROR.ordinal());
                stmtInstance.setInt(4, DBProcessingState.INVALID.ordinal());
                stmtInstance.execute();
            }
            logger.info("done - restartAll invalid: " + rowCount + " BP(s).");
        } finally {
            JdbcUtils.closeStatement(stmtInstance);
            JdbcUtils.closeStatement(insertStmt);
        }
    }

    @Override
    public void notify(List<Response<?>> responses, Connection c) throws Exception {
        final int MAX = 50;
        final List<Response<?>> subsetWithERH = new ArrayList<Response<?>>(MAX);
        final List<Response<?>> subsetWithoutERH = new ArrayList<Response<?>>(MAX);
        for (int i = 0; i < responses.size(); i++) {
            Response<?> r = responses.get(i);
            if (r.isEarlyResponseHandling()) {
                subsetWithERH.add(r);
            } else {
                subsetWithoutERH.add(r);
            }
            if (subsetWithERH.size() == MAX) {
                insertResponses(subsetWithERH, c);
                subsetWithERH.clear();
            }
            if (subsetWithoutERH.size() == MAX) {
                insertResponses(subsetWithoutERH, c);
                subsetWithoutERH.clear();
            }
        }
        insertResponses(subsetWithERH, c);
        subsetWithERH.clear();

        insertResponses(subsetWithoutERH, c);
        subsetWithoutERH.clear();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void insertResponses(List<Response<?>> responses, Connection con) throws Exception {
        if (responses.isEmpty())
            return;
        List<BatchCommand> cmds = new ArrayList<BatchCommand>(responses.size());
        for (Response<?> r : responses) {
            cmds.add(createBatchCommand4Notify(r, new Acknowledge.BestEffortAcknowledge()));
        }
        cmds.get(0).executor().doExec(cmds, con);
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public BatchCommand createBatchCommand4Finish(Workflow<?> w, Acknowledge ack) {
        return new SqlRemove.Command((PersistentWorkflow<?>) w, removeWhenFinished, System.currentTimeMillis() + dbBatchingLatencyMSec, workflowPersistencePlugin, ack);
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public BatchCommand createBatchCommand4Notify(Response<?> response, Acknowledge ack) throws Exception {
        if (response == null)
            throw new NullPointerException();
        if (response.isEarlyResponseHandling())
            return new SqlNotify.Command(response, serializer, defaultStaleResponseRemovalTimeout, System.currentTimeMillis() + dbBatchingLatencyMSec, ack);
        else
            return createBatchCommand4NotifyNoEarlyResponseHandling(response, ack);
    }

    @SuppressWarnings({ "rawtypes" })
    public abstract BatchCommand createBatchCommand4NotifyNoEarlyResponseHandling(Response<?> response, Acknowledge ack) throws Exception;

    @Override
    @SuppressWarnings({ "rawtypes" })
    public BatchCommand createBatchCommand4registerCallback(RegisterCall rc, ScottyDBStorageInterface dbStorageInterface, Acknowledge ack) throws Exception {
        if (rc == null)
            throw new NullPointerException();
        return new SqlRegisterCallback.Command(rc, serializer, dbStorageInterface, System.currentTimeMillis() + dbBatchingLatencyMSec, workflowPersistencePlugin, ack);
    }

    @Override
    public void insert(List<Workflow<?>> wfs, Connection con) throws DuplicateIdException, Exception {
        PreparedStatement stmtWF = null;
        PreparedStatement stmtQueue = null;
        try {
            final Timestamp NOW = new Timestamp(System.currentTimeMillis());
            stmtWF = con.prepareStatement("INSERT INTO COP_WORKFLOW_INSTANCE (ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,DATA,OBJECT_STATE,CREATION_TS,CLASSNAME) VALUES (?,?,?,?,?,?,?,?,?)");
            stmtQueue = con.prepareStatement("insert into COP_QUEUE (ppool_id, priority, last_mod_ts, WORKFLOW_INSTANCE_ID) values (?,?,?,?)");
            int n = 0;
            for (int i = 0; i < wfs.size(); i++) {
                Workflow<?> wf = wfs.get(i);
                logger.debug("insert({})", wf.getId());
                final SerializedWorkflow sw = serializer.serializeWorkflow(wf);
                stmtWF.setString(1, wf.getId());
                stmtWF.setInt(2, DBProcessingState.ENQUEUED.ordinal());
                stmtWF.setInt(3, wf.getPriority());
                stmtWF.setTimestamp(4, NOW);
                stmtWF.setString(5, wf.getProcessorPoolId());
                stmtWF.setString(6, sw.getData());
                stmtWF.setString(7, sw.getObjectState());
                stmtWF.setTimestamp(8, new Timestamp(wf.getCreationTS().getTime()));
                stmtWF.setString(9, wf.getClass().getName());
                stmtWF.addBatch();

                stmtQueue.setString(1, wf.getProcessorPoolId());
                stmtQueue.setInt(2, wf.getPriority());
                stmtQueue.setTimestamp(3, NOW);
                stmtQueue.setString(4, wf.getId());
                stmtQueue.addBatch();

                n++;
                if (i % 100 == 0 || (i + 1) == wfs.size()) {
                    insertStmtStatistic.start();
                    stmtWF.executeBatch();
                    stmtQueue.executeBatch();
                    insertStmtStatistic.stop(n);
                    n = 0;
                }
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<PersistentWorkflow<?>> uncheckedWfs = (List) wfs;
            workflowPersistencePlugin.onWorkflowsSaved(con, uncheckedWfs);
        } finally {
            JdbcUtils.closeStatement(stmtQueue);
            JdbcUtils.closeStatement(stmtWF);
        }
    }

    @Override
    public void insert(Workflow<?> wf, Connection con) throws Exception {
        final List<Workflow<?>> wfs = new ArrayList<Workflow<?>>(1);
        wfs.add(wf);
        insert(wfs, con);
    }

    protected List<List<String>> splitt(Collection<String> keySet, int n) {
        if (keySet.isEmpty())
            return Collections.emptyList();

        List<List<String>> r = new ArrayList<List<String>>(keySet.size() / n + 1);
        List<String> l = new ArrayList<String>(n);
        for (String s : keySet) {
            l.add(s);
            if (l.size() == n) {
                r.add(l);
                l = new ArrayList<String>(n);
            }
        }
        if (l.size() > 0) {
            r.add(l);
        }
        return r;
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public abstract BatchCommand createBatchCommand4error(Workflow<?> w, Throwable t, DBProcessingState dbProcessingState, Acknowledge ack);

    protected abstract PreparedStatement createUpdateStateStmt(final Connection c, final int max) throws SQLException;

    protected abstract PreparedStatement createDequeueStmt(final Connection c, final String ppoolId, final int max) throws SQLException;

    protected abstract PreparedStatement createDeleteStaleResponsesStmt(final Connection c, final int MAX_ROWS) throws SQLException;

    @Override
    public List<String> checkDbConsistency(Connection con) throws Exception {
        if (multiEngineMode) {
            logger.warn("Checking DB consistency when multiEngineMode is turned on!");
        }
        final PreparedStatement dequeueStmt = con.prepareStatement("select id,priority,data,object_state,PPOOL_ID from COP_WORKFLOW_INSTANCE where state not in (?,?)");
        try {
            final List<String> idsOfBadWorkflows = new ArrayList<String>();
            dequeueStmt.setInt(1, DBProcessingState.INVALID.ordinal());
            dequeueStmt.setInt(2, DBProcessingState.FINISHED.ordinal());
            ResultSet rs = dequeueStmt.executeQuery();
            while (rs.next()) {
                final String id = rs.getString(1);
                try {
                    final int prio = rs.getInt(2);
                    final String data = rs.getString(3);
                    final String objectState = rs.getString(4);
                    final String ppoolId = rs.getString(5);
                    final SerializedWorkflow sw = new SerializedWorkflow();
                    sw.setData(data);
                    sw.setObjectState(objectState);
                    final PersistentWorkflow<?> wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
                    wf.setId(id);
                    wf.setProcessorPoolId(ppoolId);
                    wf.setPriority(prio);
                    logger.debug("Successful test deserialization of workflow {}", id);
                } catch (Exception e) {
                    logger.warn("Test deserialization of workflow " + id + " failed: " + e.toString());
                    idsOfBadWorkflows.add(id);
                }
            }
            rs.close();

            return idsOfBadWorkflows;
        } finally {
            JdbcUtils.closeStatement(dequeueStmt);
        }
    }

    @Override
    public void shutdown() {
    }

    public WorkflowPersistencePlugin getWorkflowPersistencePlugin() {
        return workflowPersistencePlugin;
    }

    public void setWorkflowPersistencePlugin(
            WorkflowPersistencePlugin workflowPersistencePlugin) {
        this.workflowPersistencePlugin = workflowPersistencePlugin;
    }

    @Override
    public Workflow<?> read(String workflowInstanceId, Connection con) throws Exception {
        logger.trace("read({})", workflowInstanceId);

        PreparedStatement readStmt = null;
        PreparedStatement selectResponsesStmt = null;
        try {
            readStmt = createReadStmt(con, workflowInstanceId);

            final ResultSet rs = readStmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                return null;
            }
            PersistentWorkflow<?> wf;
            final String id = rs.getString(1);
            final int prio = rs.getInt(2);

            SerializedWorkflow sw = new SerializedWorkflow();
            sw.setData(rs.getString(3));
            sw.setObjectState(rs.getString(4));
            wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
            wf.setId(id);
            wf.setPriority(prio);
            wf.setProcessorPoolId(rs.getString(6));
            WorkflowAccessor.setCreationTS(wf, new Date(rs.getTimestamp(5).getTime()));
            DBProcessingState dbProcessingState = DBProcessingState.getByOrdinal(rs.getInt(7));
            ProcessingState state = DBProcessingState.getProcessingStateByState(dbProcessingState);
            WorkflowAccessor.setProcessingState(wf, state);
            rs.close();
            readStmt.close();

            selectResponsesStmt = con.prepareStatement("select w.WORKFLOW_INSTANCE_ID, w.correlation_id, w.timeout_ts, r.response from (select WORKFLOW_INSTANCE_ID, correlation_id, timeout_ts from COP_WAIT where WORKFLOW_INSTANCE_ID = ?) w LEFT OUTER JOIN COP_RESPONSE r ON w.correlation_id = r.correlation_id");
            selectResponsesStmt.setString(1, workflowInstanceId);
            ResultSet rsResponses = selectResponsesStmt.executeQuery();
            while (rsResponses.next()) {
                String cid = rsResponses.getString(2);
                final Timestamp timeoutTS = rsResponses.getTimestamp(3);
                boolean isTimeout = timeoutTS != null ? timeoutTS.getTime() <= System.currentTimeMillis() : false;
                String response = rsResponses.getString(4);
                Response<?> r = null;
                if (response != null) {
                    r = serializer.deserializeResponse(response);
                    wf.addResponseId(r.getResponseId());
                } else if (isTimeout) {
                    // timeout
                    r = new Response<Object>(cid);
                }
                if (r != null) {
                    wf.putResponse(r);
                }
                wf.addWaitCorrelationId(cid);
            }
            workflowPersistencePlugin.onWorkflowsLoaded(con, Arrays.<PersistentWorkflow<?>>asList(wf));

            return wf;
        } finally {
            JdbcUtils.closeStatement(readStmt);
            JdbcUtils.closeStatement(selectResponsesStmt);
        }
    }

    protected PreparedStatement createReadStmt(final Connection c, final String workflowId) throws SQLException {
        PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,object_state,creation_ts,PPOOL_ID,state from COP_WORKFLOW_INSTANCE where id = ?");
        dequeueStmt.setString(1, workflowId);
        return dequeueStmt;
    }

    protected abstract PreparedStatement createQueryAllActiveStmt(final Connection c, final String className, final int max) throws SQLException;

    @Override
    public List<Workflow<?>> queryAllActive(final String className, final Connection c, final int max) throws SQLException {
        final PreparedStatement queryStmt = createQueryAllActiveStmt(c, className, max);
        try {
            final ResultSet rs = queryStmt.executeQuery();
            final List<Workflow<?>> result = new ArrayList<Workflow<?>>();
            while (rs.next()) {
                final String id = rs.getString(1);
                final int prio = rs.getInt(3);
                final String ppoolId = rs.getString(4);
                try {
                    SerializedWorkflow sw = new SerializedWorkflow();
                    sw.setData(rs.getString(5));
                    sw.setObjectState(rs.getString(6));
                    PersistentWorkflow<?> wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
                    wf.setId(id);
                    wf.setProcessorPoolId(ppoolId);
                    wf.setPriority(prio);
                    DBProcessingState dbProcessingState = DBProcessingState.getByOrdinal(rs.getInt(2));
                    ProcessingState state = DBProcessingState.getProcessingStateByState(dbProcessingState);
                    WorkflowAccessor.setProcessingState(wf, state);
                    WorkflowAccessor.setCreationTS(wf, new Date(rs.getTimestamp(7).getTime()));
                    result.add(wf);
                } catch (Exception e) {
                    logger.error("decoding of '" + id + "' failed: " + e.toString(), e);
                }
            }
            return result;
        } finally {
            JdbcUtils.closeStatement(queryStmt);
        }
    }

    public void setMultiEngineMode(boolean multiEngineMode) {
        if (!supportsMultipleEngines && multiEngineMode) {
            throw new IllegalArgumentException("MultiEngineMode not supported!");
        }
        this.multiEngineMode = multiEngineMode;
    }
    
    @Override
    public Date readDatabaseClock(Connection con) throws SQLException {
        return null;
    }
    
    @Override
    public int queryQueueSize(String processorPoolId, int max, Connection con) throws SQLException {
        int queueSize;
        selectQueueSizeStmtStatistic.start();
        try (PreparedStatement pstmt = con.prepareStatement("SELECT count(*) FROM COP_QUEUE WHERE PPOOL_ID=?")) {
            pstmt.setString(1, processorPoolId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            queueSize = rs.getInt(1);
        }
        selectQueueSizeStmtStatistic.stop(queueSize);
        return queueSize;
    }    
}
