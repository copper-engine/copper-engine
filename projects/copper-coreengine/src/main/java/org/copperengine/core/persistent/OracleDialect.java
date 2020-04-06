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

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.CopperException;
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
import org.copperengine.core.util.FunctionWithException;
import org.copperengine.management.DatabaseDialectMXBean;
import org.copperengine.management.model.AuditTrailInfo;
import org.copperengine.management.model.AuditTrailInstanceFilter;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.copperengine.core.util.StateMapper;

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
    private StmtStatistic selectQueueSizeStmtStatistic;
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
    private boolean concurrentResponseLoading = true;

    public OracleDialect() {
    }

    @Override
    public void startup() {
        if (engineIdProvider == null || engineIdProvider.getEngineId() == null)
            throw new NullPointerException("EngineId is NULL! Change your " + getClass().getSimpleName() + " configuration.");
        initStmtStats();
    }

    private void initStmtStats() {
        dequeueAllStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery.all", runtimeStatisticsCollector);
        dequeueQueryBPsStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery.queryBPs", runtimeStatisticsCollector);
        dequeueQueryResponsesStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery.queryResponses", runtimeStatisticsCollector);
        dequeueMarkStmtStatistic = new StmtStatistic("DBStorage.dequeue.mark", runtimeStatisticsCollector);
        enqueueUpdateStateStmtStatistic = new StmtStatistic("DBStorage.enqueue.updateState", runtimeStatisticsCollector);
        insertStmtStatistic = new StmtStatistic("DBStorage.insert", runtimeStatisticsCollector);
        deleteStaleResponsesStmtStatistic = new StmtStatistic("DBStorage.deleteStaleResponses", runtimeStatisticsCollector);
        dequeueWait4RespLdrStmtStatistic = new StmtStatistic("DBStorage.wait4resLoader", runtimeStatisticsCollector);
        selectQueueSizeStmtStatistic = new StmtStatistic("DBStorage.selectQueueSize", runtimeStatisticsCollector);
    }

    public void setConcurrentResponseLoading(boolean concurrentResponseLoading) {
        this.concurrentResponseLoading = concurrentResponseLoading;
    }

    @Override
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
    @Override
    public void setDefaultStaleResponseRemovalTimeout(long defaultStaleResponseRemovalTimeout) {
        this.defaultStaleResponseRemovalTimeout = defaultStaleResponseRemovalTimeout;
    }

    @Override
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
        initStmtStats();
    }

    public void setWfRepository(WorkflowRepository wfRepository) {
        this.wfRepository = wfRepository;
    }

    public RuntimeStatisticsCollector getRuntimeStatisticsCollector() {
        return runtimeStatisticsCollector;
    }

    @Override
    public boolean isRemoveWhenFinished() {
        return removeWhenFinished;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    @Override
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
        final PreparedStatement dequeueStmt = con.prepareStatement("select id,priority,data,rowid,long_data,creation_ts,object_state,long_object_state,last_mod_ts from COP_WORKFLOW_INSTANCE where rowid in (select * from (select WFI_ROWID from COP_QUEUE where ppool_id=? and engine_id is null order by ppool_id, priority, last_mod_ts) where rownum <= ?)");
        final Map<String, Workflow<?>> map = new HashMap<String, Workflow<?>>(max * 3);
        try {
            dequeueStmt.setString(1, ppoolId);
            dequeueStmt.setInt(2, max);
            dequeueStmt.setFetchSize(500);
            dequeueAllStmtStatistic.start();
            logger.trace("Query next {} elements from queue", max);
            dequeueQueryBPsStmtStatistic.start();
            final ResultSet rs = dequeueStmt.executeQuery();
            dequeueQueryBPsStmtStatistic.stop(1);
            logger.trace("Query finished - fetching results...");
            while (rs.next()) {
                final String id = rs.getString(1);
                final int prio = rs.getInt(2);
                final String rowid = rs.getString(4);
                final Timestamp creationTS = rs.getTimestamp(6);
                final Timestamp lastModTS = rs.getTimestamp(9);
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
                    WorkflowAccessor.setLastActivityTS(wf, new Date(lastModTS.getTime()));
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
        logger.trace("Done fetching results. Waiting for response loader to finishe");
        dequeueWait4RespLdrStmtStatistic.start();
        responseLoader.endTxn();
        dequeueWait4RespLdrStmtStatistic.stop(map.size());
        logger.trace("Done waiting for response loader");

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Collection<PersistentWorkflow<?>> workflows = (Collection) map.values();
        workflowPersistencePlugin.onWorkflowsLoaded(con, workflows);
        rv.addAll(workflows);

        dequeueAllStmtStatistic.stop(map.size());

        handleInvalidWorkflowInstances(con, invalidWorkflowInstances);

        logger.debug("dequeue for pool {} returns {} element(s) in {} msec.", ppoolId, rv.size(), (System.currentTimeMillis() - startTS));
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
                responseLoader = concurrentResponseLoading ? new ConcurrentResponseLoader(dequeueQueryResponsesStmtStatistic, dequeueMarkStmtStatistic) : new DownstreamResponseLoader(dequeueQueryResponsesStmtStatistic, dequeueMarkStmtStatistic);
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
    public void deleteBroken(String workflowInstanceId, Connection c) throws Exception {
        logger.trace("deleteBroken()");

        CallableStatement stmt = c.prepareCall("begin COP_COREENGINE.deleteBrokenWorkflow(?, ?); end;");
        try {
            stmt.setString(1, workflowInstanceId);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            int delCount = stmt.getInt(2);
            if (delCount != 1) {
                throw new CopperException("Workflow \"" + workflowInstanceId + "\" can't be deleted. Is it a valid id and really in broken state? (Invalid or error?)");
            }
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
        logger.info("error/invalid workflow instance successfully deleted.");
    }

    @Override
    public void deleteWaiting(String workflowInstanceId, Connection c) throws Exception {
        logger.trace("deleteWaiting()");

        CallableStatement stmt = c.prepareCall("begin COP_COREENGINE.deleteWaitingWorkflow(?, ?); end;");
        try {
            stmt.setString(1, workflowInstanceId);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            int delCount = stmt.getInt(2);
            if (delCount != 1) {
                throw new CopperException("Workflow \"" + workflowInstanceId + "\" can't be deleted. Is it a valid id and really in Waiting state?");
            }
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
        logger.info("waiting workflow instance successfully deleted.");
    }

    @Override
    public void restartFiltered(WorkflowInstanceFilter filter, Connection c) throws Exception {
        logger.trace("restartFiltered()");

        List<String> states = filter.getStates();
        Date lastModTo = filter.getLastModTS().getTo();
        Date lastModFrom = filter.getLastModTS().getFrom();
        Date creationTo = filter.getCreationTS().getTo();
        Date creationFrom = filter.getCreationTS().getFrom();
        Timestamp lastModToTS;
        Timestamp lastModFromTS;
        Timestamp creationToTS;
        Timestamp creationFromTS;
        if (lastModTo == null) {
            lastModToTS = null;
        } else {
            lastModToTS = new Timestamp(filter.getLastModTS().getTo().getTime());
        }
        if (lastModFrom == null) {
            lastModFromTS = null;
        } else {
            lastModFromTS = new Timestamp(filter.getLastModTS().getFrom().getTime());
        }
        if (creationTo == null) {
            creationToTS = null;
        } else {
            creationToTS = new Timestamp(filter.getCreationTS().getTo().getTime());
        }
        if (creationFrom == null) {
            creationFromTS = null;
        } else {
            creationFromTS = new Timestamp(filter.getCreationTS().getFrom().getTime());
        }
        CallableStatement stmt = c.prepareCall("begin COP_COREENGINE.restartFiltered(?, ?, ?, ?, ?, ?, ?, ?); end;");
        try {
            stmt.setInt(1, this.getINTfromBOOLEAN(states.contains(ProcessingState.ERROR.name())));
            stmt.setInt(2, this.getINTfromBOOLEAN(states.contains(ProcessingState.INVALID.name())));
            stmt.setString(3, filter.getWorkflowClassname());
            stmt.setTimestamp(4, lastModToTS);
            stmt.setTimestamp(5, lastModFromTS);
            stmt.setTimestamp(6, creationToTS);
            stmt.setTimestamp(7, creationFromTS);

            stmt.registerOutParameter(8, Types.INTEGER);
            stmt.execute();
            int restartCount = stmt.getInt(8);
            if (restartCount == 0) {
                throw new CopperException("Filtered Workflows could not be restarted");
            }
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
        logger.info("Filtered workflows successfully restarted.");
    }

    @Override
    public void deleteFiltered(WorkflowInstanceFilter filter, Connection c) throws Exception {
        logger.trace("deleteFiltered()");
        List<String> states = filter.getStates();
        Date lastModTo = filter.getLastModTS().getTo();
        Date lastModFrom = filter.getLastModTS().getFrom();
        Date creationTo = filter.getCreationTS().getTo();
        Date creationFrom = filter.getCreationTS().getFrom();
        Timestamp lastModToTS;
        Timestamp lastModFromTS;
        Timestamp creationToTS;
        Timestamp creationFromTS;
        if (lastModTo == null) {
            lastModToTS = null;
        } else {
            lastModToTS = new Timestamp(filter.getLastModTS().getTo().getTime());
        }
        if (lastModFrom == null) {
            lastModFromTS = null;
        } else {
            lastModFromTS = new Timestamp(filter.getLastModTS().getFrom().getTime());
        }
        if (creationTo == null) {
            creationToTS = null;
        } else {
            creationToTS = new Timestamp(filter.getCreationTS().getTo().getTime());
        }
        if (creationFrom == null) {
            creationFromTS = null;
        } else {
            creationFromTS = new Timestamp(filter.getCreationTS().getFrom().getTime());
        }
        CallableStatement stmt = c.prepareCall("begin COP_COREENGINE.deleteFiltered(?, ?, ?, ?, ?, ?, ?, ?, ?); end;");
        try {
            stmt.setInt(1, this.getINTfromBOOLEAN(states.contains(ProcessingState.WAITING.name())));
            stmt.setInt(2, this.getINTfromBOOLEAN(states.contains(ProcessingState.ERROR.name())));
            stmt.setInt(3, this.getINTfromBOOLEAN(states.contains(ProcessingState.INVALID.name())));
            stmt.setString(4, filter.getWorkflowClassname());
            stmt.setTimestamp(5, lastModToTS);
            stmt.setTimestamp(6, lastModFromTS);
            stmt.setTimestamp(7, creationToTS);
            stmt.setTimestamp(8, creationFromTS);

            stmt.registerOutParameter(9, Types.INTEGER);
            stmt.execute();
            int delCount = stmt.getInt(9);
            if (delCount == 0) {
                throw new CopperException("Filtered Workflows could not be deleted");
            }
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
        logger.info("Filtered workflows successfully deleted.");
    }

    public int getINTfromBOOLEAN(Boolean input) {
        if (input == true) {return 1;}
        else {return 0;}
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

    @Override
    public List<String> checkDbConsistency(Connection con) throws Exception {
        if (multiEngineMode) {
            logger.warn("Checking DB consistency when multiEngineMode is turned on!");
        }
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

    @Override
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
            readStmt = con.prepareStatement("select id,priority,data,rowid,long_data,creation_ts,object_state,long_object_state,ppool_id,state,last_mod_ts from COP_WORKFLOW_INSTANCE where id = ?");
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
            final Timestamp lastModTS = rs.getTimestamp(11);
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
            WorkflowAccessor.setLastActivityTS(wf, new Date(lastModTS.getTime()));
            DBProcessingState dbProcessingState = DBProcessingState.getByOrdinal(rs.getInt(10));
            ProcessingState state = DBProcessingState.getProcessingStateByState(dbProcessingState);
            WorkflowAccessor.setProcessingState(wf, state);

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
                    r = serializer.deserializeResponse(response);
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

    @Override
    public List<Workflow<?>> queryAllActive(final String className, final Connection c, final int max) throws SQLException {
        PreparedStatement queryStmt = null;
        try {
            if (className != null) {
                queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts,last_mod_ts from COP_WORKFLOW_INSTANCE where state in (0,1,2) and classname=? and rownum <=?");
                queryStmt.setString(1, className);
                queryStmt.setInt(2, max);
            } else {
                queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts,last_mod_ts from COP_WORKFLOW_INSTANCE where state in (0,1,2) and rownum <=?");
                queryStmt.setInt(1, max);
            }
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
                    WorkflowAccessor.setLastActivityTS(wf, new Date(rs.getTimestamp(8).getTime()));
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

    public static boolean schemaMatches(Connection c) throws SQLException {
        try (PreparedStatement stmt = c.prepareStatement("select count(*) from user_tab_columns where TABLE_NAME='COP_QUEUE' and COLUMN_NAME='WFI_ROWID'")) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) == 1;
        }
    }
    
    @Override
    public Date readDatabaseClock(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("SELECT SYSTIMESTAMP FROM DUAL")) {
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getTimestamp(1);
        }
    } 
    
    @Override
    public int queryQueueSize(String processorPoolId, int max, Connection con) throws SQLException {
        int queueSize;
        selectQueueSizeStmtStatistic.start();
        try (PreparedStatement pstmt = con.prepareStatement("SELECT count(*) FROM COP_QUEUE WHERE PPOOL_ID=? AND ROWNUM <= ?")) {
            pstmt.setString(1, processorPoolId);
            pstmt.setInt(2, max);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            queueSize = rs.getInt(1);
        }
        selectQueueSizeStmtStatistic.stop(queueSize);
        return queueSize;
    }


    private StringBuilder appendQueryBase(StringBuilder sql, List<Object> params, WorkflowInstanceFilter filter) {
        sql.append(" FROM (SELECT w.timeout, w.classname, (CASE WHEN q.wfi_rowid IS NOT NULL AND w.STATE=2 THEN 0 ELSE w.STATE END) STATE, w.ID, w.PRIORITY, w.PPOOL_ID, w.DATA, w.OBJECT_STATE, w.CREATION_TS, w.LAST_MOD_TS, q.ENGINE_ID FROM COP_WORKFLOW_INSTANCE w LEFT OUTER JOIN COP_QUEUE q on w.rowid = q.wfi_rowid) x WHERE 1=1");
        if (filter.getWorkflowClassname() != null) {
            sql.append(" AND x.CLASSNAME=?");
            params.add(filter.getWorkflowClassname());
        }
        if (filter.getProcessorPoolId() != null) {
            sql.append(" AND x.PPOOL_ID=?");
            params.add(filter.getProcessorPoolId());
        }

        this.appendSqlDates(sql, params, filter);
        CommonSQLHelper.appendStates(sql, params, filter);
        return sql;
    }

    private static StringBuilder appendSqlDates(StringBuilder sql, List<Object> params, WorkflowInstanceFilter filter) {
        if (filter.getCreationTS() != null) {
            if (filter.getCreationTS().getFrom() != null) {
                sql.append(" AND x.CREATION_TS >= ?");
                params.add(new java.sql.Date(filter.getCreationTS().getFrom().getTime()));
            }
            if (filter.getCreationTS().getTo() != null) {
                sql.append(" AND x.CREATION_TS < ?");
                params.add(new java.sql.Date(filter.getCreationTS().getTo().getTime()));
            }
        }
        if (filter.getLastModTS() != null) {
            if (filter.getLastModTS().getFrom() != null) {
                sql.append(" AND x.LAST_MOD_TS >= ?");
                params.add(new java.sql.Date(filter.getLastModTS().getFrom().getTime()));
            }
            if (filter.getLastModTS().getTo() != null) {
                sql.append(" AND x.LAST_MOD_TS < ?");
                params.add(new java.sql.Date(filter.getLastModTS().getTo().getTime()));
            }
        }

        return sql;
    }

    @Override
    public String queryObjectState(String id, Connection con) throws Exception {
        PersistentWorkflow decodedState;
        String codedState = null;

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT OBJECT_STATE FROM COP_WORKFLOW_INSTANCE WHERE ID = ?");
        PreparedStatement prepedStmt = con.prepareStatement(sql.toString());
        prepedStmt.setString(1, id);
        ResultSet rs = prepedStmt.executeQuery();

        while (rs.next()) {
            codedState = rs.getString("OBJECT_STATE");
        }
        JdbcUtils.closeStatement(prepedStmt);

        try {
            decodedState = (PersistentWorkflow<?>) serializer.deserializeStateOnly(codedState, wfRepository);
        } catch (Exception e) {
            logger.error("decoding of '" + id + "' failed: " + e.toString(), e);
            throw new CopperException("Workflow \"" + id + "\" can't be deserialzed");
        }

        Map<String, Object> map = StateMapper.mapState(decodedState);

        return map.toString();
    }

    @Override
    public List<Workflow<?>> queryWorkflowInstances(WorkflowInstanceFilter filter, Connection con) throws SQLException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT x.*");
        final List<Object> params = new ArrayList<>();
        appendQueryBase(sql, params, filter);

        if (filter.getOffset() > 0) {
            addLimitationAndOffset(sql, filter.getMax(), filter.getOffset());
        } else {
            addLimitation(sql, filter.getMax());
        }

        logger.debug("queryWorkflowInstances: sql={}, params={}", sql, params);

        final StringBuilder sqlQueryErrorData = new StringBuilder("select x.* from (select * from COP_WORKFLOW_INSTANCE_ERROR where WORKFLOW_INSTANCE_ID=? order by ERROR_TS desc) x where 1=1");
        addLimitation(sqlQueryErrorData, 1);
        return CommonSQLHelper.processResult(sql.toString(), params,
                    sqlQueryErrorData.toString(), con, (FunctionWithException<ResultSet,PersistentWorkflow<?>>) this::decode);
    }

    @Override
    public int countWorkflowInstances(WorkflowInstanceFilter filter, Connection con) throws SQLException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) as COUNT_NUMBER");

        final List<Object> params = new ArrayList<>();
        appendQueryBase(sql, params, filter);
        logger.debug("queryWorkflowInstances: sql={}, params={}", sql, params);

        return CommonSQLHelper.processCountResult(sql,params, con);
    }

    @Override
    public List<AuditTrailInfo> queryAuditTrailInstances(AuditTrailInstanceFilter filter, Connection c) throws SQLException {
        logger.trace("queryAuditTrailInstances()");

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT SEQ_ID, TRANSACTION_ID, CONVERSATION_ID, CORRELATION_ID, OCCURRENCE, LOGLEVEL, CONTEXT, INSTANCE_ID, MESSAGE_TYPE ");

        if (filter.isIncludeMessages()) {
            sql.append(", LONG_MESSAGE");
        }

        final List<Object> params = new ArrayList<>();
        appendAuditTrailQueryBase(sql, params, filter);

        if (filter.getOffset() > 0 && filter.getMax() > 0) {
            addLimitationAndOffset(sql, filter.getMax(), filter.getOffset());
        } else if (filter.getMax() > 0){
            addLimitation(sql, filter.getMax());
        }

        logger.debug("queryAuditTrailInstances: sql={}, params={}", sql, params);

        return CommonSQLHelper.processAuditResult(sql.toString(), params, c, filter.isIncludeMessages(), true);
    }

    private StringBuilder appendAuditTrailQueryBase(StringBuilder sql, List<Object> params, AuditTrailInstanceFilter filter) {
        sql.append(" FROM COP_AUDIT_TRAIL_EVENT WHERE 1=1 ");

        if (filter.getLevel() != null && filter.getLevel() > 0) {
            sql.append(" AND LOGLEVEL >= ? ");
            params.add(filter.getLevel());
        }
        if (!isBlank(filter.getCorrelationId())) {
            sql.append(" AND CORRELATION_ID = ? ");
            params.add(filter.getCorrelationId());
        }

        if (!isBlank(filter.getInstanceId())) {
            sql.append(" AND INSTANCE_ID = ? ");
            params.add(filter.getInstanceId());
        }

        if (!isBlank(filter.getConversationId())) {
            sql.append(" AND CONVERSATION_ID = ? ");
            params.add(filter.getConversationId());
        }

        if (!isBlank(filter.getTransactionId())) {
            sql.append(" AND TRANSACTION_ID = ? ");
            params.add(filter.getTransactionId());
        }

        if (filter.getOccurredFrom() != null) {
            sql.append(" AND OCCURRENCE >= ? ");
            params.add(filter.getOccurredFrom());
        }

        if (filter.getOccurredTo() != null) {
            sql.append(" AND OCCURRENCE <= ? ");
            params.add(filter.getOccurredTo());
        }

        return sql;
    }

    protected boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String queryAuditTrailMessage(long id, Connection con) throws SQLException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT LONG_MESSAGE FROM COP_AUDIT_TRAIL_EVENT WHERE SEQ_ID = ?");

        try (PreparedStatement pStmtQueryWFIs = con.prepareStatement(sql.toString())) {
            pStmtQueryWFIs.setObject(1, id);
            ResultSet rs = pStmtQueryWFIs.executeQuery();
            while (rs.next()) {
                try {
                    Clob message = rs.getClob("LONG_MESSAGE");
                    if ((int) message.length() > 0) {
                        return message.getSubString(1, (int) message.length());
                    }
                    return null;
                } catch (Exception e) {
                    logger.error("decoding of '" + rs + "' failed: " + e.toString(), e);
                }
            }
        }

        return null;
    }

    @Override
    public int countAuditTrailInstances(AuditTrailInstanceFilter filter, Connection con) throws SQLException {
        logger.trace("countAuditTrailInstances()");
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) as COUNT_NUMBER");
        final List<Object> params = new ArrayList<>();

        appendAuditTrailQueryBase(sql, params, filter);
        logger.debug("queryWorkflowInstances: sql={}, params={}", sql, params);

        return CommonSQLHelper.processCountResult(sql,params, con);
    }

    protected PersistentWorkflow<?> decode(ResultSet rs) throws SQLException, Exception {
        final String id = rs.getString("ID");
        final int prio = rs.getInt("PRIORITY");
        final String ppoolId = rs.getString("PPOOL_ID");
        final SerializedWorkflow sw = new SerializedWorkflow();
        sw.setData(rs.getString("DATA"));
        sw.setObjectState(rs.getString("OBJECT_STATE"));
        final PersistentWorkflow<?> wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
        wf.setId(id);
        wf.setProcessorPoolId(ppoolId);
        wf.setPriority(prio);

        final DBProcessingState dbProcessingState = DBProcessingState.getByOrdinal(rs.getInt("STATE"));
        final ProcessingState state = DBProcessingState.getProcessingStateByState(dbProcessingState);
        WorkflowAccessor.setProcessingState(wf, state);
        WorkflowAccessor.setCreationTS(wf, new Date(rs.getTimestamp("CREATION_TS").getTime()));
        WorkflowAccessor.setLastActivityTS(wf, new Date(rs.getTimestamp("LAST_MOD_TS").getTime()));
        WorkflowAccessor.setTimeoutTS(wf, rs.getTimestamp("TIMEOUT"));
        return wf;
    }

    protected void addLimitation(StringBuilder sql, int max) {
        sql.append(" FETCH FIRST " + max + " ROWS ONLY");
    }

    protected void addLimitationAndOffset(StringBuilder sql,int max, int offset) {
        sql.append(" OFFSET " + offset + " ROWS");
        addLimitation(sql, max);
    }

}
