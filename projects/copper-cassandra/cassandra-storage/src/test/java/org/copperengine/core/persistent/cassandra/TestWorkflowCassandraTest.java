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
package org.copperengine.core.persistent.cassandra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.management.model.WorkflowInfo;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class TestWorkflowCassandraTest extends CassandraTest {

    @Test
    public void testParallel() throws Exception {
        Assumptions.assumeTrue(factory != null);
        List<String> cids = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final String cid = factory.getEngine().createUUID();
            final TestData data = new TestData(cid, "foo");
            final WorkflowInstanceDescr<TestData> wfid = new WorkflowInstanceDescr<TestData>("org.copperengine.core.persistent.cassandra.workflows.TestWorkflow", data, cid, 1, null);
            factory.getEngine().run(wfid);
            cids.add(cid);
        }
        for (String cid : cids) {
            Object response = factory.backchannel.get().wait(cid, 10000, TimeUnit.MILLISECONDS);
            Assertions.assertNotNull(response,"no response for workflow instance " + cid);
            Assertions.assertEquals("OK", response);
        }
        Thread.sleep(250);
        WorkflowInstanceFilter filter = new WorkflowInstanceFilter();
        List<WorkflowInfo> result = factory.getEngine().queryWorkflowInstances(filter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSerial() throws Exception {
        Assumptions.assumeTrue(factory != null);
        for (int i = 0; i < 3; i++) {
            final String cid = factory.getEngine().createUUID();
            final TestData data = new TestData(cid, "foo");
            final WorkflowInstanceDescr<TestData> wfid = new WorkflowInstanceDescr<TestData>("org.copperengine.core.persistent.cassandra.workflows.TestWorkflow", data, cid, 1, null);
            factory.getEngine().run(wfid);
            Object response = factory.backchannel.get().wait(cid, 10000, TimeUnit.MILLISECONDS);
            Assertions.assertNotNull(response);
            Assertions.assertEquals("OK", response);
        }
    }

}
