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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.batcher.BatchCommand;

/**
 * Oracle implementation of the {@link ScottyDBStorageInterface}.
 *
 * @author austermann
 */
public class OracleSimpleDialect extends AbstractSqlDialect {

    public OracleSimpleDialect() {
        super(true, false);
    }

    @Override
    protected PreparedStatement createUpdateStateStmt(final Connection c, final int max) throws SQLException {
        final Timestamp NOW = new Timestamp(System.currentTimeMillis());
        PreparedStatement pstmt = c.prepareStatement(queryUpdateQueueState + " AND ROWNUM <= " + max);
        pstmt.setTimestamp(1, NOW);
        pstmt.setTimestamp(2, NOW);
        return pstmt;
    }

    @Override
    protected PreparedStatement createDequeueStmt(final Connection c, final String ppoolId, final int max) throws SQLException {
        String sql = "select id,priority,data,object_state,creation_ts,last_mod_ts from COP_WORKFLOW_INSTANCE where id in (select * from (select WORKFLOW_INSTANCE_ID from COP_QUEUE where ppool_id = ? and engine_id is NULL order by priority, last_mod_ts) where rownum <= " + max + ")";
        PreparedStatement dequeueStmt = c.prepareStatement(sql);
        dequeueStmt.setString(1, ppoolId);
        return dequeueStmt;
    }

    @Override
    protected PreparedStatement createDeleteStaleResponsesStmt(final Connection c, final int MAX_ROWS) throws SQLException {
        PreparedStatement stmt = c.prepareStatement("delete from COP_RESPONSE where response_timeout < ? and not exists (select * from COP_WAIT w where w.correlation_id = COP_RESPONSE.correlation_id AND ROWNUM <= " + MAX_ROWS + ")");
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
        return new OracleSimpleNotifyNoEarlyResponseHandling.Command(response, serializer, defaultStaleResponseRemovalTimeout, System.currentTimeMillis() + dbBatchingLatencyMSec, ack);
    }

    @Override
    public String getDialectDescription() {
        return "Oracle simple";
    }

    @Override
    protected PreparedStatement createQueryAllActiveStmt(Connection c, String className, int max) throws SQLException {
        PreparedStatement queryStmt;
        if (className != null) {
            queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts,last_mod_ts,timeout from COP_WORKFLOW_INSTANCE where state in (0,1,2) and classname=? AND ROWNUM <= " + max);
            queryStmt.setString(1, className);
        } else {
            queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts,last_mod_ts,timeout from COP_WORKFLOW_INSTANCE where state in (0,1,2) AND ROWNUM <= " + max);
        }
        return queryStmt;
    }

    @Override
    protected void doLock(Connection con, final String lockContext) throws SQLException {
        try (CallableStatement stmt = con.prepareCall("{? = call SYS.DBMS_LOCK.REQUEST(?, SYS.DBMS_LOCK.X_MODE, SYS.DBMS_LOCK.MAXWAIT, TRUE)}")) {
            stmt.registerOutParameter(1, java.sql.Types.INTEGER);
            stmt.setInt(2, computeLockId(lockContext));
            stmt.execute();
            int rv = stmt.getInt(1);
            if (rv != 0 /* OK */&& rv != 4 /* Already own lock */) {
                throw new SQLException("DBMS_LOCK.REQUEST failed (" + rv + ")");
            }
        }
    }

    @Override
    protected void doReleaseLock(Connection con, final String lockContext) {
        // DBMS_LOCK is automatically released on commit
    }

    @Override
    public void insert(Workflow<?> wf, Connection con) throws Exception {
        try {
            super.insert(wf, con);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new DuplicateIdException(e);
            }
            throw e;
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
    

    @Override
    protected void addLimitation(StringBuilder sql, int max) {
        sql.append(" AND ROWNUM <= ").append(max);
    }

    @Override
    protected void addLimitationAndOffset(StringBuilder sql, int max, int offset) {
        sql.append(" OFFSET " + offset + " ROWS");
        addLimitation(sql, max);
    }
}
