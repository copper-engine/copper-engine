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
package org.copperengine.regtest.test.tranzient.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.copperengine.core.EngineState;
import org.copperengine.regtest.test.backchannel.WorkflowResult;
import org.copperengine.regtest.test.tranzient.TransientEngineTestContext;
import org.junit.jupiter.api.Test;

public class SubWorkflowTransientEngineTest {

    @Test
    public void testWorkflow() throws Exception {
        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());
            ctx.getEngine().run("org.copperengine.regtest.test.tranzient.simple.SimpleTestParentWorkflow", "testData");
            WorkflowResult r = ctx.getBackChannelQueue().dequeue(2000, TimeUnit.MILLISECONDS);
            assertNotNull(r);
        }
    }

}
