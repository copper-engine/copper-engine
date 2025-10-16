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
package org.copperengine.regtest.test.tranzient.classhierarchy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.copperengine.core.EngineState;
import org.copperengine.core.util.BlockingResponseReceiver;
import org.copperengine.regtest.test.tranzient.TransientEngineTestContext;
import org.junit.jupiter.api.Test;

public class DerivedTransientEngineTest {

    @Test
    public void testWorkflow() throws Exception {
        TransientEngineTestContext ctx = new TransientEngineTestContext();
        try {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            final BlockingResponseReceiver<Integer> brr = new BlockingResponseReceiver<Integer>();
            ctx.getEngine().run("org.copperengine.regtest.test.tranzient.classhierarchy.DerivedDerived", brr);
            brr.wait4response(30000);
            assertEquals(10, brr.getResponse().intValue());
        } finally {
            ctx.close();
        }
        assertEquals(EngineState.STOPPED, ctx.getEngine().getEngineState());

    }
}
