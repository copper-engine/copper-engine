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
package org.copperengine.core.test.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.sql.ResultSet;

import org.copperengine.core.EngineState;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.db.utility.RetryingTransaction;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.test.DataHolder;
import org.copperengine.core.test.backchannel.BackChannelQueue;
import org.copperengine.core.test.backchannel.WorkflowResult;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OraclePersistentWorkflowTest extends SpringlessBasePersistentWorkflowTest {

    private static final DataSourceType DS_CONTEXT = DataSourceType.Oracle;
    private static final Logger logger = LoggerFactory.getLogger(OraclePersistentWorkflowTest.class);

    private static boolean dbmsAvailable = false;

    static {
        if (Boolean.getBoolean(Constants.SKIP_EXTERNAL_DB_TESTS_KEY)) {
            dbmsAvailable = true;
        } else {
            try (TestContext context = new TestContext(DS_CONTEXT, false)) {
                dbmsAvailable = context.isDbmsAvailable();
            }
        }
    }

    @Override
    protected boolean skipTests() {
        return Boolean.getBoolean(Constants.SKIP_EXTERNAL_DB_TESTS_KEY);
    }

    @Test
    public void testAsynchResponse() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testAsynchResponse(DS_CONTEXT);
    }

    @Test
    public void testAsynchResponseLargeData() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testAsynchResponseLargeData(DS_CONTEXT, 65536);
    }

    @Test
    public void testWithConnection() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testWithConnection(DS_CONTEXT);
    }

    @Test
    public void testWithConnectionBulkInsert() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testWithConnectionBulkInsert(DS_CONTEXT);
    }

    @Test
    public void testTimeouts() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testTimeouts(DS_CONTEXT);
    }

    @Test
    public void testMultipleEngines() throws Exception {
        assumeFalse(skipTests());

        assertTrue("DBMS not available", dbmsAvailable);

        logger.info("running testMultipleEngines");
        final int NUMB = 50;

        final TestContext contextRed = new TestContext(DS_CONTEXT, true, "red", true);
        contextRed.startup();

        final TestContext contextBlue = new TestContext(DS_CONTEXT, false, "blue", true) {
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

    @Test
    public void testErrorHandlingInCoreEngine() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testErrorHandlingInCoreEngine(DS_CONTEXT);
    }

    @Test
    public void testParentChildWorkflow() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testParentChildWorkflow(DS_CONTEXT);
    }

    @Test
    public void testErrorKeepWorkflowInstanceInDB() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testErrorKeepWorkflowInstanceInDB(DS_CONTEXT);
    }

    @Test
    public void testErrorHandlingInCoreEngine_restartAll() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testErrorHandlingInCoreEngine_restartAll(DS_CONTEXT);
    }

    @Test
    public void testCompressedAuditTrail() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testCompressedAuditTrail(DS_CONTEXT);
    }

    @Test
    public void testAutoCommit() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testAutoCommit(DS_CONTEXT);
    }

    @Test
    public void testAuditTrailUncompressed() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testAuditTrailUncompressed(DS_CONTEXT);
    }

    @Test
    public void testErrorHandlingWithWaitHook() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testErrorHandlingWithWaitHook(DS_CONTEXT);
    }

    @Test
    public void testAuditTrailCustomSeqNr() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testAuditTrailCustomSeqNr(DS_CONTEXT);
    }

    @Test
    public void testNotifyWithoutEarlyResponseHandling() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testNotifyWithoutEarlyResponseHandling(DS_CONTEXT);
    }

    @Test
    public void testFailOnDuplicateInsert() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testFailOnDuplicateInsert(DS_CONTEXT);
    }

    @Test
    public void testWaitForEver() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testWaitForEver(DS_CONTEXT);
    }

    @Test
    public void testQueryAllActive() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testQueryAllActive(DS_CONTEXT);
    }

    @Test
    public void testMulipleResponsesForSameCidPersistentTestWorkflow() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testMulipleResponsesForSameCidPersistentTestWorkflow(DS_CONTEXT);
    }

}
