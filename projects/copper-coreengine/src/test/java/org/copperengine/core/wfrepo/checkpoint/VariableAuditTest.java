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
package org.copperengine.core.wfrepo.checkpoint;

import java.io.File;
import java.util.Map;

import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.copperengine.core.util.PojoDependencyInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VariableAuditTest {

    public static final String ORDER_WORKFLOW = "test/OrderWorkflow";

    private static class DependencyInjector extends PojoDependencyInjector {
        private DependencyInjector(final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap) {
            register("workflowMap", workflowMap);
        }
    }
    @Test
    void execute() throws Exception {

        // Action with Assumption, but I use Assertions to be sure
        // that test fails if something goes wrong
        TransientEngineFactory factory = new TransientEngineFactory() {
            @Override
            protected File getWorkflowSourceDirectory() {
                return new File("./src/test/workflow");
            }
        };
        var workflowMapCheckpointCollector = new WorkflowMapCheckpointCollector();
        TransientScottyEngine engine = factory.create(workflowMapCheckpointCollector);
        try {
            Assertions.assertEquals("STARTED", engine.getState());
            engine.setDependencyInjector(new DependencyInjector (workflowMapCheckpointCollector.getWorkflowMap()));
            engine.run(ORDER_WORKFLOW.replace('/', '.'), null);
        } finally {
            Thread.sleep(500);
            Assertions.assertEquals(0, engine.getNumberOfWorkflowInstances());
            engine.shutdown();
        }

    }
}
