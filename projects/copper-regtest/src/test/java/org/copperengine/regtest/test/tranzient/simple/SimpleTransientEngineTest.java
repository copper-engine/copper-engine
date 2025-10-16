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

import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.EngineState;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.regtest.test.backchannel.WorkflowResult;
import org.copperengine.regtest.test.tranzient.TransientEngineTestContext;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleTransientEngineTest {

    @Test
    public void testWorkflow() throws Exception {
        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            ctx.getEngine().run("org.copperengine.regtest.test.tranzient.simple.SimpleTransientWorkflow", null);
            WorkflowResult response = ctx.getBackChannelQueue().dequeue(5000, TimeUnit.MILLISECONDS);
            assertEquals(Integer.valueOf(10), response.getResult());
        }

    }

    @Test
    public void testDuplicateIdException() throws Exception {
        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            ctx.getEngine().run(new WorkflowInstanceDescr<String>("org.copperengine.regtest.test.tranzient.simple.VerySimpleTransientWorkflow", "data", "singleton", null, null));

            assertThrows(DuplicateIdException.class,
                         () -> ctx.getEngine().run(new WorkflowInstanceDescr<String>("org.copperengine.regtest.test.tranzient.simple.VerySimpleTransientWorkflow", "data", "singleton", null, null)));

        }
    }
}
