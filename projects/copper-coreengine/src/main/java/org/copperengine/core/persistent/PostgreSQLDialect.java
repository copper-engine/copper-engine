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
import java.util.Date;
import java.util.List;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.db.utility.JdbcUtils;
import org.copperengine.core.util.FunctionWithException;
import org.copperengine.management.model.AuditTrailInfo;
import org.copperengine.management.model.AuditTrailInstanceFilter;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL implementation of the {@link ScottyDBStorageInterface}.
 *
 * @author austermann
 */
public class PostgreSQLDialect extends AbstractSqlDialect {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLDialect.class);

    public PostgreSQLDialect() {
        super(true, false, false);
    }

    @Override
    protected PreparedStatement createUpdateStateStmt(final Connection c, final int max) throws SQLException {
        final Timestamp NOW = new Timestamp(System.currentTimeMillis());
        PreparedStatement pstmt = c.prepareStatement(queryUpdateQueueState + " LIMIT " + max);
        pstmt.setTimestamp(1, NOW);
        pstmt.setTimestamp(2, NOW);
        return pstmt;
    }

    @Override
    protected PreparedStatement createDequeueStmt(final Connection c, final String ppoolId, final int max) throws SQLException {
        PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,object_state,creation_ts,last_mod_ts from COP_WORKFLOW_INSTANCE where id in (select WORKFLOW_INSTANCE_ID from COP_QUEUE where ppool_id = ?  and engine_id is NULL order by priority, last_mod_ts) LIMIT " + max);
        dequeueStmt.setString(1, ppoolId);
        return dequeueStmt;
    }

    @Override
    protected PreparedStatement createDeleteStaleResponsesStmt(final Connection c, final int MAX_ROWS) throws SQLException {
        PreparedStatement stmt = c.prepareStatement("delete from COP_RESPONSE where response_timeout < ? and not exists (select * from COP_WAIT w where w.correlation_id = COP_RESPONSE.correlation_id LIMIT " + MAX_ROWS + ")");
        stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        return stmt;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public BatchCommand createBatchCommand4error(Workflow<?> w, Throwable t, DBProcessingState dbProcessingState, Acknowledge ack) {
        return new SqlSetToError.Command((PersistentWorkflow<?>) w, t, dbProcessingState, System.currentTimeMillis() + dbBatchingLatencyMSec, ack);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public BatchCommand createBatchCommand4NotifyNoEarlyResponseHandling(Response<?> response, Acknowledge ack) throws Exception {
        return new PostgreSQLNotifyNoEarlyResponseHandling.Command(response, serializer, defaultStaleResponseRemovalTimeout, System.currentTimeMillis() + dbBatchingLatencyMSec, ack);
    }

    @Override
    public String getDialectDescription() {
        return "PostgreSQL";
    }

    @Override
    public void insert(List<Workflow<?>> wfs, Connection con) throws DuplicateIdException, Exception {
        try {
            super.insert(wfs, con);
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("cop_workflow_instance_pkey") || (e.getNextException() != null && e.getNextException().getMessage().toLowerCase().contains("cop_workflow_instance_pkey"))) {
                throw new DuplicateIdException(e);
            }
            throw e;
        }
    }

    @Override
    protected PreparedStatement createQueryAllActiveStmt(Connection c, String className, int max) throws SQLException {
        PreparedStatement queryStmt;
        if (className != null) {
            queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts,last_mod_ts,timeout from COP_WORKFLOW_INSTANCE where state in (0,1,2) and classname=? LIMIT " + max);
            queryStmt.setString(1, className);
        } else {
            queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts,last_mod_ts,timeout from COP_WORKFLOW_INSTANCE where state in (0,1,2) LIMIT " + max);
        }
        return queryStmt;
    }

    /**
     * ref: https://www.postgresql.org/docs/9.5/static/functions-admin.html#FUNCTIONS-ADVISORY-LOCKS
     * pg_advisory_lock locks an application-defined resource, which can be identified either by a single 64-bit key
     * value or two 32-bit key values (note that these two key spaces do not overlap). If another session already holds
     * a lock on the same resource identifier, this function will wait until the resource becomes available. The lock is
     * exclusive. Multiple lock requests stack, so that if the same resource is locked three times it must then be
     * unlocked three times to be released for other sessions' use.
     * DO NOT USE IT WITHOUT finally { release_lock(lockContext)} or it will be deadlocked!!
     */
    @Override
    protected void doLock(Connection con, final String lockContext) throws SQLException {
        logger.debug("Trying to acquire db lock for '{}'", lockContext);
        final int lockId = computeLockId(lockContext);
        PreparedStatement stmt = con.prepareStatement("SELECT pg_advisory_xact_lock (?)");
        stmt.setInt(1, lockId);
        try {
            stmt.executeQuery();
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    @Override
    protected void doReleaseLock(Connection con, final String lockContext) {
        // pg_advisory_xact_lock automatically releases the lock at the end of the transaction
    }
    

    @Override
    protected void addLimitation(StringBuilder sql, int max) {
        sql.append(" LIMIT ").append(max);
    }

    @Override
    protected void addLimitationAndOffset(StringBuilder sql, int max, int offset) {
        sql.append(" OFFSET " + offset);
        addLimitation(sql, max);
    }

    @Override
    public List<Workflow<?>> queryWorkflowInstances(WorkflowInstanceFilter filter, Connection con) throws SQLException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT x.*");
        final List<Object> params = new ArrayList<>();
        this.appendQueryBase(sql, params, filter);

        if (filter.getOffset() > 0 && filter.getMax() > 0) {
            addLimitationAndOffset(sql, filter.getMax(), filter.getOffset());
        } else if (filter.getMax() > 0){
            addLimitation(sql, filter.getMax());
        }

        logger.debug("queryWorkflowInstances: sql={}, params={}", sql, params);

        final StringBuilder sqlQueryErrorData = new StringBuilder("select x.* from (select * from COP_WORKFLOW_INSTANCE_ERROR where WORKFLOW_INSTANCE_ID=? order by ERROR_TS desc) x where 1=1");
        addLimitation(sqlQueryErrorData, 1);

        return CommonSQLHelper.processResult(sql.toString(), params, sqlQueryErrorData.toString(), con, (FunctionWithException<ResultSet,PersistentWorkflow<?>>) this::decode);
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
    public void restartFiltered(WorkflowInstanceFilter filter, Connection con) throws Exception {

        boolean validFilter = true;
        for (String state : filter.getStates()) {
            if (!state.equalsIgnoreCase("Error") && !state.equalsIgnoreCase("Invalid")) {
                validFilter = false;
            }
        }

        if (validFilter == true) {
            PreparedStatement insertStmt = null;
            PreparedStatement stmtInstance = null;
            int parameterIndexCounter = 1;
            List<Object> params = new ArrayList<>();
            StringBuilder sqlMain = new StringBuilder();
            StringBuilder sqlFilter = getSQLFilter(filter, params);
            try {

                sqlMain.append("insert into COP_QUEUE (ppool_id, priority, last_mod_ts, WORKFLOW_INSTANCE_ID) (SELECT ppool_id, priority, last_mod_ts, id FROM COP_WORKFLOW_INSTANCE as x");
                sqlMain.append(sqlFilter.toString());
                sqlMain.append(")");
                insertStmt = con.prepareStatement(sqlMain.toString());

                getSQLParams(insertStmt, filter, params, parameterIndexCounter);


                logger.info("Adding filtered WF's to queue...");
                int rowCount = insertStmt.executeUpdate();
                if (rowCount > 0) {
                    StringBuilder sql = new StringBuilder();
                    sql.append("UPDATE COP_WORKFLOW_INSTANCE as x SET x.STATE=?, x.LAST_MOD_TS=?");
                    sql.append(sqlFilter.toString());
                    stmtInstance = con.prepareStatement(sql.toString());

                    final Timestamp NOW = new Timestamp(System.currentTimeMillis());
                    stmtInstance.setInt(1, DBProcessingState.ENQUEUED.ordinal());
                    stmtInstance.setTimestamp(2, NOW);

                    getSQLParams(stmtInstance, filter, params, 3);
                    stmtInstance.execute();
                }
                logger.info("done - restartFiltered invalid: " + rowCount + " BP(s).");
            } finally {
                JdbcUtils.closeStatement(stmtInstance);
                JdbcUtils.closeStatement(insertStmt);
            }
        } else {
            logger.info("Invalid Filter applied. Filter must not contain States other than ERROR or INVALID.");
        }

    }

    private PreparedStatement getSQLParams(PreparedStatement insertStmt, WorkflowInstanceFilter filter, List<Object> params, int parameterIndexCounter) throws Exception {
        if (filter.getStates() != null) {
            for(int i = 0; i < filter.getStates().size(); i++) {
                if (filter.getStates().get(i).equalsIgnoreCase("Error")) {
                    insertStmt.setInt(parameterIndexCounter, DBProcessingState.ERROR.ordinal());
                }
                else if (filter.getStates().get(i).equalsIgnoreCase("Invalid")) {
                    insertStmt.setInt(parameterIndexCounter, DBProcessingState.INVALID.ordinal());
                }
                else if (filter.getStates().get(i).equalsIgnoreCase("Waiting")) {
                    insertStmt.setInt(parameterIndexCounter, DBProcessingState.WAITING.ordinal());
                }
                parameterIndexCounter++;

            }
        }
        if (filter.getWorkflowClassname() != null) {
            insertStmt.setObject(parameterIndexCounter, filter.getWorkflowClassname());
            parameterIndexCounter++;
        }

        for (Object time :  params) {
            Timestamp ts = new Timestamp(((Date)time).getTime());
            insertStmt.setTimestamp(parameterIndexCounter, ts);
            parameterIndexCounter++;
        }
        return insertStmt;
    }

    private StringBuilder getSQLFilter(WorkflowInstanceFilter filter, List<Object> params) {
        StringBuilder sqlFilter = new StringBuilder();
        sqlFilter.append(" WHERE 1=1");

        if (filter.getStates() != null && filter.getStates().size() != 0) {
            if (filter.getStates().size() == 1) {
                sqlFilter.append(" and x.state=?");
            } else {
                sqlFilter.append(" and (");
                for (int i = 0; i < filter.getStates().size(); i++) {
                    sqlFilter.append("x.state = ?");
                    if (i < filter.getStates().size() - 1) {
                        sqlFilter.append(" or ");
                    }
                }
                sqlFilter.append(")");
            }
        }
        if (filter.getWorkflowClassname() != null) {
            sqlFilter.append(" and x.classname=?");
        }

        CommonSQLHelper.appendSQLDates(sqlFilter, params, filter);
        return sqlFilter;
    }

    private StringBuilder appendQueryBase(StringBuilder sql, List<Object> params, WorkflowInstanceFilter filter) {
        sql.append(" FROM (SELECT w.timeout, w.classname, (CASE WHEN q.WORKFLOW_INSTANCE_ID IS NOT NULL AND w.STATE=2 THEN 0 ELSE w.STATE END) STATE, w.ID, w.PRIORITY, w.PPOOL_ID, w.DATA, w.OBJECT_STATE, w.CREATION_TS, w.LAST_MOD_TS, q.ENGINE_ID FROM COP_WORKFLOW_INSTANCE w LEFT OUTER JOIN COP_QUEUE q on w.id = q.WORKFLOW_INSTANCE_ID) x WHERE 1=1");
        if (filter.getWorkflowClassname() != null) {
            sql.append(" AND x.CLASSNAME=?");
            params.add(filter.getWorkflowClassname());
        }
        if (filter.getProcessorPoolId() != null) {
            sql.append(" AND x.PPOOL_ID=?");
            params.add(filter.getProcessorPoolId());
        }
        CommonSQLHelper.appendSQLDates(sql, params, filter);
        CommonSQLHelper.appendSQLStates(sql, params, filter);

        return sql;
    }

    @Override
    public List<AuditTrailInfo> queryAuditTrailInstances(AuditTrailInstanceFilter filter, Connection con) throws SQLException {
        logger.debug("queryAuditTrailInstances started with filter={}", filter);

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

        return CommonSQLHelper.processAuditResult(sql.toString(), params, con, filter.isIncludeMessages(), supportsClob);
    }

    @Override
    public int countAuditTrailInstances(AuditTrailInstanceFilter filter, Connection con) throws SQLException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) as COUNT_NUMBER");
        final List<Object> params = new ArrayList<>();

        appendAuditTrailQueryBase(sql, params, filter);
        logger.debug("queryWorkflowInstances: sql={}, params={}", sql, params);

        return CommonSQLHelper.processCountResult(sql,params, con);
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
            params.add(new java.sql.Date(filter.getOccurredFrom().getTime()));
        }

        if (filter.getOccurredTo() != null) {
            sql.append(" AND OCCURRENCE <= ? ");
            params.add(new java.sql.Date(filter.getOccurredTo().getTime()));
        }

        return sql;
    }

}
