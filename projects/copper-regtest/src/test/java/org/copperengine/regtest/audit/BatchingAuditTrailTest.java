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
package org.copperengine.regtest.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.sql.DataSource;

import org.copperengine.core.audit.AuditTrailEvent;
import org.copperengine.core.audit.BatchInsertIntoAutoTrail.Command;
import org.copperengine.core.audit.BatchInsertIntoAutoTrail.Executor;
import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.audit.BatchingAuditTrail.Property2ColumnMapping;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.NullCallback;
import org.copperengine.regtest.persistent.DataSourceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchingAuditTrailTest {

    private static final Logger logger = LoggerFactory.getLogger(BatchingAuditTrailTest.class);

    DataSource ds;

    @Before
    public void setUp() throws Exception {
        ds = DataSourceFactory.createDerbyDbDatasource();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetSqlStmt() throws Exception {
        BatchingAuditTrail batchingAuditTrail = new BatchingAuditTrail();
        batchingAuditTrail.setDataSource(ds);
        batchingAuditTrail.startup();

        assertEquals("INSERT INTO COP_AUDIT_TRAIL_EVENT (LOGLEVEL,OCCURRENCE,CONVERSATION_ID,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE,LONG_MESSAGE) VALUES (?,?,?,?,?,?,?,?,?)", batchingAuditTrail.getSqlStmt());
    }

    @Test
    public void testLog() throws Exception {
        BatchingAuditTrail batchingAuditTrail = new BatchingAuditTrail();
        batchingAuditTrail.setDataSource(ds);
        batchingAuditTrail.startup();

        Connection con = ds.getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.execute("DELETE FROM COP_AUDIT_TRAIL_EVENT");
            stmt.close();

            AuditTrailEvent e = new AuditTrailEvent(1, new Date(), "conversationId", "context", "instanceId", "correlationId", "transactionId", "message", "messageType", null);

            doSynchLog(batchingAuditTrail, con, e);
            con.commit();
        } finally {
            con.close();
        }

        assertEquals("INSERT INTO COP_AUDIT_TRAIL_EVENT (LOGLEVEL,OCCURRENCE,CONVERSATION_ID,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE,LONG_MESSAGE) VALUES (?,?,?,?,?,?,?,?,?)", batchingAuditTrail.getSqlStmt());
    }

    private void doSynchLog(BatchingAuditTrail batchingAuditTrail, Connection con, AuditTrailEvent e) throws Exception {
        @SuppressWarnings("unchecked")
        BatchCommand<Executor, Command> cmd = batchingAuditTrail.createBatchCommand(e, true, NullCallback.instance);
        Collection<BatchCommand<Executor, Command>> cmdList = Arrays.<BatchCommand<Executor, Command>>asList(cmd);
        cmd.executor().doExec(cmdList, con);
    }

    @Test
    public void testCustomTable() throws Exception {
        createCustomAuditTrailTable();

        final ArrayList<Property2ColumnMapping> additionalMapping = new ArrayList<BatchingAuditTrail.Property2ColumnMapping>();
        additionalMapping.add(new Property2ColumnMapping("customInt", "CUSTOM_INT"));
        additionalMapping.add(new Property2ColumnMapping("customTimestamp", "CUSTOM_TIMESTAMP"));
        additionalMapping.add(new Property2ColumnMapping("customVarchar", "CUSTOM_VARCHAR"));

        final BatchingAuditTrail batchingAuditTrail = new BatchingAuditTrail();
        batchingAuditTrail.setDataSource(ds);
        batchingAuditTrail.setDbTable("COP_AUDIT_TRAIL_EVENT_EXTENDED");
        batchingAuditTrail.setAuditTrailEventClass(ExtendedAutitTrailEvent.class);
        batchingAuditTrail.setAdditionalMapping(additionalMapping);
        batchingAuditTrail.startup();

        assertEquals("INSERT INTO COP_AUDIT_TRAIL_EVENT_EXTENDED (CUSTOM_INT,CUSTOM_TIMESTAMP,CUSTOM_VARCHAR,LOGLEVEL,OCCURRENCE,CONVERSATION_ID,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE,LONG_MESSAGE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", batchingAuditTrail.getSqlStmt());

        final Connection con = ds.getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.execute("DELETE FROM COP_AUDIT_TRAIL_EVENT_EXTENDED");

            ExtendedAutitTrailEvent e = new ExtendedAutitTrailEvent(1, new Date(), "conversationId", "context", "instanceId", "correlationId", "transactionId", "message", "messageType", "TEST", 4711, new Timestamp(System.currentTimeMillis()));
            doSynchLog(batchingAuditTrail, con, e);
            con.commit();

            AuditTrailEvent e2 = new AuditTrailEvent(1, new Date(), "conversationId", "context", "instanceId", "correlationId", "transactionId", "message", "messageType", null);
            doSynchLog(batchingAuditTrail, con, e2);
            con.commit();

            ResultSet rs = stmt.executeQuery("SELECT * FROM COP_AUDIT_TRAIL_EVENT_EXTENDED ORDER BY SEQ_ID ASC");
            assertTrue(rs.next());
            assertEquals("conversationId", rs.getString("CONVERSATION_ID"));
            assertEquals("TEST", rs.getString("CUSTOM_VARCHAR"));

            assertTrue(rs.next());
            assertEquals("conversationId", rs.getString("CONVERSATION_ID"));
            assertNull(rs.getString("CUSTOM_VARCHAR"));

            assertFalse(rs.next());
            rs.close();
            stmt.close();

        } finally {
            con.close();
        }

    }

    private void createCustomAuditTrailTable() throws IOException, SQLException {
        final StringBuilder sql = new StringBuilder();
        final BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("extendedAuditTrail.sql")));
        try {
            String l = null;
            while ((l = r.readLine()) != null) {
                sql.append(l).append("\n");
            }
        } finally {
            r.close();
        }

        final String sqlStmt = sql.toString();
        final Connection c = ds.getConnection();
        try {
            Statement stmt = c.createStatement();
            stmt.execute(sqlStmt);
            stmt.close();
        } catch (SQLException e) {
            logger.debug("creation of table failed", e);
        } finally {
            c.close();
        }
    }

}
