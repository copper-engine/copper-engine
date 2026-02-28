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
import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CheckpointCollectorTest {

    public static final String TEST_AUDITOR_WORKFLOW = "test/AuditorWorkflow";
    public static final String BASE_WORKFLOW = "org/copperengine/core/Workflow";

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
        var workflowRepositoryCheckpointCollectorMock = Mockito.mock(CheckpointCollector.class);
        TransientScottyEngine engine = factory.create(workflowRepositoryCheckpointCollectorMock);
        try {
            Assertions.assertEquals("STARTED", engine.getState());
            engine.run("test.AuditorWorkflow", null);
        } finally {
            LockSupport.parkNanos(Duration.ofMillis(500).toNanos());
            Assertions.assertEquals(0, engine.getNumberOfWorkflowInstances());
            engine.shutdown();
        }


        verifyCheckpointCollectorCalls(workflowRepositoryCheckpointCollectorMock);
    }

    private static void verifyCheckpointCollectorCalls(final CheckpointCollector workflowRepositoryCheckpointCollectorMock) {
        Mockito.verify(workflowRepositoryCheckpointCollectorMock).startInstrument();
        Mockito.verify(workflowRepositoryCheckpointCollectorMock).workflowStart(TEST_AUDITOR_WORKFLOW, BASE_WORKFLOW);
        Mockito.verify(workflowRepositoryCheckpointCollectorMock).workflowEnd(TEST_AUDITOR_WORKFLOW);
        Mockito.verify(
                        workflowRepositoryCheckpointCollectorMock,
                        Mockito
                                .times(12)
                )
                .addCheckpointInfo(
                        Mockito.argThat(
                                checkpointInfo ->
                                        TEST_AUDITOR_WORKFLOW
                                                .equals(checkpointInfo.methodDefinition().workflowClassName())
                        )
                );
        Mockito.verify(workflowRepositoryCheckpointCollectorMock).endInstrument();
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "main",
                                        "()V"
                                ),
                                0,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "wait",
                                        "(Lorg/copperengine/core/WaitMode;I[Ljava/lang/String;)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addVariableInfo(
                        new CheckpointCollector.VariableInfo(
                                "this",
                                0,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "subWorkflow",
                                        "(IJ)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addVariableInfo(
                        new CheckpointCollector.VariableInfo(
                                "i1",
                                1,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "subWorkflow",
                                        "(IJ)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addVariableInfo(
                        new CheckpointCollector.VariableInfo(
                                "l",
                                2,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "subWorkflow",
                                        "(IJ)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "main",
                                        "()V"
                                ),
                                1,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "wait",
                                        "(Lorg/copperengine/core/WaitMode;I[Lorg/copperengine/core/Callback;)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "main",
                                        "()V"
                                ),
                                2,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "wait",
                                        "(Lorg/copperengine/core/WaitMode;JLjava/util/concurrent/TimeUnit;[Ljava/lang/String;)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "main",
                                        "()V"
                                ),
                                3,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "wait",
                                        "(Lorg/copperengine/core/WaitMode;JLjava/util/concurrent/TimeUnit;[Lorg/copperengine/core/Callback;)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "main",
                                        "()V"
                                ),
                                4,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "waitForAll",
                                        "([Ljava/lang/String;)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "main",
                                        "()V")
                                ,
                                5,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "waitForAll",
                                        "([Lorg/copperengine/core/Callback;)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "main",
                                        "()V"
                                ),
                                6,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "subWorkflow",
                                        "(IJ)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "main",
                                        "()V")
                                ,
                                7,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "subWorkflow",
                                        "(IJ)V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "main",
                                        "()V"
                                ),
                                8,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "interruptableMethod",
                                        "()V"

                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "subWorkflow",
                                        "(IJ)V"
                                ),
                                0,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "resubmit",
                                        "()V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "subWorkflow",
                                        "(IJ)V"
                                ),
                                1,
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "savepoint",
                                        "()V"
                                )
                        )
                );
        Mockito
                .verify(workflowRepositoryCheckpointCollectorMock)
                .addCheckpointInfo(
                        new CheckpointCollector.CheckpointInfo(
                                new CheckpointCollector.MethodInfo(
                                        TEST_AUDITOR_WORKFLOW,
                                        "subWorkflow",
                                        "(IJ)V"
                                ),
                                2,
                                new CheckpointCollector.MethodInfo(
                                        BASE_WORKFLOW,
                                        "savepoint",
                                        "()V"
                                )
                        )
                );
    }
}
