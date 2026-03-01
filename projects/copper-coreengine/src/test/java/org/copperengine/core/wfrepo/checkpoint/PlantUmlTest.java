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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlantUmlTest {

    public static final String ANALYSER_WORKFLOW = "test/AnalyzerWorkflow";

    public static final WorkflowMapCheckpointCollector.Method MAIN_METHOD = new WorkflowMapCheckpointCollector.Method("main", "()V");

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
            engine.run(ANALYSER_WORKFLOW.replace('/', '.'), null);
        } finally {
            Thread.sleep(500);
            Assertions.assertEquals(0, engine.getNumberOfWorkflowInstances());
            engine.shutdown();
        }

        final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap = workflowMapCheckpointCollector.getWorkflowMap();
        assertPlantUml(workflowMap);
    }

    private static void assertPlantUml(final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        appendMainStart(stringBuilder);
        new WorkflowMapAnalyzer(workflowMap)
                .analyse(
                        ANALYSER_WORKFLOW,
                        MAIN_METHOD,
                        getPlantUmlConsumer(stringBuilder),
                        0,
                        Integer.MAX_VALUE
                );
        appendMainEnd(stringBuilder);

        Assertions.assertEquals(getDiagram(), stringBuilder.toString());
    }

    private static String getDiagram() throws IOException {
        try (final InputStream is = PlantUmlTest.class.getResourceAsStream("/diagram.adoc")) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }

    }

    private static Consumer<WorkflowMapAnalyzer.Consumable> getPlantUmlConsumer(final StringBuilder stringBuilder) {
        final AtomicInteger lastDepth = new AtomicInteger(0);
        return consumeable -> {

            for (; lastDepth.get() > consumeable.depth(); lastDepth.getAndDecrement()) {
                appendPartionEnd(stringBuilder);
            }

            if (consumeable.call().jumpNo() == 0) {
                appendPartitionStart(stringBuilder, consumeable);
                lastDepth.set(consumeable.depth());
            }
            appendCase(stringBuilder, consumeable);
        };
    }

    private static void appendMainStart(final StringBuilder stringBuilder) {
        stringBuilder.append("""
                [plantuml, diagramm-name, png]
                ----
                
                @startuml
                start
                :START;
                """);
    }


    private static void appendPartitionStart(
            final StringBuilder stringBuilder,
            final WorkflowMapAnalyzer.Consumable consumeable
    ) {
        stringBuilder
                .append("""
                        partition %s {
                        switch (entryNo?)
                        case ( none )
                          :Prolog;
                        """.formatted(consumeable.method().methodName()));
    }

    private static void appendCase(
            final StringBuilder stringBuilder,
            final WorkflowMapAnalyzer.Consumable consumeable
    ) {
        stringBuilder
                .append("""
                        case ( %d )
                          :RESUME;
                          :process segment;
                        """.formatted(consumeable.call().jumpNo()));
    }


    private static void appendPartionEnd(final StringBuilder stringBuilder) {
        stringBuilder.append("""
                endswitch
                switch(interrupt?)
                  case(yes)
                    :INTERRUPT;
                      stop
                  case(no)
                    :Epilog;
                endswitch
                :END;
                stop
                }
                """);
    }

    private static void appendMainEnd(final StringBuilder stringBuilder) {
        stringBuilder.append("""
                endswitch
                switch(interrupt?)
                  case(yes)
                    :INTERRUPT;
                      stop
                  case(no)
                    :Epilog;
                endswitch
                :END;
                stop
                }
                """);
    }
}
