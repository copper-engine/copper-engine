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
package org.copperengine.core.test.tranzient.lang;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.copperengine.core.CopperException;
import org.copperengine.core.EngineState;
import org.copperengine.core.test.backchannel.WorkflowResult;
import org.copperengine.core.test.tranzient.TransientEngineTestContext;
import org.junit.Test;

public class LocalVarTest {

    @Test
    public void testWorkflow() throws Exception {
        doTest("org.copperengine.core.test.tranzient.lang.LocalVarTransientWorkflow1", 15);
    }

    @Test
    public void testWorkflow2() throws Exception {
        doTest("org.copperengine.core.test.tranzient.lang.LocalVarTransientWorkflow2", 5);

    }

    private void doTest(String wfClassname, int expectedResult) throws CopperException, InterruptedException {

        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            ctx.getEngine().run(wfClassname, null);

            WorkflowResult response = ctx.getBackChannelQueue().dequeue(30000, TimeUnit.MILLISECONDS);

            assertEquals(expectedResult, ((Integer) response.getResult()).intValue());
        }
    }
}
