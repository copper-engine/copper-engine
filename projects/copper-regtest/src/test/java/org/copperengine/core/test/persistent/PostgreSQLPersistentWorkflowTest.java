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

import org.junit.Test;

public class PostgreSQLPersistentWorkflowTest extends SpringlessBasePersistentWorkflowTest {

    private static final DataSourceType DS_CONTEXT = DataSourceType.Postgres;

    private static boolean dbmsAvailable = false;

    static {
        if (Boolean.getBoolean(Constants.SKIP_EXTERNAL_DB_TESTS_KEY)) {
            dbmsAvailable = true;
        } else {
            try (PersistentEngineTestContext context = new PersistentEngineTestContext(DS_CONTEXT, false)) {
                dbmsAvailable = context.isDbmsAvailable();
            }
        }
    }

    @Override
    protected boolean skipTests() {
        return Boolean.getBoolean(Constants.SKIP_EXTERNAL_DB_TESTS_KEY) || !dbmsAvailable;
    }

    @Test
    public void testAsynchResponse() throws Exception {
        super.testAsynchResponse(DS_CONTEXT);
    }

    @Test
    public void testAsynchResponseLargeData() throws Exception {
        super.testAsynchResponseLargeData(DS_CONTEXT, 65536);
    }

    @Test
    public void testWithConnection() throws Exception {
        super.testWithConnection(DS_CONTEXT);
    }

    @Test
    public void testWithConnectionBulkInsert() throws Exception {
        super.testWithConnectionBulkInsert(DS_CONTEXT);
    }

    @Test
    public void testTimeouts() throws Exception {
        super.testTimeouts(DS_CONTEXT);
    }

    @Test
    public void testErrorHandlingInCoreEngine() throws Exception {
        super.testErrorHandlingInCoreEngine(DS_CONTEXT);
    }

    @Test
    public void testParentChildWorkflow() throws Exception {
        super.testParentChildWorkflow(DS_CONTEXT);
    }

    @Test
    public void testErrorKeepWorkflowInstanceInDB() throws Exception {
        super.testErrorKeepWorkflowInstanceInDB(DS_CONTEXT);
    }

    @Test
    public void testAutoCommit() throws Exception {
        super.testAutoCommit(DS_CONTEXT);
    }

    @Test
    public void testAuditTrailUncompressed() throws Exception {
        super.testAuditTrailUncompressed(DS_CONTEXT);
    }

    @Test
    public void testErrorHandlingWithWaitHook() throws Exception {
        super.testErrorHandlingWithWaitHook(DS_CONTEXT);
    }

    @Test
    public void testNotifyWithoutEarlyResponseHandling() throws Exception {
        super.testNotifyWithoutEarlyResponseHandling(DS_CONTEXT);
    }

    @Test
    public void testFailOnDuplicateInsert() throws Exception {
        super.testFailOnDuplicateInsert(DS_CONTEXT);
    }

    @Test
    public void testWaitForEver() throws Exception {
        super.testWaitForEver(DS_CONTEXT);
    }

    @Test
    public void testErrorHandlingInCoreEngine_restartAll() throws Exception {
        super.testErrorHandlingInCoreEngine_restartAll(DS_CONTEXT);
    }

    @Test
    public void testQueryAllActive() throws Exception {
        super.testQueryAllActive(DS_CONTEXT);
    }

    @Test
    public void testMulipleResponsesForSameCidPersistentTestWorkflow() throws Exception {
        super.testMulipleResponsesForSameCidPersistentTestWorkflow(DS_CONTEXT);
    }

    @Test
    public void testMultipleEngines() throws Exception {
        super.testMultipleEngines(DS_CONTEXT);
    }
    
    @Test
    public void testJmxQueryWorkflowInstances() throws Exception {
        super.testJmxQueryWorkflowInstances(DS_CONTEXT);
    }

    @Test
    public void testJmxQueryWorkflowInstancesERROR() throws Exception {
        super.testJmxQueryWorkflowInstancesERROR(DS_CONTEXT);
    }
}
