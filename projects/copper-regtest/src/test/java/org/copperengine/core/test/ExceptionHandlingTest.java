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
package org.copperengine.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.copperengine.core.EngineState;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.test.tranzient.TransientTestContext;
import org.copperengine.management.model.WorkflowInfo;
import org.junit.Test;

public class ExceptionHandlingTest {

    @Test
    public void testExceptionHandlingTestWF() throws Exception {
        try (TransientTestContext ctx = new TransientTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            String data = "data";

            final WorkflowInstanceDescr<String> descr = new WorkflowInstanceDescr<String>("org.copperengine.core.test.ExceptionHandlingTestWF");
            descr.setId("1234456");
            descr.setData(data);

            ctx.getEngine().run(descr);

            Thread.sleep(1000L);

            WorkflowInfo info = ctx.getEngine().queryWorkflowInstance(descr.getId());

            assertNull(info);
        }
    }

    @Test
    public void testIssueClassCastExceptionWorkflow3() throws Exception {
        try (TransientTestContext ctx = new TransientTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            String data = "data";

            final WorkflowInstanceDescr<String> descr = new WorkflowInstanceDescr<String>("org.copperengine.core.test.IssueClassCastExceptionWorkflow3");
            descr.setId("1234456");
            descr.setData(data);

            ctx.getEngine().run(descr);

            Thread.sleep(1000L);

            WorkflowInfo info = ctx.getEngine().queryWorkflowInstance(descr.getId());

            assertNull(info);
        }
    }

}
