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
 * PostgreSQL implementation of the {@link ScottyDBStorageInterface}.
 *
 * @author austermann
 */
public class PostgreSQLDialect extends AbstractSqlDialect {
    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLDialect.class);

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
        PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,object_state,creation_ts from COP_WORKFLOW_INSTANCE where id in (select WORKFLOW_INSTANCE_ID from COP_QUEUE where ppool_id = ? order by priority, last_mod_ts) LIMIT " + max);
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
            queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts from COP_WORKFLOW_INSTANCE where state in (0,1,2) and classname=? LIMIT " + max);
            queryStmt.setString(1, className);
        } else {
            queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts from COP_WORKFLOW_INSTANCE where state in (0,1,2) LIMIT " + max);
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
    protected void lock(Connection con, final String lockContext) throws SQLException {
        if (!multiEngineMode) {
            return;
        }
        if (logger.isDebugEnabled())
            logger.debug("Trying to acquire db lock for '" + lockContext);
        final int lockId = computeLockId(lockContext);
        PreparedStatement stmt = con.prepareStatement("SELECT pg_advisory_lock(?)");
        stmt.setInt(1, lockId);
        try {
            stmt.executeQuery();
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    @Override
    protected void releaseLock(Connection con, final String lockContext) {
        if (!multiEngineMode) {
            return;
        }
        if (logger.isDebugEnabled())
            logger.debug("Trying to release db lock for '" + lockContext);
        PreparedStatement stmt = null;
        final int lockId = computeLockId(lockContext);
        try {
            stmt = con.prepareStatement("select pg_advisory_unlock(?)");
            stmt.setInt(1, lockId);

            final ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                final boolean releaseLockResult = rs.getBoolean(1);
                if (releaseLockResult) {
                    // success
                    return;
                }
                throw new SQLException("error pg_advisory_unlock(" + lockId + ")");
            }
        } catch (SQLException e) {
            logger.error("release_lock failed", e);
        } finally {
            if (stmt != null) {
                JdbcUtils.closeStatement(stmt);
            }
        }

    }
}
