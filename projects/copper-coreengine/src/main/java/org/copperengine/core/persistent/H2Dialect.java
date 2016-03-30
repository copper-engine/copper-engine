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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.batcher.BatchCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * H2 implementation of the {@link DatabaseDialect} interface.
 *
 * @author dmoebius
 * @since 3.1
 */
public class H2Dialect extends AbstractSqlDialect {

    private static final Logger logger = LoggerFactory.getLogger(H2Dialect.class);

    private DataSource dataSource;
    private boolean autocreateSchema = false;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setAutocreateSchema(boolean autocreateSchema) {
        this.autocreateSchema = autocreateSchema;
    }

    @Override
    public String getDialectDescription() {
        return "H2";
    }

    @Override
    public synchronized void startup() {
        try {
            if (dataSource == null)
                throw new NullPointerException("dataSource in " + getClass().getSimpleName() + " is null");
            if (autocreateSchema)
                checkAndCreateSchema(dataSource);
        } catch (Exception e) {
            throw new Error("startup failed", e);
        }
        super.startup();
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
    protected PreparedStatement createDequeueStmt(final Connection c, final String ppoolId, final int maxRows) throws SQLException {
        PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,object_state,creation_ts from COP_WORKFLOW_INSTANCE where id in (select WORKFLOW_INSTANCE_ID from COP_QUEUE where ppool_id = ? order by priority, last_mod_ts) LIMIT " + maxRows);
        dequeueStmt.setString(1, ppoolId);
        return dequeueStmt;
    }

    @Override
    protected PreparedStatement createDeleteStaleResponsesStmt(final Connection c, final int maxRows) throws SQLException {
        PreparedStatement stmt = c.prepareStatement("delete from COP_RESPONSE where response_timeout < ? and not exists (select * from COP_WAIT w where w.correlation_id = COP_RESPONSE.correlation_id LIMIT " + maxRows + ")");
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
        return new SqlNotifyNoEarlyResponseHandling.Command(response, serializer, defaultStaleResponseRemovalTimeout, System.currentTimeMillis() + dbBatchingLatencyMSec, ack);
    }

    @Override
    public void insert(List<Workflow<?>> wfs, Connection con) throws DuplicateIdException, Exception {
        try {
            super.insert(wfs, con);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // The error with code 23505 is thrown when trying to insert a row that would violate a unique index or
                // primary key.
                // See http://www.h2database.com/javadoc/org/h2/constant/ErrorCode.html#c23505
                throw new DuplicateIdException(e);
            }
            throw e;
        }
    }

    public static void checkAndCreateSchema(DataSource ds) throws SQLException, IOException {
        Connection c = ds.getConnection();
        try {
            if (tablesExist(c)) {
                logger.info("COPPER schema already exists");
                return;
            }
            logger.info("Creating COPPER schema...");
            String sql = getResourceAsString(H2Dialect.class, "/h2/create-schema.sql");
            Statement stmt = c.createStatement();
            try {
                stmt.execute(sql);
            } finally {
                stmt.close();
            }
        } finally {
            c.close();
        }
        logger.info("Created COPPER schema.");
    }

    public static void dropSchema(DataSource ds) throws SQLException, IOException {
        Connection c = ds.getConnection();
        try {
            logger.info("Dropping COPPER schema...");
            String sql = getResourceAsString(H2Dialect.class, "/h2/drop-schema.sql");
            Statement stmt = c.createStatement();
            try {
                stmt.execute(sql);
            } finally {
                stmt.close();
            }
        } finally {
            c.close();
        }
        logger.info("Dropped COPPER schema.");
    }

    private static boolean tablesExist(Connection c) throws SQLException {
        final Statement stmt = c.createStatement();
        try {
            stmt.execute("SELECT count(*) FROM COP_WORKFLOW_INSTANCE");
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            stmt.close();
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

}
