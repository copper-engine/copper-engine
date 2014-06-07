/*
 * Copyright 2002-2014 SCOOP Software GmbH
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.EngineIdProvider;
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
 * Oracle implementation of the {@link DatabaseDialect} interface
 * It supports multiple engines (cluster) connected to one database.
 * 
 * @author austermann
 */
public class OracleDialect implements DatabaseDialect, DatabaseDialectMXBean {

    private static final Logger logger = LoggerFactory.getLogger(OracleDialect.class);

    // internal members
    private StmtStatistic dequeueAllStmtStatistic;
    private StmtStatistic dequeueQueryBPsStmtStatistic;
    private StmtStatistic dequeueQueryResponsesStmtStatistic;
    private StmtStatistic dequeueMarkStmtStatistic;
    private StmtStatistic enqueueUpdateStateStmtStatistic;
    private StmtStatistic insertStmtStatistic;
    private StmtStatistic deleteStaleResponsesStmtStatistic;
    private StmtStatistic dequeueWait4RespLdrStmtStatistic;
    private final Map<String, ResponseLoader> responseLoaders = new HashMap<String, ResponseLoader>();
    private WorkflowPersistencePlugin workflowPersistencePlugin = WorkflowPersistencePlugin.NULL_PLUGIN;

    // mandatory Properties
    private WorkflowRepository wfRepository = null;
    private EngineIdProvider engineIdProvider = null;

    // optional Properties
    private boolean multiEngineMode = false;
    private int lockWaitSeconds = 10;
    private RuntimeStatisticsCollector runtimeStatisticsCollector = new NullRuntimeStatisticsCollector();
    private Serializer serializer = new StandardJavaSerializer();
    private boolean removeWhenFinished = true;
    private long defaultStaleResponseRemovalTimeout = 60 * 60 * 1000;
    private int dbBatchingLatencyMSec = 0;

    public OracleDialect() {
        initStmtStats();
    }

    public void startup() {
        if (engineIdProvider == null || engineIdProvider.getEngineId() == null)
            throw new NullPointerException("EngineId is NULL! Change your " + getClass().getSimpleName() + " configuration.");
    }

    private void initStmtStats() {
        dequeueAllStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery.all", runtimeStatisticsCollector);
        dequeueQueryBPsStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery.queryBPs", runtimeStatisticsCollector);
        dequeueQueryResponsesStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery.queryResponses", runtimeStatisticsCollector);
        dequeueMarkStmtStatistic = new StmtStatistic("DBStorage.dequeue.mark", runtimeStatisticsCollector);
        enqueueUpdateStateStmtStatistic = new StmtStatistic("DBStorage.enqueue.updateState", runtimeStatisticsCollector);
        insertStmtStatistic = new StmtStatistic("DBStorage.insert", runtimeStatisticsCollector);
        deleteStaleResponsesStmtStatistic = new StmtStatistic("DBStorage.deleteStaleResponses", runtimeStatisticsCollector);
        dequeueWait4RespLdrStmtStatistic = new StmtStatistic("DBStorage.wait4resLoaderStmtStatistic", runtimeStatisticsCollector);

    }

    public void setDbBatchingLatencyMSec(int dbBatchingLatencyMSec) {
        this.dbBatchingLatencyMSec = dbBatchingLatencyMSec;
    }

    /**
     * Sets the default removal timeout for stale responses in the underlying database. A response is stale/timed out
     * when
     * there is no workflow instance waiting for it within the specified amount of time.
     * 
     * @param defaultStaleResponseRemovalTimeout
     *        timeout
     */
    public void setDefaultStaleResponseRemovalTimeout(long defaultStaleResponseRemovalTimeout) {
        this.defaultStaleResponseRemovalTimeout = defaultStaleResponseRemovalTimeout;
    }

    public void setRemoveWhenFinished(boolean removeWhenFinished) {
        this.removeWhenFinished = removeWhenFinished;
    }

    public void setEngineIdProvider(EngineIdProvider engineIdProvider) {
        this.engineIdProvider = engineIdProvider;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public void setLockWaitSeconds(int lockWaitSeconds) {
        this.lockWaitSeconds = lockWaitSeconds;
    }

    public void setMultiEngineMode(boolean multiEngineMode) {
        this.multiEngineMode = multiEngineMode;
    }

    public void setRuntimeStatisticsCollector(RuntimeStatisticsCollector runtimeStatisticsCollector) {
        this.runtimeStatisticsCollector = runtimeStatisticsCollector;
    }

    public void setWfRepository(WorkflowRepository wfRepository) {
        this.wfRepository = wfRepository;
    }

    public RuntimeStatisticsCollector getRuntimeStatisticsCollector() {
        return runtimeStatisticsCollector;
    }

    public boolean isRemoveWhenFinished() {
        return removeWhenFinished;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public long getDefaultStaleResponseRemovalTimeout() {
        return defaultStaleResponseRemovalTimeout;
    }

    @Override
    public void resumeBrokenBusinessProcesses(Connection con) throws Exception {
        logger.info("Reactivating queue entries...");
        final PreparedStatement stmt = con.prepareStatement("UPDATE COP_QUEUE SET engine_id = null WHERE engine_id=?");
        try {
            stmt.setString(1, engineIdProvider.getEngineId());
            stmt.execute();
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
        logger.info("done!");
    }

    @Override
    public List<Workflow<?>> dequeue(final String ppoolId, final int max, Connection con) throws Exception {
        logger.trace("dequeue({},{})", ppoolId, max);

        final long startTS = System.currentTimeMillis();
        final List<Workflow<?>> rv = new ArrayList<Workflow<?>>(max);

        lock(con, "dequeue#" + ppoolId);

        ResponseLoader responseLoader = getResponseLoader(ppoolId);
        responseLoader.setCon(con);
        responseLoader.setSerializer(serializer);
        responseLoader.setEngineId(engineIdProvider.getEngineId());
        responseLoader.beginTxn();

        final List<OracleSetToError.Command> invalidWorkflowInstances = new ArrayList<OracleSetToError.Command>();
        final PreparedStatement dequeueStmt = con.prepareStatement("select id,priority,data,rowid,long_data,creation_ts,object_state,long_object_state from COP_WORKFLOW_INSTANCE where rowid in (select * from (select WFI_ROWID from COP_QUEUE where ppool_id=? and engine_id is null order by ppool_id, priority, last_mod_ts) where rownum <= ?)");
        final Map<String, Workflow<?>> map = new HashMap<String, Workflow<?>>(max * 3);
        try {
            dequeueStmt.setString(1, ppoolId);
            dequeueStmt.setInt(2, max);
            dequeueAllStmtStatistic.start();
            dequeueQueryBPsStmtStatistic.start();
            final ResultSet rs = dequeueStmt.executeQuery();
            dequeueQueryBPsStmtStatistic.stop(1);
            while (rs.next()) {
                final String id = rs.getString(1);
                final int prio = rs.getInt(2);
                final String rowid = rs.getString(4);
                final Timestamp creationTS = rs.getTimestamp(6);
                try {
                    String objectState = rs.getString(7);
                    if (objectState == null)
                        objectState = rs.getString(8);
                    String data = rs.getString(3);
                    if (data == null)
                        data = rs.getString(5);
                    SerializedWorkflow sw = new SerializedWorkflow();
                    sw.setData(data);
                    sw.setObjectState(objectState);
                    PersistentWorkflow<?> wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
                    wf.setId(id);
                    wf.setProcessorPoolId(ppoolId);
                    wf.setPriority(prio);
                    wf.rowid = rowid;
                    wf.oldPrio = prio;
                    wf.oldProcessorPoolId = ppoolId;
                    WorkflowAccessor.setCreationTS(wf, new Date(creationTS.getTime()));
                    map.put(wf.getId(), wf);
                    responseLoader.enqueue(wf);
                } catch (Exception e) {
                    logger.error("decoding of '" + id + "' failed: " + e.toString(), e);
                    invalidWorkflowInstances.add(new OracleSetToError.Command(new DummyPersistentWorkflow(id, ppoolId, rowid, prio), e, System.currentTimeMillis() + dbBatchingLatencyMSec, DBProcessingState.INVALID, new Acknowledge.BestEffortAcknowledge()));
                }
            }
        } finally {
            JdbcUtils.closeStatement(dequeueStmt);
        }

        dequeueWait4RespLdrStmtStatistic.start();
        responseLoader.endTxn();
        dequeueWait4RespLdrStmtStatistic.stop(map.size());

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Collection<PersistentWorkflow<?>> workflows = (Collection<PersistentWorkflow<?>>) (Collection) map.values();
        workflowPersistencePlugin.onWorkflowsLoaded(con, workflows);
        rv.addAll(workflows);

        dequeueAllStmtStatistic.stop(map.size());

        handleInvalidWorkflowInstances(con, invalidWorkflowInstances);

        if (logger.isDebugEnabled())
            logger.debug("dequeue for pool " + ppoolId + " returns " + rv.size() + " element(s) in " + (System.currentTimeMillis() - startTS) + " msec.");
        return rv;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void handleInvalidWorkflowInstances(Connection con, final List invalidWorkflowInstances) throws Exception {
        logger.debug("invalidWorkflowInstances.size()={}", invalidWorkflowInstances.size());
        if (invalidWorkflowInstances.isEmpty()) {
            return;
        }
        ((BatchCommand) invalidWorkflowInstances.get(0)).executor().doExec(invalidWorkflowInstances, con);
    }

    @Override
    public int updateQueueState(final int max, final Connection con) throws SQLException {
        CallableStatement stmt = null;
        try {
            final long startTS = System.currentTimeMillis();
            lock(con, "updateQueueState");
            enqueueUpdateStateStmtStatistic.start();
            stmt = con.prepareCall("begin COP_COREENGINE.enqueue(?,?); end;");
            stmt.setInt(1, max);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            int rowcount = stmt.getInt(2);
            enqueueUpdateStateStmtStatistic.stop(rowcount == 0 ? 1 : rowcount);
            logger.debug("Queue update in {} msec", System.currentTimeMillis() - startTS);
            return rowcount;
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    @Override
    public int deleteStaleResponse(Connection con, int maxRows) throws Exception {
        if (logger.isTraceEnabled())
            logger.trace("deleteStaleResponse()");

        lock(con, "deleteStaleResponse");

        final PreparedStatement stmt = con.prepareStatement("delete from COP_RESPONSE r where response_timeout < ? and not exists (select * from COP_WAIT w where w.correlation_id = r.correlation_id) and rownum <= " + maxRows);
        try {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            deleteStaleResponsesStmtStatistic.start();
            final int rowCount = stmt.executeUpdate();
            deleteStaleResponsesStmtStatistic.stop(rowCount);
            logger.trace("deleted {} stale response(s).", rowCount);
            return rowCount;
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    /**
     * returns an int value between 0 and 1073741823 (exclusive)
     */
    static int computeLockId(String s) {
        // This method handles the following fact: Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE
        int hashCode = s.hashCode();
        if (hashCode == Integer.MIN_VALUE) {
            hashCode = 13;
        }
        return Math.abs(hashCode) % 1073741823;
    }

    private void lock(Connection c, String context) throws SQLException {
        if (!multiEngineMode)
            return;
        final int lockId = computeLockId(context);
        int result = 0;
        for (int i = 0; i < 3; i++) {
            if (logger.isDebugEnabled())
                logger.debug("Trying to acquire db lock for '" + context + "' ==> lockId=" + lockId);
            CallableStatement stmt = c.prepareCall("{? = call dbms_lock.request(?,DBMS_LOCK.X_MODE,?,TRUE)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, lockId); // lock id
            stmt.setInt(3, lockWaitSeconds); // wait time in seconds
            stmt.execute();
            result = stmt.getInt(1);
            if (logger.isDebugEnabled())
                logger.debug("acquire lock returned with value '" + result + "'");
            if (result == 0 /* OK */|| result == 4 /* Already own lock specified by id or lockhandle */)
                return;
            if (result == 3 /* Parameter error */|| result == 5 /* Illegal lock handle */)
                throw new SQLException(result == 3 ? "Parameter error" : "Illegal lock handle");
            assert result == 1 || result == 2;
        }
        if (result == 1)
            throw new SQLException("unable to acquire lock: timeout");
        if (result == 2)
            throw new SQLException("unable to acquire lock: deadlock");
    }

    @Override
    public void insert(final List<Workflow<?>> wfs, final Connection con) throws Exception {
        final PreparedStatement stmt = con.prepareStatement("INSERT INTO COP_WORKFLOW_INSTANCE (ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,DATA,LONG_DATA,OBJECT_STATE,LONG_OBJECT_STATE,CREATION_TS,CLASSNAME) VALUES (?,?,?,SYSTIMESTAMP,?,?,?,?,?,?,?)");
        try {
            int n = 0;
            for (int i = 0; i < wfs.size(); i++) {
                Workflow<?> wf = wfs.get(i);
                final SerializedWorkflow sw = serializer.serializeWorkflow(wf);
                stmt.setString(1, wf.getId());
                stmt.setInt(2, DBProcessingState.ENQUEUED.ordinal());
                stmt.setInt(3, wf.getPriority());
                stmt.setString(4, wf.getProcessorPoolId());
                if (sw.getData() != null) {
                    stmt.setString(5, sw.getData().length() > 4000 ? null : sw.getData());
                    stmt.setString(6, sw.getData().length() > 4000 ? sw.getData() : null);
                } else {
                    stmt.setString(5, null);
                    stmt.setString(6, null);
                }
                if (sw.getObjectState() != null) {
                    stmt.setString(7, sw.getObjectState().length() > 4000 ? null : sw.getObjectState());
                    stmt.setString(8, sw.getObjectState().length() > 4000 ? sw.getObjectState() : null);
                } else {
                    stmt.setString(7, null);
                    stmt.setString(8, null);
                }
                stmt.setTimestamp(9, new Timestamp(wf.getCreationTS().getTime()));
                stmt.setString(10, wf.getClass().getName());
                stmt.addBatch();
                n++;
                if (i % 100 == 0 || (i + 1) == wfs.size()) {
                    insertStmtStatistic.start();
                    stmt.executeBatch();
                    insertStmtStatistic.stop(n);
                    n = 0;
                }
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<PersistentWorkflow<?>> uncheckedWfs = (List) wfs;
            workflowPersistencePlugin.onWorkflowsSaved(con, uncheckedWfs);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new DuplicateIdException(e);
            }
            throw e;
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    @Override
    public void insert(final Workflow<?> wf, final Connection con) throws Exception {
        final List<Workflow<?>> wfs = new ArrayList<Workflow<?>>(1);
        wfs.add(wf);
        insert(wfs, con);
    }

    @Override
    public void restart(final String workflowInstanceId, Connection c) throws Exception {
        logger.trace("restart({})", workflowInstanceId);
        CallableStatement stmt = c.prepareCall("begin COP_COREENGINE.restart(?); end;");
        try {
            stmt.setString(1, workflowInstanceId);
            stmt.execute();
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
        logger.info(workflowInstanceId + " successfully queued for restart.");
    }

    private ResponseLoader getResponseLoader(final String ppoolId) {
        ResponseLoader responseLoader = null;
        synchronized (responseLoaders) {
            responseLoader = responseLoaders.get(ppoolId);
            if (responseLoader == null) {
                responseLoader = new ResponseLoader(dequeueQueryResponsesStmtStatistic, dequeueMarkStmtStatistic);
                responseLoader.start();
                responseLoaders.put(ppoolId, responseLoader);
            }
        }
        return responseLoader;
    }

    @Override
    public void restartAll(Connection c) throws Exception {
        logger.trace("restartAll()");
        CallableStatement stmt = c.prepareCall("begin COP_COREENGINE.restart_all; end;");
        try {
            stmt.execute();
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
        logger.info("All error/invalid workflow instances successfully queued for restart.");
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
    public BatchCommand createBatchCommand4Finish(final Workflow<?> w, final Acknowledge callback) {
        final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
        return new OracleRemove.Command(pwf, removeWhenFinished, System.currentTimeMillis() + dbBatchingLatencyMSec, workflowPersistencePlugin, callback);
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public BatchCommand createBatchCommand4Notify(final Response<?> response, final Acknowledge callback) throws Exception {
        if (response == null)
            throw new NullPointerException();
        if (response.isEarlyResponseHandling())
            return new OracleNotify.Command(response, serializer, defaultStaleResponseRemovalTimeout, System.currentTimeMillis() + dbBatchingLatencyMSec, callback);
        else
            return new OracleNotifyNoEarlyResponseHandling.Command(response, serializer, defaultStaleResponseRemovalTimeout, System.currentTimeMillis() + dbBatchingLatencyMSec, callback);
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public BatchCommand createBatchCommand4registerCallback(final RegisterCall rc, final ScottyDBStorageInterface dbStorageInterface, final Acknowledge callback) throws Exception {
        if (rc == null)
            throw new NullPointerException();
        return new OracleRegisterCallback.Command(rc, serializer, dbStorageInterface, System.currentTimeMillis() + dbBatchingLatencyMSec, workflowPersistencePlugin, callback);
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public BatchCommand createBatchCommand4error(Workflow<?> w, Throwable t, DBProcessingState dbProcessingState, final Acknowledge callback) {
        final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
        return new OracleSetToError.Command(pwf, t, System.currentTimeMillis() + dbBatchingLatencyMSec, callback);
    }

    public void error(Workflow<?> w, Throwable t, Connection con) throws Exception {
        runSingleBatchCommand(con, createBatchCommand4error(w, t, DBProcessingState.ERROR, new Acknowledge.BestEffortAcknowledge()));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void runSingleBatchCommand(Connection con, BatchCommand cmd) throws Exception {
        List<BatchCommand> commands = Collections.singletonList(cmd);
        cmd.executor().doExec(commands, con);
    }

    public List<String> checkDbConsistency(Connection con) throws Exception {
        if (multiEngineMode)
            throw new CopperRuntimeException("Cannot check DB consistency when multiEngineMode is turned on!");

        final PreparedStatement dequeueStmt = con.prepareStatement("select id,priority,creation_ts,data,long_data,object_state,long_object_state,PPOOL_ID from COP_WORKFLOW_INSTANCE where state not in (?,?)");
        try {
            final List<String> idsOfBadWorkflows = new ArrayList<String>();
            dequeueStmt.setInt(1, DBProcessingState.INVALID.ordinal());
            dequeueStmt.setInt(2, DBProcessingState.FINISHED.ordinal());
            ResultSet rs = dequeueStmt.executeQuery();
            while (rs.next()) {
                final String id = rs.getString(1);
                try {
                    final int prio = rs.getInt(2);
                    String data = rs.getString(4);
                    if (data == null)
                        data = rs.getString(5);
                    String objectState = rs.getString(6);
                    if (objectState == null)
                        objectState = rs.getString(7);
                    final String ppoolId = rs.getString(8);
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
            return idsOfBadWorkflows;
        } finally {
            JdbcUtils.closeStatement(dequeueStmt);
        }
    }

    public void shutdown() {
        synchronized (responseLoaders) {
            for (ResponseLoader responseLoader : responseLoaders.values()) {
                responseLoader.shutdown();
            }
        }
    }

    @Override
    public int getDbBatchingLatencyMSec() {
        return dbBatchingLatencyMSec;
    }

    @Override
    public String getDialectDescription() {
        return "Oracle";
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
            readStmt = con.prepareStatement("select id,priority,data,rowid,long_data,creation_ts,object_state,long_object_state,ppool_id from COP_WORKFLOW_INSTANCE where id = ?");
            readStmt.setString(1, workflowInstanceId);

            final ResultSet rs = readStmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                return null;
            }
            PersistentWorkflow<?> wf;
            final String id = rs.getString(1);
            final int prio = rs.getInt(2);
            final String rowid = rs.getString(4);
            final Timestamp creationTS = rs.getTimestamp(6);
            String objectState = rs.getString(7);
            if (objectState == null)
                objectState = rs.getString(8);
            String data = rs.getString(3);
            if (data == null)
                data = rs.getString(5);
            SerializedWorkflow sw = new SerializedWorkflow();
            sw.setData(data);
            sw.setObjectState(objectState);
            wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
            wf.setId(id);
            wf.setProcessorPoolId(rs.getString(9));
            wf.setPriority(prio);
            wf.rowid = rowid;
            wf.oldPrio = prio;
            wf.oldProcessorPoolId = rs.getString(9);
            WorkflowAccessor.setCreationTS(wf, new Date(creationTS.getTime()));
            rs.close();
            readStmt.close();

            selectResponsesStmt = con.prepareStatement("select w.WORKFLOW_INSTANCE_ID, w.correlation_id, r.response, r.long_response, w.is_timed_out from (select WORKFLOW_INSTANCE_ID, correlation_id, case when timeout_ts < systimestamp then 1 else 0 end is_timed_out from COP_WAIT where WORKFLOW_INSTANCE_ID = ?) w, COP_RESPONSE r where w.correlation_id = r.correlation_id(+)");
            selectResponsesStmt.setString(1, workflowInstanceId);
            ResultSet rsResponses = selectResponsesStmt.executeQuery();
            while (rsResponses.next()) {
                String cid = rsResponses.getString(2);
                boolean isTimeout = rsResponses.getBoolean(5);
                String response = rsResponses.getString(3);
                if (response == null)
                    response = rsResponses.getString(4);
                Response<?> r = null;
                if (response != null) {
                    r = (Response<?>) serializer.deserializeResponse(response);
                    wf.addResponseId(r.getResponseId());
                } else if (isTimeout) {
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
        PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,object_state,creation_ts,PPOOL_ID from COP_WORKFLOW_INSTANCE where id = ?");
        dequeueStmt.setString(1, workflowId);
        return dequeueStmt;
    }

}
