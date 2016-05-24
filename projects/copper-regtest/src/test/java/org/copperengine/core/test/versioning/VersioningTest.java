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
package org.copperengine.core.test.versioning;

import static org.junit.Assert.assertEquals;

import org.copperengine.core.EngineState;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.WorkflowVersion;
import org.copperengine.core.test.tranzient.TransientEngineTestContext;
import org.copperengine.core.util.BlockingResponseReceiver;
import org.junit.Test;

public class VersioningTest {

    @Test
    public void testFindLatest() throws Exception {
        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();

            WorkflowVersion v = ctx.getRepo().findLatestMajorVersion(VersionTestWorkflowDef.NAME, 9);
            assertEquals(new WorkflowVersion(9, 3, 1), v);

            v = ctx.getRepo().findLatestMinorVersion(VersionTestWorkflowDef.NAME, 9, 1);
            assertEquals(new WorkflowVersion(9, 1, 1), v);
        }
    }

    @Test
    public void testLatest() throws Exception {
        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            final BlockingResponseReceiver<String> brr = new BlockingResponseReceiver<String>();
            final WorkflowInstanceDescr<BlockingResponseReceiver<String>> descr = new WorkflowInstanceDescr<BlockingResponseReceiver<String>>(VersionTestWorkflowDef.NAME);
            descr.setData(brr);

            ctx.getEngine().run(descr);

            brr.wait4response(5000);
            final String workflowClassname = brr.getResponse();

            assertEquals("org.copperengine.core.test.versioning.VersionTestWorkflow_14_5_67", workflowClassname);
        }
    }

    @Test
    public void testVersion() throws Exception {
        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            final BlockingResponseReceiver<String> brr = new BlockingResponseReceiver<String>();
            final WorkflowInstanceDescr<BlockingResponseReceiver<String>> descr = new WorkflowInstanceDescr<BlockingResponseReceiver<String>>(VersionTestWorkflowDef.NAME);
            descr.setVersion(new WorkflowVersion(1, 0, 1));
            descr.setData(brr);

            ctx.getEngine().run(descr);

            brr.wait4response(5000);
            final String workflowClassname = brr.getResponse();

            assertEquals("org.copperengine.core.test.versioning.VersionTestWorkflow_1_0_1", workflowClassname);
        }

    }

}
