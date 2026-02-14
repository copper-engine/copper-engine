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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.copperengine.core.EngineState;
import org.copperengine.core.db.utility.RetryingTransaction;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.regtest.test.backchannel.BackChannelQueue;
import org.copperengine.regtest.test.backchannel.WorkflowResult;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class BaseSpringTxnPersistentWorkflowTest extends BasePersistentWorkflowTest {

    protected ConfigurableApplicationContext createContext(String dsContext) {
        String prefix = "src/test/resources/";
        return new FileSystemXmlApplicationContext(new String[] {
                prefix + dsContext,
                prefix + "SpringTxnPersistentWorkflowTest/persistent-engine-unittest-context.xml",
                prefix + "unittest-context.xml" });
    }

    public void testSpringTxnUnitTestWorkflow(String dsContext) throws Exception {
        assumeFalse(skipTests());
        final ConfigurableApplicationContext context = createContext(dsContext);
        cleanDB(context.getBean(DataSource.class));
        final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
        final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
        try {
            engine.startup();
            engine.run("org.copperengine.regtest.test.persistent.springtxn.SpringTxnUnitTestWorkflow", "TestData");
            WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
            assertNotNull(x);
            Commons.assertNullException(x.getException());
            assertNotNull(x.getResult());

            // check
            new RetryingTransaction<Void>(context.getBean(DataSource.class)) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = getConnection().createStatement();
                    ResultSet rs = stmt.executeQuery("select count(*) from COP_AUDIT_TRAIL_EVENT");
                    assertTrue(rs.next());
                    int c = rs.getInt(1);
                    assertEquals(7, c);
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
}
