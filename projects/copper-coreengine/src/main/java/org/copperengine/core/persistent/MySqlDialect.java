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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.List;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.db.utility.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQL implementation of the {@link DatabaseDialect} interface.
 *
 * @author austermann
 */
public class MySqlDialect extends AbstractSqlDialect {

    private static final Logger logger = LoggerFactory.getLogger(MySqlDialect.class);

    public MySqlDialect() {
        super(true, false);
    }

    @Override
    protected PreparedStatement createUpdateStateStmt(final Connection c, final int max) throws SQLException {
        final Timestamp NOW = new Timestamp(System.currentTimeMillis());
        PreparedStatement pstmt = c.prepareStatement(queryUpdateQueueState + " LIMIT 0," + max);
        pstmt.setTimestamp(1, NOW);
        pstmt.setTimestamp(2, NOW);
        return pstmt;
    }

    @Override
    protected PreparedStatement createDequeueStmt(final Connection c, final String ppoolId, final int max) throws SQLException {
        PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,object_state,creation_ts,last_mod_ts from COP_WORKFLOW_INSTANCE where id in (select WORKFLOW_INSTANCE_ID from COP_QUEUE where ppool_id = ?  and engine_id is NULL order by priority, last_mod_ts) LIMIT 0," + max);
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
        return new MySqlNotifyNoEarlyResponseHandling.Command(response, serializer, defaultStaleResponseRemovalTimeout, System.currentTimeMillis() + dbBatchingLatencyMSec, ack);
    }

    @Override
    public void insert(List<Workflow<?>> wfs, Connection con) throws DuplicateIdException, Exception {
        try {
            super.insert(wfs, con);
        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException || (e.getCause() != null && e.getCause() instanceof SQLIntegrityConstraintViolationException)) {
                throw new DuplicateIdException(e);
            }
            throw e;
        }
    }

    @Override
    public String getDialectDescription() {
        return "MySQL";
    }

    @Override
    protected PreparedStatement createQueryAllActiveStmt(Connection c, String className, int max) throws SQLException {
        PreparedStatement queryStmt;
        if (className != null) {
            queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts,last_mod_ts,timeout from COP_WORKFLOW_INSTANCE where state in (0,1,2) and classname=? LIMIT 0," + max);
            queryStmt.setString(1, className);
        } else {
            queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts,last_mod_ts,timeout from COP_WORKFLOW_INSTANCE where state in (0,1,2) LIMIT 0," + max);
        }
        return queryStmt;
    }

    /**
     * Note: For MySQL the advisory lock only applies to the current connection, if the connection terminates, it will
     * release the lock automatically.
     * If you try to lock multiple times on the same lockContext, for the same connection, you need to release multiple
     * times, it won't deadlock since version 5.7.5, please consult:
     * <a href="http://dev.mysql.com/doc/refman/5.7/en/miscellaneous-functions.html#function_get-lock">
     * http://dev.mysql.com/doc/refman/5.7/en/miscellaneous-functions.html#function_get-lock</a>
     */
    @Override
    protected void doLock(Connection con, final String lockContext) throws SQLException {
        logger.debug("Trying to acquire db lock for '{}'", lockContext);
        PreparedStatement stmt = con.prepareStatement("select get_lock(?,?)");
        stmt.setString(1, lockContext);
        stmt.setInt(2, ACQUIRE_BLOCKING_WAIT_SEC);
        try {
            final ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                final int lockResult = rs.getInt(1);
                if (lockResult == 1) {
                    // success
                    return;
                }

                final String errorMsgPrefix = "error acquire lock(" + lockContext + "," + ACQUIRE_BLOCKING_WAIT_SEC + "): ";
                if (rs.wasNull()) {
                    throw new SQLException(errorMsgPrefix + "unknown");
                } else if (lockResult == 0) {
                    // timeout
                    throw new SQLException(errorMsgPrefix + "timeout");
                }
            }
            // something else must be horribly wrong
            throw new SQLException("Please check your version of MySQL, to make sure it supports get_lock() & release_lock()");
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    @Override
    protected void doReleaseLock(Connection con, final String lockContext) {
        logger.debug("Trying to release db lock for '{}'", lockContext);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("select release_lock(?)");
            stmt.setString(1, lockContext);

            final ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                final int releaseLockResult = rs.getInt(1);
                if (releaseLockResult == 1) {
                    // success
                    return;
                }

                final String errorMsgPrefix = "error release_lock(" + lockContext + "): ";
                if (rs.wasNull()) {
                    throw new SQLException(errorMsgPrefix + "doesn't exist");
                } else if (releaseLockResult == 0) {
                    // failed release the lock
                    throw new SQLException(errorMsgPrefix + "not current connection's lock");
                }
            }
            // something else must be horribly wrong
            throw new SQLException("Please check your version of MySQL, to make sure it supports get_lock() & release_lock()");
        } catch (SQLException e) {
            logger.error("release_lock failed", e);
        } finally {
            if (stmt != null) {
                JdbcUtils.closeStatement(stmt);
            }
        }
    }
    

    @Override
    protected void addLimitation(StringBuilder sql, List<Object> params, int max) {
        sql.append(" LIMIT 0,").append(max);
    }       
}
