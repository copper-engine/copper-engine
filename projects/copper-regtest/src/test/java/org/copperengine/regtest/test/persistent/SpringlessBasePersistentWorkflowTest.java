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
package org.copperengine.regtest.test.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.CopperException;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.EngineState;
import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.ProcessingState;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowFactory;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.audit.AuditTrailEvent;
import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.audit.CompressedBase64PostProcessor;
import org.copperengine.core.audit.DummyPostProcessor;
import org.copperengine.core.db.utility.RetryingTransaction;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.management.model.WorkflowInfo;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.copperengine.regtest.test.DataHolder;
import org.copperengine.regtest.test.backchannel.BackChannelQueue;
import org.copperengine.regtest.test.backchannel.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringlessBasePersistentWorkflowTest {

    private static final Logger logger = LoggerFactory.getLogger(SpringlessBasePersistentWorkflowTest.class);

    private static final long DEQUEUE_TIMEOUT = 120;

    static final String PersistentUnitTestWorkflow_NAME = "org.copperengine.regtest.test.persistent.PersistentUnitTestWorkflow";
    static final String WaitForEverTestWF_NAME = "org.copperengine.regtest.test.WaitForEverTestWF";
    static final String JmxTestWF_NAME = "org.copperengine.regtest.test.persistent.jmx.JmxTestWorkflow";
    static final String DeleteBrokenTestWF_NAME = "org.copperengine.regtest.test.persistent.DeleteBrokenTestWorkflow";

    public final void testDummy() {
        // for junit only
    }

    protected boolean skipTests() {
        return false;
    }

    void cleanDB(DataSource ds) throws Exception {
        new RetryingTransaction<Void>(ds) {
            @Override
            protected Void execute() throws Exception {
                Statement stmt = createStatement(getConnection());
                stmt.execute("DELETE FROM COP_AUDIT_TRAIL_EVENT");
                stmt.close();
                stmt = createStatement(getConnection());
                stmt.execute("DELETE FROM COP_WAIT");
                stmt.close();
                stmt = createStatement(getConnection());
                stmt.execute("DELETE FROM COP_RESPONSE");
                stmt.close();
                stmt = createStatement(getConnection());
                stmt.execute("DELETE FROM COP_QUEUE");
                stmt.close();
                stmt = createStatement(getConnection());
                stmt.execute("DELETE FROM COP_WORKFLOW_INSTANCE");
                stmt.close();
                stmt = createStatement(getConnection());
                stmt.execute("DELETE FROM COP_WORKFLOW_INSTANCE_ERROR");
                stmt.close();
                stmt = createStatement(getConnection());
                stmt.execute("DELETE FROM COP_LOCK");
                stmt.close();
                return null;
            }
        }.run();
    }

    private void checkNumbOfResponsesInDB(final PersistentEngineTestContext ctx, final int expected) throws Exception {
        for (int i = 0; i < 10; i++) {
            if (ctx.getBatcher().getQueueSize() == 0) {
                break;
            }
            Thread.sleep(100);
        }
        new RetryingTransaction<Void>(ctx.getDataSource()) {
            @Override
            protected Void execute() throws Exception {
                PreparedStatement pstmt = getConnection().prepareStatement("SELECT count(*) FROM COP_RESPONSE");
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                int actual = rs.getInt(1);
                org.junit.Assert.assertEquals(expected, actual);
                return null;
            }
        }.run();
    }

    private Statement createStatement(Connection con) throws SQLException {
        return con.createStatement();
    }

    final String createTestData(int length) {
        StringBuilder dataSB = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int pos = (int) (Math.random() * 70.0);
            dataSB.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890!§$%&/()=?".substring(pos, pos + 1));
        }
        return dataSB.toString();
    }

    public void testWaitForEver(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testWaitForEver");
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final BackChannelQueue backChannelQueue = context.getBackChannelQueue();
        try {
            assertEquals(EngineState.STARTED, engine.getEngineState());

            final String uuid = UUID.randomUUID().toString();
            engine.run(WaitForEverTestWF_NAME, uuid);

            WorkflowResult x = backChannelQueue.dequeue(5, TimeUnit.SECONDS);
            assertNull(x);

        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    protected void closeContext(final PersistentEngineTestContext context) {
        context.shutdown();
    }

    public void testAsynchResponse(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testAsynchResponse");
        final int NUMB = 50;
        final String DATA = createTestData(50);
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final BackChannelQueue backChannelQueue = context.getBackChannelQueue();
        try {
            assertEquals(EngineState.STARTED, engine.getEngineState());

            for (int i = 0; i < NUMB; i++) {
                engine.run(PersistentUnitTestWorkflow_NAME, DATA);
            }

            for (int i = 0; i < NUMB; i++) {
                WorkflowResult x = backChannelQueue.dequeue(DEQUEUE_TIMEOUT, TimeUnit.SECONDS);
                assertNotNull(x);
                assertNotNull(x.getResult());
                assertNotNull(x.getResult().toString().length() == DATA.length());
                assertNull(x.getException());
            }
            checkNumbOfResponsesInDB(context, 0);

        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());

    }

    public void testFailOnDuplicateInsert(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testFailOnDuplicateInsert");
        final String DATA = createTestData(50);

        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        engine.getDbStorage().setRemoveWhenFinished(false);
        try {
            WorkflowInstanceDescr<String> desc = new WorkflowInstanceDescr<String>(PersistentUnitTestWorkflow_NAME, DATA, "DUPLICATE#ID", 1, null);
            engine.run(desc);
            engine.run(desc);
            org.junit.Assert.fail("expected an DuplicateIdException");
        } catch (DuplicateIdException e) {
            // ok
        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());

    }

    public void testAsynchResponseLargeData(DataSourceType dsType, int dataSize) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testAsynchResponse");
        final int NUMB = 20;
        final String DATA = createTestData(dataSize);
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final BackChannelQueue backChannelQueue = context.getBackChannelQueue();
        try {
            assertEquals(EngineState.STARTED, engine.getEngineState());

            for (int i = 0; i < NUMB; i++) {
                engine.run(PersistentUnitTestWorkflow_NAME, DATA);
            }

            for (int i = 0; i < NUMB; i++) {
                WorkflowResult x = backChannelQueue.dequeue(DEQUEUE_TIMEOUT, TimeUnit.SECONDS);
                assertNotNull(x);
                assertNotNull(x.getResult());
                assertNotNull(x.getResult().toString().length() == DATA.length());
                assertNull(x.getException());
            }

            checkNumbOfResponsesInDB(context, 0);

        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());

    }

    protected PersistentEngineTestContext createContext(DataSourceType dsType) {
        PersistentEngineTestContext ctx = new PersistentEngineTestContext(dsType, true);
        ctx.startup();
        return ctx;
    }

    public void testWithConnection(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testWithConnection");
        final int NUMB = 20;
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final BackChannelQueue backChannelQueue = context.getBackChannelQueue();
        try {
            assertEquals(EngineState.STARTED, engine.getEngineState());

            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    for (int i = 0; i < NUMB; i++) {
                        engine.run(new WorkflowInstanceDescr<Serializable>("org.copperengine.regtest.test.persistent.DBMockAdapterUsingPersistentUnitTestWorkflow"), getConnection());
                    }
                    return null;
                }
            }.run();

            for (int i = 0; i < NUMB; i++) {
                WorkflowResult x = backChannelQueue.dequeue(DEQUEUE_TIMEOUT, TimeUnit.SECONDS);
                assertNotNull(x);
                assertNull(x.getResult());
                assertNull(x.getException());
            }

            checkNumbOfResponsesInDB(context, 0);

        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());

    }

    public void testWithConnectionBulkInsert(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testWithConnectionBulkInsert");
        final int NUMB = 50;
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final BackChannelQueue backChannelQueue = context.getBackChannelQueue();
        try {
            assertEquals(EngineState.STARTED, engine.getEngineState());

            final List<Workflow<?>> list = new ArrayList<Workflow<?>>();
            for (int i = 0; i < NUMB; i++) {
                WorkflowFactory<?> wfFactory = engine.createWorkflowFactory(PersistentUnitTestWorkflow_NAME);
                Workflow<?> wf = wfFactory.newInstance();
                list.add(wf);
            }

            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    engine.run(list, getConnection());
                    return null;
                }
            }.run();

            for (int i = 0; i < NUMB; i++) {
                WorkflowResult x = backChannelQueue.dequeue(DEQUEUE_TIMEOUT, TimeUnit.SECONDS);
                assertNotNull(x);
                assertNull(x.getResult());
                assertNull(x.getException());
            }

            checkNumbOfResponsesInDB(context, 0);

        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());

    }

    public void testTimeouts(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testTimeouts");
        final int NUMB = 10;
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final BackChannelQueue backChannelQueue = context.getBackChannelQueue();
        try {
            assertEquals(EngineState.STARTED, engine.getEngineState());

            for (int i = 0; i < NUMB; i++) {
                engine.run("org.copperengine.regtest.test.persistent.TimingOutPersistentUnitTestWorkflow", null);
            }

            for (int i = 0; i < NUMB; i++) {
                WorkflowResult x = backChannelQueue.dequeue(DEQUEUE_TIMEOUT, TimeUnit.SECONDS);
                assertNotNull(x);
                assertNull(x.getResult());
                assertNull(x.getException());
            }

            checkNumbOfResponsesInDB(context, 0);

        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());

    }

    public void testErrorHandlingInCoreEngine(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            final WorkflowInstanceDescr<Serializable> wfInstanceDescr = new WorkflowInstanceDescr<Serializable>("org.copperengine.regtest.test.persistent.ExceptionThrowingPersistentUnitTestWorkflow");
            wfInstanceDescr.setId(engine.createUUID());
            engine.run(wfInstanceDescr);
            Thread.sleep(5000);
            // check
            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    ResultSet rs = stmt.executeQuery("select * from COP_WORKFLOW_INSTANCE_ERROR");
                    assertTrue(rs.next());
                    assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
                    assertNotNull(rs.getString("EXCEPTION"));
                    assertFalse(rs.next());
                    rs.close();
                    stmt.close();
                    return null;
                }
            }.run();
            engine.restart(wfInstanceDescr.getId());
            Thread.sleep(5000);
            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    ResultSet rs = stmt.executeQuery("select * from COP_WORKFLOW_INSTANCE_ERROR");
                    assertTrue(rs.next());
                    assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
                    assertNotNull(rs.getString("EXCEPTION"));
                    assertTrue(rs.next());
                    assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
                    assertNotNull(rs.getString("EXCEPTION"));
                    assertFalse(rs.next());
                    rs.close();
                    stmt.close();
                    return null;
                }
            }.run();
        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testErrorHandlingInCoreEngine_restartAll(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            final WorkflowInstanceDescr<Serializable> wfInstanceDescr = new WorkflowInstanceDescr<Serializable>("org.copperengine.regtest.test.persistent.ExceptionThrowingPersistentUnitTestWorkflow");
            wfInstanceDescr.setId(engine.createUUID());
            engine.run(wfInstanceDescr);
            Thread.sleep(5000);
            // check
            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    ResultSet rs = stmt.executeQuery("select * from COP_WORKFLOW_INSTANCE_ERROR");
                    assertTrue(rs.next());
                    assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
                    assertNotNull(rs.getString("EXCEPTION"));
                    assertFalse(rs.next());
                    rs.close();
                    stmt.close();
                    return null;
                }
            }.run();
            engine.restartAll();
            Thread.sleep(5000);
            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    ResultSet rs = stmt.executeQuery("select * from COP_WORKFLOW_INSTANCE_ERROR");
                    assertTrue(rs.next());
                    assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
                    assertNotNull(rs.getString("EXCEPTION"));
                    assertTrue(rs.next());
                    assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
                    assertNotNull(rs.getString("EXCEPTION"));
                    assertFalse(rs.next());
                    rs.close();
                    stmt.close();
                    return null;
                }
            }.run();
        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testParentChildWorkflow(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testParentChildWorkflow");
        final int NUMB = 20;
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final BackChannelQueue backChannelQueue = context.getBackChannelQueue();
        try {
            assertEquals(EngineState.STARTED, engine.getEngineState());

            for (int i = 0; i < NUMB; i++) {
                engine.run("org.copperengine.regtest.test.persistent.subworkflow.TestParentWorkflow", null);
            }

            for (int i = 0; i < NUMB; i++) {
                WorkflowResult x = backChannelQueue.dequeue(DEQUEUE_TIMEOUT, TimeUnit.SECONDS);
                assertNotNull(x);
                assertNull(x.getResult());
                assertNull(x.getException());
            }
        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testErrorKeepWorkflowInstanceInDB(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testErrorKeepWorkflowInstanceInDB");
        final int NUMB = 20;
        final String DATA = createTestData(50);
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final BackChannelQueue backChannelQueue = context.getBackChannelQueue();
        engine.getDbStorage().setRemoveWhenFinished(false);
        try {
            assertEquals(EngineState.STARTED, engine.getEngineState());

            for (int i = 0; i < NUMB; i++) {
                engine.run(PersistentUnitTestWorkflow_NAME, DATA);
            }

            for (int i = 0; i < NUMB; i++) {
                WorkflowResult x = backChannelQueue.dequeue(DEQUEUE_TIMEOUT, TimeUnit.SECONDS);
                assertNotNull(x);
                assertNotNull(x.getResult());
                assertNotNull(x.getResult().toString().length() == DATA.length());
                assertNull(x.getException());
            }

            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    ResultSet rs = stmt.executeQuery("select count(*) from COP_WORKFLOW_INSTANCE");
                    assertTrue(rs.next());
                    int x = rs.getInt(1);
                    assertEquals(NUMB, x);
                    rs.close();
                    stmt.close();
                    return null;
                }
            }.run();

        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testCompressedAuditTrail(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testCompressedAuditTrail");
        final int NUMB = 20;
        final String DATA = createTestData(50);

        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final BackChannelQueue backChannelQueue = context.getBackChannelQueue();
        context.getAuditTrail().setMessagePostProcessor(new CompressedBase64PostProcessor());
        try {
            assertEquals(EngineState.STARTED, engine.getEngineState());

            for (int i = 0; i < NUMB; i++) {
                engine.run(PersistentUnitTestWorkflow_NAME, DATA);
            }

            for (int i = 0; i < NUMB; i++) {
                WorkflowResult x = backChannelQueue.dequeue(DEQUEUE_TIMEOUT, TimeUnit.SECONDS);
                assertNotNull(x);
                assertNotNull(x.getResult());
                assertNotNull(x.getResult().toString().length() == DATA.length());
                assertNull(x.getException());
            }
            Thread.sleep(1000);

            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    ResultSet rs = stmt.executeQuery("select unique message from (select dbms_lob.substr(long_message, 4000, 1 ) message from COP_AUDIT_TRAIL_EVENT) order by 1 asc");
                    assertTrue(rs.next());
                    // logger.info("\""+new CompressedBase64PostProcessor().deserialize(rs.getString(1))+"\"");
                    // System.out.println(new CompressedBase64PostProcessor().deserialize(rs.getString(1)));
                    assertEquals("finished", new CompressedBase64PostProcessor().deserialize(rs.getString(1)));
                    assertTrue(rs.next());
                    assertEquals("foo successfully called", new CompressedBase64PostProcessor().deserialize(rs.getString(1)));
                    // System.out.println(new CompressedBase64PostProcessor().deserialize(rs.getString(1)));
                    assertFalse(rs.next());
                    rs.close();
                    stmt.close();
                    return null;
                }
            }.run();

        } catch (Exception e) {
            logger.error("testCompressedAuditTrail failed", e);
            throw e;
        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());

    }

    public void testAutoCommit(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testAutoCommit");
        final PersistentEngineTestContext context = createContext(dsType);
        try {
            DataSource ds = context.getDataSource();
            new RetryingTransaction<Void>(ds) {
                @Override
                protected Void execute() throws Exception {
                    assertFalse(getConnection().getAutoCommit());
                    return null;
                }
            };
        } finally {
            closeContext(context);
        }
    }

    private static String createTestMessage(int size) {
        final StringBuilder sb = new StringBuilder(4000);
        for (int i = 0; i < (size / 10); i++) {
            sb.append("0123456789");
        }
        final String msg = sb.toString();
        return msg;
    }

    public void testAuditTrailUncompressed(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testAuditTrailSmallData");
        final PersistentEngineTestContext context = createContext(dsType);
        try {
            BatchingAuditTrail auditTrail = context.getAuditTrail();
            auditTrail.setMessagePostProcessor(new DummyPostProcessor());
            auditTrail.synchLog(1, new Date(), "4711", dsType.name(), "4711", "4711", "4711", null, "TEXT");
            auditTrail.synchLog(1, new Date(), "4711", dsType.name(), "4711", "4711", "4711", createTestMessage(500), "TEXT");
            auditTrail.synchLog(1, new Date(), "4711", dsType.name(), "4711", "4711", "4711", createTestMessage(5000), "TEXT");
            auditTrail.synchLog(1, new Date(), "4711", dsType.name(), "4711", "4711", "4711", createTestMessage(50000), "TEXT");
        } finally {
            closeContext(context);
        }
    }

    public void testErrorHandlingWithWaitHook(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            final WorkflowInstanceDescr<Serializable> wfInstanceDescr = new WorkflowInstanceDescr<Serializable>("org.copperengine.regtest.test.persistent.ErrorWaitHookUnitTestWorkflow");
            wfInstanceDescr.setId(engine.createUUID());
            engine.run(wfInstanceDescr, null);
            Thread.sleep(3500);
            // check
            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    ResultSet rs = stmt.executeQuery("select * from COP_WORKFLOW_INSTANCE_ERROR");
                    assertTrue(rs.next());
                    assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
                    assertNotNull(rs.getString("EXCEPTION"));
                    assertFalse(rs.next());
                    rs.close();
                    stmt.close();
                    return null;
                }
            }.run();
        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testAuditTrailCustomSeqNr(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testAuditTrailCustomSeqNr");
        final PersistentEngineTestContext context = createContext(dsType);
        final BatchingAuditTrail auditTrail = context.getAuditTrail();
        auditTrail.setMessagePostProcessor(new DummyPostProcessor());
        try {
            long seqNr = 1;
            auditTrail.synchLog(new AuditTrailEvent(1, new Date(), "4711", dsType.name(), "4711", "4711", "4711", null, "TEXT", seqNr++));
            auditTrail.synchLog(new AuditTrailEvent(1, new Date(), "4711", dsType.name(), "4711", "4711", "4711", createTestMessage(500), "TEXT", seqNr++));
            auditTrail.synchLog(new AuditTrailEvent(1, new Date(), "4711", dsType.name(), "4711", "4711", "4711", createTestMessage(5000), "TEXT", seqNr++));
            auditTrail.synchLog(new AuditTrailEvent(1, new Date(), "4711", dsType.name(), "4711", "4711", "4711", createTestMessage(50000), "TEXT", seqNr++));
            // check
            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    ResultSet rs = stmt.executeQuery("select seq_id from COP_AUDIT_TRAIL_EVENT order by seq_id");
                    assertTrue(rs.next());
                    assertEquals(1, rs.getLong(1));
                    assertTrue(rs.next());
                    assertEquals(2, rs.getLong(1));
                    assertTrue(rs.next());
                    assertEquals(3, rs.getLong(1));
                    assertTrue(rs.next());
                    assertEquals(4, rs.getLong(1));
                    assertFalse(rs.next());
                    rs.close();
                    stmt.close();
                    return null;
                }
            }.run();
        } finally {
            closeContext(context);
        }
    }

    public void testNotifyWithoutEarlyResponseHandling(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        logger.info("running testNotifyWithoutEarlyResponseHandling");
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    try {
                        Response<?> response = new Response<String>("CID#withEarlyResponse", "TEST", null);
                        engine.notify(response, getConnection());
                        Statement stmt = createStatement(getConnection());
                        ResultSet rs = stmt.executeQuery("select * from COP_RESPONSE");
                        assertTrue(rs.next());
                        assertEquals(response.getCorrelationId(), rs.getString("CORRELATION_ID"));
                        assertFalse(rs.next());
                        getConnection().rollback();

                        response = new Response<String>("CID#withoutEarlyResponse", "TEST", null);
                        response.setEarlyResponseHandling(false);
                        engine.notify(response, getConnection());
                        rs = stmt.executeQuery("select * from COP_RESPONSE");
                        assertFalse(rs.next());
                        rs.close();
                        stmt.close();
                        getConnection().rollback();
                    } catch (Exception e) {
                        logger.error("testNotifyWithoutEarlyResponseHandling failed", e);
                        throw e;
                    }
                    return null;
                }
            }.run();
        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());

    }

    public void testQueryAllActive(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            // just check, that the underlying SQL statements are ok.
            assertEquals(0, engine.queryActiveWorkflowInstances(null, 100).size());
            assertEquals(0, engine.queryActiveWorkflowInstances("foo.john.Doe", 100).size());
        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    private <T> void manualSend(PersistentProcessingEngine engine, String correlationId, T data) {
        Response<T> response = new Response<>(correlationId, data, null);
        response.setResponseId(UUID.randomUUID().toString());
        System.out.println("manualSend: " + response.getResponseId());
        Acknowledge.DefaultAcknowledge ack = new Acknowledge.DefaultAcknowledge();
        engine.notify(response, ack);
        ack.waitForAcknowledge();

    }

    public void testMulipleResponsesForSameCidPersistentTestWorkflow(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            String cid = "testSingleCID";
            try
            {
                engine.run("MulipleResponsesForSameCidPersistentTestWorkflow", cid);
                Thread.sleep(1000); // wait for it to start up
                for (int i = 1; i <= 9; i++)
                {
                    manualSend(engine, cid, "Response#" + i);
                }
                manualSend(engine, cid, "GG");
                Thread.sleep(1000);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testMultipleEngines(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());

        logger.info("running testMultipleEngines");
        final int NUMB = 50;

        final PersistentEngineTestContext contextRed = new PersistentEngineTestContext(dsType, true, "red", true);
        contextRed.startup();

        final PersistentEngineTestContext contextBlue = new PersistentEngineTestContext(dsType, false, "blue", true) {
            @Override
            protected DataHolder createDataHolder() {
                return contextRed.getDataHolder();
            }

            @Override
            protected BackChannelQueue createBackChannelQueue() {
                return contextRed.getBackChannelQueue();
            }
        };
        contextBlue.startup();

        final PersistentScottyEngine engineRed = contextRed.getEngine();
        final PersistentScottyEngine engineBlue = contextBlue.getEngine();
        final BackChannelQueue backChannelQueue = contextRed.getBackChannelQueue();
        try {
            assertEquals(EngineState.STARTED, engineRed.getEngineState());
            assertEquals(EngineState.STARTED, engineBlue.getEngineState());

            for (int i = 0; i < NUMB; i++) {
                ProcessingEngine engine = i % 2 == 0 ? engineRed : engineBlue;
                engine.run(PersistentUnitTestWorkflow_NAME, null);
            }

            int x = 0;
            long startTS = System.currentTimeMillis();
            while (x < NUMB && startTS + 15000 > System.currentTimeMillis()) {
                WorkflowResult wfr = backChannelQueue.poll();

                if (wfr != null) {
                    assertNull(wfr.getResult());
                    assertNull(wfr.getException());
                    x++;
                } else {
                    Thread.sleep(50);
                }
            }
            assertSame("Test failed - Timeout - " + x + " responses so far", x, NUMB);

            Thread.sleep(1000);

            // check for late queue entries
            assertNull(backChannelQueue.poll());

            // check AuditTrail Log
            new RetryingTransaction<Void>(contextRed.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    ResultSet rs = getConnection().createStatement().executeQuery("SELECT count(*) FROM COP_AUDIT_TRAIL_EVENT");
                    rs.next();
                    int count = rs.getInt(1);
                    assertEquals(NUMB * 6, count);
                    rs.close();
                    return null;
                }
            }.run();
        } finally {
            contextRed.close();
            contextBlue.close();
        }
        assertEquals(EngineState.STOPPED, engineRed.getEngineState());
        assertEquals(EngineState.STOPPED, engineBlue.getEngineState());
        assertEquals(0, engineRed.getNumberOfWorkflowInstances());
        assertEquals(0, engineBlue.getNumberOfWorkflowInstances());
    }
        
    
    public void testJmxQueryWorkflowInstances(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final int NUMB_OF_WFI = Runtime.getRuntime().availableProcessors();
        try {
            final WorkflowInstanceFilter EMPTY_FILTER = new WorkflowInstanceFilter();
            WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
            filter.setWorkflowClassname(JmxTestWF_NAME);
            assertEquals(0, engine.queryWorkflowInstances(filter).size());

            context.jmxTestAdapter.get().blockFoo();
            for (int i=0; i<NUMB_OF_WFI; i++) {
                engine.run(JmxTestWF_NAME, "Foo");
            }
            Thread.sleep(200); // wait for it to start up

            logger.info("query RUNNING...");
            filter.setStates(Arrays.asList(ProcessingState.RUNNING.name()));
            assertEqualsX(engine, NUMB_OF_WFI, filter);
            for (WorkflowInfo w : engine.queryWorkflowInstances(filter)) {
                assertNotNull(w.getDataAsString());
                assertNotNull(w.getCreationTS());
                assertNull(w.getErrorData());
                assertNotNull(w.getLastModTS());
                assertNull(w.getLastWaitStackTrace());
                assertNotNull(w.getProcessorPoolId());
                assertEquals(ProcessingState.RUNNING.name(), w.getState());
                assertNull(w.getTimeout());
            }

            assertEquals(NUMB_OF_WFI, engine.queryWorkflowInstances(EMPTY_FILTER).size());
            for (WorkflowInfo w : engine.queryWorkflowInstances(EMPTY_FILTER)) {
                assertNotNull(w.getDataAsString());
                assertNotNull(w.getCreationTS());
                assertNull(w.getErrorData());
                assertNotNull(w.getLastModTS());
                assertNull(w.getLastWaitStackTrace());
                assertNotNull(w.getProcessorPoolId());
                assertEquals(ProcessingState.RUNNING.name(), w.getState());
                assertNull(w.getTimeout());
            }

            context.jmxTestAdapter.get().unblockFoo();

            filter.setStates(Arrays.asList(ProcessingState.WAITING.name()));
            assertEqualsX(engine, NUMB_OF_WFI, filter);
            assertEqualsX(engine, NUMB_OF_WFI, EMPTY_FILTER); // should be the same with a completely empty filter
            for (WorkflowInfo w : engine.queryWorkflowInstances(EMPTY_FILTER)) {
                assertNotNull(w.getDataAsString());
                assertNotNull(w.getCreationTS());
                assertNull(w.getErrorData());
                assertNotNull(w.getLastModTS());
                assertNotNull(w.getLastWaitStackTrace());
                assertNotNull(w.getProcessorPoolId());
                assertEquals(ProcessingState.WAITING.name(), w.getState());
                assertNotNull(w.getTimeout());
            }

            context.jmxTestAdapter.get().blockFoo();
            context.jmxTestAdapter.get().createResponses();

            filter.setStates(Arrays.asList(ProcessingState.RUNNING.name()));
            assertEqualsX(engine, NUMB_OF_WFI, filter);
            for (WorkflowInfo w : engine.queryWorkflowInstances(filter)) {
                assertNotNull(w.getDataAsString());
                assertNotNull(w.getCreationTS());
                assertNull(w.getErrorData());
                assertNotNull(w.getLastModTS());
                assertNotNull(w.getLastWaitStackTrace());
                assertNotNull(w.getProcessorPoolId());
                assertEquals(ProcessingState.RUNNING.name(), w.getState());
            }

            assertEquals(NUMB_OF_WFI, engine.queryWorkflowInstances(EMPTY_FILTER).size());
            for (WorkflowInfo w : engine.queryWorkflowInstances(EMPTY_FILTER)) {
                assertNotNull(w.getDataAsString());
                assertNotNull(w.getCreationTS());
                assertNull(w.getErrorData());
                assertNotNull(w.getLastModTS());
                assertNotNull(w.getLastWaitStackTrace());
                assertNotNull(w.getProcessorPoolId());
                assertEquals(ProcessingState.RUNNING.name(), w.getState());
                assertNull(w.getTimeout());
            }

            context.jmxTestAdapter.get().unblockFoo();

            filter.setStates(Arrays.asList(ProcessingState.WAITING.name()));
            assertEqualsX(engine, NUMB_OF_WFI, filter);
            assertEqualsX(engine, NUMB_OF_WFI, EMPTY_FILTER); // should be the same with a completely empty filter
            for (WorkflowInfo w : engine.queryWorkflowInstances(EMPTY_FILTER)) {
                assertNotNull(w.getDataAsString());
                assertNotNull(w.getCreationTS());
                assertNull(w.getErrorData());
                assertNotNull(w.getLastModTS());
                assertNotNull(w.getLastWaitStackTrace());
                assertNotNull(w.getProcessorPoolId());
                assertEquals(ProcessingState.WAITING.name(), w.getState());
                assertNotNull(w.getTimeout());
            }

            context.jmxTestAdapter.get().createResponses();


            filter.setStates(null);
            assertEqualsX(engine, 0, filter);



        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testJmxQueryWithOffsetWorkflowInstances(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final int NUMB_OF_WFI = Runtime.getRuntime().availableProcessors();
        try {
            final WorkflowInstanceFilter EMPTY_FILTER = new WorkflowInstanceFilter();
            WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
            filter.setWorkflowClassname(JmxTestWF_NAME);
            assertEquals(0, engine.queryWorkflowInstances(filter).size());

            context.jmxTestAdapter.get().blockFoo();
            for (int i=0; i<NUMB_OF_WFI; i++) {
                engine.run(JmxTestWF_NAME, "Foo");
            }
            Thread.sleep(200); // wait for it to start up

            logger.info("query RUNNING...");
            filter.setStates(Arrays.asList(ProcessingState.RUNNING.name()));
            assertEqualsX(engine, NUMB_OF_WFI, filter);
            for (WorkflowInfo w : engine.queryWorkflowInstances(filter)) {
                assertNotNull(w.getDataAsString());
                assertNotNull(w.getCreationTS());
                assertNull(w.getErrorData());
                assertNotNull(w.getLastModTS());
                assertNull(w.getLastWaitStackTrace());
                assertNotNull(w.getProcessorPoolId());
                assertEquals(ProcessingState.RUNNING.name(), w.getState());
                assertNull(w.getTimeout());
            }

            filter.setOffset(1);
            assertEqualsX(engine, NUMB_OF_WFI - 1, filter);
            filter.setOffset(NUMB_OF_WFI);
            assertEqualsX(engine,  0, filter);

            context.jmxTestAdapter.get().createResponses();

            filter.setStates(null);
            assertEqualsX(engine, 0, filter);


        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testJmxCountWorkflowInstances(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        final int NUMB_OF_WFI = Runtime.getRuntime().availableProcessors();
        try {
            final WorkflowInstanceFilter EMPTY_FILTER = new WorkflowInstanceFilter();
            WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
            filter.setWorkflowClassname(JmxTestWF_NAME);
            assertEquals(0, engine.countWorkflowInstances(filter));

            context.jmxTestAdapter.get().blockFoo();
            for (int i=0; i<NUMB_OF_WFI; i++) {
                engine.run(JmxTestWF_NAME, "Foo");
            }
            Thread.sleep(200); // wait for it to start up

            logger.info("query RUNNING...");
            filter.setStates(Arrays.asList(ProcessingState.RUNNING.name()));
            assertEqualsCountX(engine, NUMB_OF_WFI, filter);

            assertEquals(NUMB_OF_WFI, engine.countWorkflowInstances(EMPTY_FILTER));

            context.jmxTestAdapter.get().unblockFoo();

            filter.setStates(Arrays.asList(ProcessingState.WAITING.name()));
            assertEqualsCountX(engine, NUMB_OF_WFI, filter);
            assertEqualsCountX(engine, NUMB_OF_WFI, EMPTY_FILTER); // should be the same with a completely empty filter

            context.jmxTestAdapter.get().blockFoo();
            context.jmxTestAdapter.get().createResponses();

            filter.setStates(Arrays.asList(ProcessingState.RUNNING.name()));
            assertEqualsCountX(engine, NUMB_OF_WFI, filter);

            assertEquals(NUMB_OF_WFI, engine.queryWorkflowInstances(EMPTY_FILTER).size());

            context.jmxTestAdapter.get().unblockFoo();

            filter.setStates(Arrays.asList(ProcessingState.WAITING.name()));
            assertEqualsCountX(engine, NUMB_OF_WFI, filter);
            assertEqualsCountX(engine, NUMB_OF_WFI, EMPTY_FILTER); // should be the same with a completely empty filter

            context.jmxTestAdapter.get().createResponses();

            filter.setStates(null);
            assertEqualsCountX(engine, 0, filter);

        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testJmxRaisingExceptionQuery(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {

            WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
            filter.setStates(Arrays.asList(ProcessingState.RUNNING.name(), ProcessingState.ERROR.name()));
            engine.queryWorkflowInstances(filter);

        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testJmxRaisingExceptionCount(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
            filter.setStates(Arrays.asList(ProcessingState.RUNNING.name(), ProcessingState.ERROR.name()));
            engine.countWorkflowInstances(filter);
        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    private void assertEqualsX(final PersistentScottyEngine engine, final int NUMB_OF_WFI, WorkflowInstanceFilter filter) throws InterruptedException {
        for (int i=0; i<50; i++) {
            Thread.sleep(100);
            if (engine.queryWorkflowInstances(filter).size() == NUMB_OF_WFI)
                break;
            }
        assertEquals(NUMB_OF_WFI, engine.queryWorkflowInstances(filter).size());
    }

    private void assertEqualsCountX(final PersistentScottyEngine engine, final int NUMB_OF_WFI, WorkflowInstanceFilter filter) throws InterruptedException {
        for (int i=0; i < 50; i++) {
            Thread.sleep(100);
            if (engine.countWorkflowInstances(filter) == NUMB_OF_WFI)
                break;
        }
        assertEquals(NUMB_OF_WFI, engine.countWorkflowInstances(filter));
    }

    public void testJmxQueryWorkflowInstancesERROR(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            final int NUMB_OF_WFI = 4;
            final WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
            assertEquals(0, engine.queryWorkflowInstances(filter).size());

            for (int i=0; i<NUMB_OF_WFI; i++) {
                engine.run(JmxTestWF_NAME, "ERROR");
            }
            Thread.sleep(200); // wait for it to start up / bring workflows to error state

            logger.info("query RUNNING...");
            filter.setStates(Arrays.asList(ProcessingState.ERROR.name()));
            assertEqualsX(engine, NUMB_OF_WFI, filter);
            for (WorkflowInfo w : engine.queryWorkflowInstances(filter)) {
                assertNotNull(w.getDataAsString());
                assertNotNull(w.getCreationTS());
                assertNotNull(w.getErrorData());
                assertNotNull(w.getLastModTS());
                assertNull(w.getLastWaitStackTrace());
                assertNotNull(w.getProcessorPoolId());
                assertEquals(ProcessingState.ERROR.name(), w.getState());
                assertNull(w.getTimeout());
                assertTrue(w.getErrorData().getExceptionStackTrace().contains("Test!!!"));
            }

            filter.setOffset(1);
            assertEqualsX(engine, NUMB_OF_WFI - 1, filter);
            filter.setOffset(NUMB_OF_WFI);
            assertEqualsX(engine,  0, filter);
        } 
        finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testJmxQueryWithOffsetWorkflowInstancesERROR(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            final int NUMB_OF_WFI = 4;
            final WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
            assertEquals(0, engine.queryWorkflowInstances(filter).size());

            for (int i=0; i<NUMB_OF_WFI; i++) {
                engine.run(JmxTestWF_NAME, "ERROR");
            }
            Thread.sleep(200); // wait for it to start up / bring workflows to error state

            logger.info("query RUNNING...");
            filter.setStates(Arrays.asList(ProcessingState.ERROR.name()));
            assertEqualsX(engine, NUMB_OF_WFI, filter);
            for (WorkflowInfo w : engine.queryWorkflowInstances(filter)) {
                assertNotNull(w.getDataAsString());
                assertNotNull(w.getCreationTS());
                assertNotNull(w.getErrorData());
                assertNotNull(w.getLastModTS());
                assertNull(w.getLastWaitStackTrace());
                assertNotNull(w.getProcessorPoolId());
                assertEquals(ProcessingState.ERROR.name(), w.getState());
                assertNull(w.getTimeout());
                assertTrue(w.getErrorData().getExceptionStackTrace().contains("Test!!!"));
            }
        }
        finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testJmxCountWorkflowInstancesERROR(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            final int NUMB_OF_WFI = 4;
            final WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
            assertEquals(0, engine.queryWorkflowInstances(filter).size());

            for (int i=0; i<NUMB_OF_WFI; i++) {
                engine.run(JmxTestWF_NAME, "ERROR");
            }
            Thread.sleep(200); // wait for it to start up / bring workflows to error state

            logger.info("query RUNNING...");
            filter.setStates(Arrays.asList(ProcessingState.ERROR.name()));
            assertEqualsCountX(engine, NUMB_OF_WFI, filter);
        }
        finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

    public void testJmxCountWorkflowInstancesWaiting(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            final int NUMB_OF_WFI = 4;
            final WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
            assertEquals(0, engine.queryWorkflowInstances(filter).size());

            for (int i=0; i<NUMB_OF_WFI; i++) {
                engine.run(WaitForEverTestWF_NAME, "ERROR");
            }
            Thread.sleep(200); // wait for it to start up / bring workflows to error state

            logger.info("query RUNNING...");
            filter.setStates(Arrays.asList(ProcessingState.WAITING.name()));
            assertEqualsCountX(engine, NUMB_OF_WFI, filter);
        }
        finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }

//    public void testJmxDeleteWorkflowInstancesWaiting(DataSourceType dsType) throws Exception {
//        assumeFalse(skipTests());
//        final PersistentEngineTestContext context = createContext(dsType);
//        final PersistentScottyEngine engine = context.getEngine();
//        try {
//            final int NUMB_OF_WFI = 4;
//            final WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
//            assertEquals(0, engine.queryWorkflowInstances(filter).size());
//
//            for (int i=0; i<NUMB_OF_WFI; i++) {
//                engine.run(WaitForEverTestWF_NAME, "ERROR");
//            }
//            Thread.sleep(200); // wait for it to start up / bring workflows to error state
//
//            logger.info("query RUNNING...");
//            filter.setStates(Arrays.asList(ProcessingState.WAITING.name()));
//            assertEqualsCountX(engine, NUMB_OF_WFI, filter);
//            engine.deleteFiltered(filter);
//            assertEqualsCountX(engine, 0, filter);
//        }
//        finally {
//            closeContext(context);
//        }
//        assertEquals(EngineState.STOPPED, engine.getEngineState());
//        assertEquals(0, engine.getNumberOfWorkflowInstances());
//    }


    public void testDeleteBrokenWorkflowInstance(DataSourceType dsType) throws Exception {
        assumeFalse(skipTests());
        // Start 5 workflows, let one be broken. Delete the broken one, check database correctness.
        final PersistentEngineTestContext context = createContext(dsType);
        final PersistentScottyEngine engine = context.getEngine();
        try {
            final int NUMB_OF_GOOD_CONCURRENT_WFI = 5;
            final WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
            assertEquals(0, engine.queryWorkflowInstances(filter).size());

            final WorkflowInstanceDescr<Integer> brokenWorkflow = new WorkflowInstanceDescr<>(DeleteBrokenTestWF_NAME, 1);
            brokenWorkflow.setId(engine.createUUID());
            engine.run(brokenWorkflow);

            // Wait for engine to launch workflow and then query results until all are done.
            do {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // Retry..
                }
            } while (engine.queryActiveWorkflowInstances(null, 1).size() > 0);

            // Now check database structure reflects broken workflow.
            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    ResultSet rs = stmt.executeQuery("select * from COP_WORKFLOW_INSTANCE_ERROR");
                    assertTrue(rs.next());
                    assertEquals(brokenWorkflow.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
                    assertNotNull(rs.getString("EXCEPTION"));
                    assertFalse(rs.next());
                    rs.close();
                    stmt.close();
                    Statement stmtCount = createStatement(getConnection());
                    ResultSet rsCount = stmtCount.executeQuery("SELECT COUNT(*) AS total FROM COP_WORKFLOW_INSTANCE_ERROR");
                    assertTrue(rsCount.next());
                    assertEquals(1, rsCount.getInt("total"));
                    rsCount.close();
                    stmtCount.close();
                    Statement stmtCountResponses = createStatement(getConnection());
                    ResultSet rsCountResponses = stmtCountResponses.executeQuery("SELECT COUNT(*) AS total FROM COP_RESPONSE");
                    assertTrue(rsCountResponses.next());
                    // Workflow waits for 2 responses, but one is imaginary and not responded to, thus not in response table. One should have a response. So COP_RESPONSE count is 1, COP_WAIT 2  (as waiting for 2).
                    assertEquals(1, rsCountResponses.getInt("total"));
                    rsCountResponses.close();
                    stmtCountResponses.close();
                    Statement stmtCountWait = createStatement(getConnection());
                    ResultSet rsCountWait = stmtCountWait.executeQuery("SELECT COUNT(*) AS total FROM COP_WAIT");
                    assertTrue(rsCountWait.next());
                    assertEquals(2, rsCountWait.getInt("total"));
                    rsCountWait.close();
                    stmtCountWait.close();
                    return null;
                }
            }.run();


            // Run some non-breaking workflows concurrently and delete broken workflow at the same time
            for (int i=0; i<NUMB_OF_GOOD_CONCURRENT_WFI; i++) {
                engine.run(DeleteBrokenTestWF_NAME, 0);
            }
            engine.deleteBroken(brokenWorkflow.getId());

            // And try to delete one which doesn't exist
            boolean caughtException = false;
            try {
                engine.deleteBroken(engine.createUUID());
            } catch (CopperException e) {
                caughtException = true;
                assertTrue(e.getMessage().contains("can't be deleted"));
            } finally {
                assertTrue(caughtException);
            }

            do {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // Retry..
                }
            } while (engine.queryActiveWorkflowInstances(null, 1).size() > 0);

            // Make sure, it is removed accordingly...
            new RetryingTransaction<Void>(context.getDataSource()) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmtCount = createStatement(getConnection());
                    ResultSet rsCount = stmtCount.executeQuery("SELECT COUNT(*) AS total FROM COP_WORKFLOW_INSTANCE_ERROR");
                    assertTrue(rsCount.next());
                    assertEquals(0, rsCount.getInt("total"));
                    rsCount.close();
                    stmtCount.close();
                    Statement stmtCountResponses = createStatement(getConnection());
                    ResultSet rsCountResponses = stmtCountResponses.executeQuery("SELECT COUNT(*) AS total FROM COP_RESPONSE");
                    assertTrue(rsCountResponses.next());
                    assertEquals(0, rsCountResponses.getInt("total"));
                    rsCountResponses.close();
                    stmtCountResponses.close();
                    Statement stmtCountWait = createStatement(getConnection());
                    ResultSet rsCountWait = stmtCountWait.executeQuery("SELECT COUNT(*) AS total FROM COP_WAIT");
                    assertTrue(rsCountWait.next());
                    assertEquals(0, rsCountWait.getInt("total"));
                    rsCountWait.close();
                    stmtCountWait.close();
                    return null;
                }
            }.run();

            // And now, as all workflows finished execution, test if they all wrote their data back to backchannel and just the single one was deleted.
            BackChannelQueue bcQueue = context.getBackChannelQueue();
            int numWFsDequeud = 0;
            try {
                while (bcQueue.dequeue(10, TimeUnit.MILLISECONDS) != null) {
                    numWFsDequeud++;
                }
            } catch (Exception e) {
                // Don't care..
            }
            assertEquals(NUMB_OF_GOOD_CONCURRENT_WFI, numWFsDequeud);


        } finally {
            closeContext(context);
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
        assertEquals(0, engine.getNumberOfWorkflowInstances());
    }
    
}
