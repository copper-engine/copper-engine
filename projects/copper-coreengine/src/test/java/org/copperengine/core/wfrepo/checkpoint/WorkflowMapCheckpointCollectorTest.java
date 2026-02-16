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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WorkflowMapCheckpointCollectorTest {

    public static final String ANALYSER_WORKFLOW = "test/AnalyzerWorkflow";

    public static final WorkflowMapCheckpointCollector.Method MAIN_METHOD = new WorkflowMapCheckpointCollector.Method("main", "()V");

    @Test
    void execute() throws Exception {

        // Action with Assumption, but I use Assertions tp be sure
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
            engine.run(ANALYSER_WORKFLOW.replace('/', '.'), null);
        } finally {
            Thread.sleep(500);
            Assertions.assertEquals(0, engine.getNumberOfWorkflowInstances());
            engine.shutdown();
        }

        final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap = workflowMapCheckpointCollector.getWorkflowMap();
        assertImmutable(workflowMap);
        assertCheckpointsMin(workflowMap);
        assertCheckpointsMax(workflowMap);
    }

    private static void assertCheckpointsMin(final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap) {
        StringBuilder stringBuilder = new StringBuilder();
        new WorkflowMapAnalyzer(workflowMap)
                .analyse(
                        ANALYSER_WORKFLOW,
                        MAIN_METHOD,
                        getTextConsumer(stringBuilder),
                        0,
                        Integer.MIN_VALUE
                );

        Assertions.assertEquals("""
                test/AnalyzerWorkflow#main
                    0 test/AnalyzerBaseWorkflow#main
                    1 test/AnalyzerBaseWorkflow#callService
                    2 test/AnalyzerWorkflow#callService
                    3 test/AnalyzerWorkflow#wait
                """, stringBuilder.toString());
    }

    private static void assertCheckpointsMax(final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap) {
        StringBuilder stringBuilder = new StringBuilder();
        new WorkflowMapAnalyzer(workflowMap)
                .analyse(
                        ANALYSER_WORKFLOW,
                        MAIN_METHOD,
                        getTextConsumer(stringBuilder),
                        0,
                        Integer.MAX_VALUE
                );

        Assertions.assertEquals("""
                test/AnalyzerWorkflow#main
                    0 test/AnalyzerBaseWorkflow#main
                    1 test/AnalyzerBaseWorkflow#callService
                        0 test/AnalyzerBaseWorkflow#wait
                    2 test/AnalyzerWorkflow#callService
                        0 test/AnalyzerBaseWorkflow#wait
                    3 test/AnalyzerWorkflow#wait
                """, stringBuilder.toString());
    }

    private void assertImmutable(final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap) {


        final var workflow = new WorkflowMapCheckpointCollector.Workflow(
                "NOT_ALLOWED",
                "NOT_ALLOWED",
                Map.of()
        );
        Assertions
                .assertThrows(
                        UnsupportedOperationException.class,
                        () -> {
                            workflowMap.put(
                                    "NOT_ALLOWED",
                                    workflow
                            );
                        }
                );


        final var newMethod = new WorkflowMapCheckpointCollector.Method(
                "methodName",
                "methodDescriptor"
        );
        final Map<WorkflowMapCheckpointCollector.Method, List<WorkflowMapCheckpointCollector.Call>> methodMap = workflowMap
                .get(ANALYSER_WORKFLOW)
                .methodMap();
        final var calls = methodMap.get(MAIN_METHOD);
        Assertions
                .assertThrows(
                        UnsupportedOperationException.class,
                        () -> methodMap
                                .put(
                                        newMethod,
                                        calls
                                )
                );


        final var call = new WorkflowMapCheckpointCollector.Call(
                7,
                "ownerWorkflowClassName",
                newMethod
        );
        Assertions
                .assertThrows(
                        UnsupportedOperationException.class,
                        () -> calls.add(call)
                );
    }

    private static Consumer<WorkflowMapAnalyzer.Consumable> getTextConsumer(final StringBuilder stringBuilder) {
        return consumeable -> {
            final WorkflowMapCheckpointCollector.Call call = consumeable.call();
            if (consumeable.depth() == 0 && call.jumpNo() == 0) {
                stringBuilder
                        .append(consumeable.workflowClassName()).append('#')
                        .append(consumeable.method().methodName())
                        .append("\n");
            }
            stringBuilder
                    .append("    ".repeat(consumeable.depth() + 1))
                    .append(call.jumpNo()).append(" ")
                    .append(call.ownerWorkflowClassName()).append('#')
                    .append(call.interruptable().methodName())
                    .append("\n");
        };
    }
}
