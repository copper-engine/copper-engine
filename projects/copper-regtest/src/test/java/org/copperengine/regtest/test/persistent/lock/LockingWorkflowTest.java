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
package org.copperengine.regtest.test.persistent.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.util.Backchannel;
import org.copperengine.regtest.test.persistent.DataSourceType;
import org.copperengine.regtest.test.persistent.PersistentEngineTestContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockingWorkflowTest {

    private static final Logger logger = LoggerFactory.getLogger(LockingWorkflowTest.class);

    @Test
    public void testDerbyDB() throws Exception {
        testMain(DataSourceType.DerbyDB);
    }

    @Test
    public void testOracle() throws Exception {
        testMain(DataSourceType.Oracle);
    }

    @Test
    public void testMySQL() throws Exception {
        testMain(DataSourceType.MySQL);
    }

    @Test
    public void testH2() throws Exception {
        testMain(DataSourceType.H2);
    }

    @Test
    public void testPostgres() throws Exception {
        testMain(DataSourceType.Postgres);
    }

    private void testMain(DataSourceType dsType) throws Exception {
        logger.info("Testing {}", dsType);
        try (PersistentEngineTestContext ctx = new PersistentEngineTestContext(dsType, true)) {
            if (!ctx.isDbmsAvailable()) {
                logger.warn("DBMS {} not available - test skipped!", dsType);
                return;
            }
            ctx.startup();

            PersistentProcessingEngine engine = ctx.getEngine();
            Backchannel backchannel = ctx.getBackchannel();

            List<WorkflowInstanceDescr<?>> wfid = new ArrayList<WorkflowInstanceDescr<?>>();
            for (int i = 0; i < 10; i++) {
                String wfId = engine.createUUID();
                wfid.add(new WorkflowInstanceDescr<String>("org.copperengine.regtest.test.persistent.lock.LockingWorkflow", "COPPER", wfId, null, null));
            }

            logger.info("Launching workflow instances...");
            engine.runBatch(wfid);
            logger.info("Done launching workflow instances. Now waiting for results...");

            for (WorkflowInstanceDescr<?> x : wfid) {
                Boolean res = (Boolean) backchannel.wait(x.getId(), 30, TimeUnit.SECONDS);
                org.junit.Assert.assertNotNull(res);
                org.junit.Assert.assertTrue(res);
            }

            logger.info("Test finished!");
        }
    }
}
