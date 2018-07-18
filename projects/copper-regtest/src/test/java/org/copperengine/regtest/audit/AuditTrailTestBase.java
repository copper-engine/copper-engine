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

import org.copperengine.core.audit.AbstractAuditTrail;
import org.copperengine.core.audit.AbstractAuditTrail.Property2ColumnMapping;
import org.copperengine.core.audit.AuditTrailEvent;
import org.copperengine.core.audit.BatchInsertIntoAutoTrail.Command;
import org.copperengine.core.audit.BatchInsertIntoAutoTrail.Executor;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.NullCallback;
import org.copperengine.regtest.persistent.DataSourceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.*;

public abstract class AuditTrailTestBase {

    private static final Logger logger = LoggerFactory.getLogger(AuditTrailTestBase.class);

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
        AbstractAuditTrail auditTrail = getAuditTrail();
        auditTrail.startup();

        assertEquals("INSERT INTO COP_AUDIT_TRAIL_EVENT (LOGLEVEL,OCCURRENCE,CONVERSATION_ID,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE,LONG_MESSAGE) VALUES (?,?,?,?,?,?,?,?,?)", auditTrail.getSqlStmt());
    }

    protected AbstractAuditTrail getAuditTrail() throws Exception {
        AbstractAuditTrail auditTrail = getTestAuditTrail();
        auditTrail.setDataSource(ds);

        return auditTrail;
    }

    abstract AbstractAuditTrail getTestAuditTrail() throws Exception;

    @Test
    public void testLog() throws Exception {
        AbstractAuditTrail auditTrail = getAuditTrail();
        auditTrail.startup();

        Connection con = ds.getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.execute("DELETE FROM COP_AUDIT_TRAIL_EVENT");
            stmt.close();

            AuditTrailEvent e = new AuditTrailEvent(1, new Date(), "conversationId", "context", "instanceId", "correlationId", "transactionId", "message", "messageType", null);

            doSynchLog(auditTrail, con, e);
            con.commit();
        } finally {
            con.close();
        }

        assertEquals("INSERT INTO COP_AUDIT_TRAIL_EVENT (LOGLEVEL,OCCURRENCE,CONVERSATION_ID,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE,LONG_MESSAGE) VALUES (?,?,?,?,?,?,?,?,?)", auditTrail.getSqlStmt());
    }

    private void doSynchLog(AbstractAuditTrail auditTrail, Connection con, AuditTrailEvent e) throws Exception {
        @SuppressWarnings("unchecked")
        BatchCommand<Executor, Command> cmd = auditTrail.createBatchCommand(e, true, NullCallback.instance);
        Collection<BatchCommand<Executor, Command>> cmdList = Arrays.<BatchCommand<Executor, Command>>asList(cmd);
        cmd.executor().doExec(cmdList, con);
    }

    @Test
    public void testCustomTable() throws Exception {
        createCustomAuditTrailTable();

        final ArrayList<Property2ColumnMapping> additionalMapping = new ArrayList<Property2ColumnMapping>();
        additionalMapping.add(new Property2ColumnMapping("customInt", "CUSTOM_INT"));
        additionalMapping.add(new Property2ColumnMapping("customTimestamp", "CUSTOM_TIMESTAMP"));
        additionalMapping.add(new Property2ColumnMapping("customVarchar", "CUSTOM_VARCHAR"));

        final AbstractAuditTrail auditTrail = getAuditTrail();
        auditTrail.setDbTable("COP_AUDIT_TRAIL_EVENT_EXTENDED");
        auditTrail.setAuditTrailEventClass(ExtendedAutitTrailEvent.class);
        auditTrail.setAdditionalMapping(additionalMapping);
        auditTrail.startup();

        assertEquals("INSERT INTO COP_AUDIT_TRAIL_EVENT_EXTENDED (CUSTOM_INT,CUSTOM_TIMESTAMP,CUSTOM_VARCHAR,LOGLEVEL,OCCURRENCE,CONVERSATION_ID,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE,LONG_MESSAGE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", auditTrail.getSqlStmt());

        final Connection con = ds.getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.execute("DELETE FROM COP_AUDIT_TRAIL_EVENT_EXTENDED");

            ExtendedAutitTrailEvent e = new ExtendedAutitTrailEvent(1, new Date(), "conversationId", "context", "instanceId", "correlationId", "transactionId", "message", "messageType", "TEST", 4711, new Timestamp(System.currentTimeMillis()));
            doSynchLog(auditTrail, con, e);
            con.commit();

            AuditTrailEvent e2 = new AuditTrailEvent(1, new Date(), "conversationId", "context", "instanceId", "correlationId", "transactionId", "message", "messageType", null);
            doSynchLog(auditTrail, con, e2);
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
