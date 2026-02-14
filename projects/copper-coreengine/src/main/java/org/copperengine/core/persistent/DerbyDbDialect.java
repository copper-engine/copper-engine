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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
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
 * Apache Derby implementation of the {@link DatabaseDialect} interface.
 *
 * @author austermann
 */
public class DerbyDbDialect extends AbstractSqlDialect {

    private static final Logger logger = LoggerFactory.getLogger(DerbyDbDialect.class);

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public synchronized void startup() {
        try {
            if (dataSource == null)
                throw new NullPointerException("dataSource in " + getClass().getSimpleName() + " is null");
            checkAndCreateSchema(dataSource);
        } catch (Exception e) {
            throw new Error("startup failed", e);
        }
        super.startup();
    }

    @Override
    protected PreparedStatement createUpdateStateStmt(final Connection c, final int max) throws SQLException {
        final Timestamp NOW = new Timestamp(System.currentTimeMillis());
        PreparedStatement pstmt = c.prepareStatement(queryUpdateQueueState + " FETCH FIRST " + max + " ROWS ONLY");
        pstmt.setTimestamp(1, NOW);
        pstmt.setTimestamp(2, NOW);
        return pstmt;
    }

    @Override
    protected PreparedStatement createDequeueStmt(final Connection c, final String ppoolId, final int maxRows) throws SQLException {
        PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,object_state,creation_ts,last_mod_ts from COP_WORKFLOW_INSTANCE where id in (select WORKFLOW_INSTANCE_ID from COP_QUEUE where ppool_id = ? and engine_id is NULL order by priority, last_mod_ts) FETCH FIRST " + maxRows + " ROWS ONLY");
        dequeueStmt.setString(1, ppoolId);
        return dequeueStmt;
    }

    @Override
    protected PreparedStatement createDeleteStaleResponsesStmt(final Connection c, final int maxRows) throws SQLException {
        PreparedStatement stmt = c.prepareStatement("delete from COP_RESPONSE where response_timeout < ? and not exists (select * from COP_WAIT w where w.correlation_id = COP_RESPONSE.correlation_id FETCH FIRST " + maxRows + " ROWS ONLY)");
        stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        return stmt;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public BatchCommand createBatchCommand4error(Workflow<?> w, Throwable t, DBProcessingState dbProcessingState, Acknowledge ack) {
        return new DerbyDbSetToError.Command((PersistentWorkflow<?>) w, t, dbProcessingState, ack);
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
            if (e instanceof SQLIntegrityConstraintViolationException || (e.getCause() != null && e.getCause() instanceof SQLIntegrityConstraintViolationException)) {
                throw new DuplicateIdException(e);
            }
            throw e;
        }
    }

    @Override
    public String getDialectDescription() {
        return "DerbyDB";
    }

    public static void checkAndCreateSchema(DataSource ds) throws SQLException, IOException {
        Connection c = ds.getConnection();
        try {
            if (tablesExist(c)) {
                logger.info("COPPER schema already exists");
                return;
            }
            logger.info("Creating COPPER schema...");
            BufferedReader reader = new BufferedReader(new InputStreamReader(DerbyDbDialect.class.getResourceAsStream("/derbydb/create-schema.sql")));
            try {
                String s;
                StringBuilder sb = new StringBuilder(256);
                while ((s = reader.readLine()) != null) {
                    s = s.trim();
                    if (s.length() == 0 || s.startsWith("--"))
                        continue;
                    sb.append(s);
                    if (s.endsWith(";")) {
                        sb.deleteCharAt(sb.length() - 1);
                        logger.info("Executing: " + sb.toString());
                        String sql = sb.toString();
                        Statement stmt = c.createStatement(
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_UPDATABLE,
                                ResultSet.CLOSE_CURSORS_AT_COMMIT);
                        try {
                            stmt.execute(sql);
                        } catch (SQLException e) {
                            logger.error("", e);
                        } finally {
                            stmt.close();
                        }
                        sb = new StringBuilder(256);
                    } else {
                        sb.append("\n");
                    }
                }
            } finally {
                reader.close();
            }
        } finally {
            c.close();
        }
        logger.info("Created COPPER schema.");
    }

    private static boolean tablesExist(Connection c) throws SQLException {
        final Statement stmt = c.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT);
        try {
            stmt.execute("SELECT count(*) FROM COP_WORKFLOW_INSTANCE");
            logger.debug("COP_WORKFLOW_INSTANCE exists");
            return true;
        } catch (SQLException e) {
            logger.debug("COP_WORKFLOW_INSTANCE does not exist");
            return false;
        } finally {
            stmt.close();
        }
    }

    public static void shutdownDerby() {
        boolean gotSQLExc = false;
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException se) {
            if (se.getSQLState().equals("XJ015")) {
                gotSQLExc = true;
            }
        }
        if (!gotSQLExc) {
            logger.warn("Database did not shut down normally");
        } else {
            logger.info("Database shut down normally");
        }
    }

    @Override
    protected PreparedStatement createQueryAllActiveStmt(Connection c, String className, int max) throws SQLException {
        PreparedStatement queryStmt;
        if (className != null) {
            queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts,last_mod_ts,timeout from COP_WORKFLOW_INSTANCE where state in (0,1,2) and classname=? FETCH FIRST " + max + " ROWS ONLY");
            queryStmt.setString(1, className);
        } else {
            queryStmt = c.prepareStatement("select id,state,priority,ppool_id,data,object_state,creation_ts,last_mod_ts,timeout from COP_WORKFLOW_INSTANCE where state in (0,1,2) FETCH FIRST " + max + " ROWS ONLY");
        }
        return queryStmt;
    }

    @Override
    protected void addLimitation(StringBuilder sql, int max) {
        sql.append(" FETCH FIRST " + max + " ROWS ONLY");
    }

    @Override
    protected void addLimitationAndOffset(StringBuilder sql,int max, int offset) {
        sql.append(" OFFSET " + offset + " ROWS");
        addLimitation(sql, max);
    }
}
