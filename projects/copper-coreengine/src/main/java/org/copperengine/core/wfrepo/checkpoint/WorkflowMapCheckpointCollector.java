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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A `CheckpointCollector` collects workflows and their associated checkpoints
 * and provides them with {@link #getWorkflowMap()}.
 *
 * This class tracks workflows, methods, and calls, enabling organization and immutability of workflow structures.
 * It operates as a finite state machine with distinct states to enforce specific actions during the checkpoint collection process.
 *
 * The jumpNo start with 0 for each method and is incremented for each call.
 *
 * States:
 * - `InitialState`: The starting state where only `startInstrument` is allowed.
 * - `BeforeWorkflowState`: Transition state where workflows are registered and ready for checkpoint addition.
 * - `InWorkflowState`: Active state during which checkpoints are added to workflows.
 * - `FinalState`: Terminal state after the collection process, where no modifications are allowed.
 */
public class WorkflowMapCheckpointCollector implements CheckpointCollector {

    private final Map<String, Workflow> workflowMap = new HashMap<>();
    private int expectedJumpNo = 0;

    private final CheckpointCollector initialState = new InitialState();
    private final CheckpointCollector beforeWorkflowState = new BeforeWorkflowState();
    private final CheckpointCollector inWorkflowState = new InWorkflowState();
    private final CheckpointCollector finalState = new FinalState();

    private CheckpointCollector state = initialState;

    private final Object lock = new Object();

    @Override
    public void startInstrument() {
        CheckpointCollector.super.startInstrument();
        synchronized (lock) {
            state.startInstrument();
        }
    }

    @Override
    public void workflowStart(String workflowClassName, String superWorkflowClassName) {
        CheckpointCollector.super.workflowStart(workflowClassName, superWorkflowClassName);
        synchronized (lock) {
            state.workflowStart(workflowClassName, superWorkflowClassName);
        }
    }

    @Override
    public void add(CheckPoint checkpoint) {
        CheckpointCollector.super.add(checkpoint);
        synchronized (lock) {
            state.add(checkpoint);
        }
    }

    @Override
    public void workflowEnd(String workflowClassName) {
        CheckpointCollector.super.workflowEnd(workflowClassName);
        synchronized (lock) {
            state.workflowEnd(workflowClassName);
        }
    }

    @Override
    public void endInstrument() {
        CheckpointCollector.super.endInstrument();
        synchronized (lock) {
            state.endInstrument();
        }
    }

    public Map<String, Workflow> getWorkflowMap() {
        return Map.copyOf(workflowMap);
    }

    public record Workflow(
            String name,
            String superWorkflowName,
            Map<Method, List<Call>> methodMap
    ) {
        Workflow withMethodMap(
                final Map<Method, List<Call>> methodMap
        ) {
            return new Workflow(name, superWorkflowName, methodMap);
        }
    }

    public record Method(
            String methodName,
            String methodDescriptor
    ) {
        static Method ofCheckPoint(final CheckPoint checkPoint) {
            return new Method(
                    checkPoint.methodName(),
                    checkPoint.methodDescriptor()
            );
        }
    }

    public record Call(
            int jumpNo,
            String ownerWorkflowClassName,
            Method interruptable
    ) {
        static Call ofCheckPoint(final CheckPoint checkPoint) {
            return new Call(
                    checkPoint.jumpNo(),
                    checkPoint.ownerWorkflowClassName(),
                    new Method(
                            checkPoint.interruptableMethodName(),
                            checkPoint.interruptableMethodDescriptor()
                    )
            );
        }
    }

    private static Workflow immutableWorkflow(final Workflow workflow) {
        workflow
                .methodMap()
                .forEach((method, calls) ->
                        workflow
                                .methodMap()
                                .put(method, List.copyOf(calls))
                );
        return workflow.withMethodMap(Map.copyOf(workflow.methodMap()));
    }

    private abstract class IllegalState implements CheckpointCollector {
        @Override
        public void startInstrument() {
            throw new IllegalStateException("startInstrument not allowed");
        }

        @Override
        public void workflowStart(final String workflowClassName, final String superWorkflowClassName) {
            throw new IllegalStateException("workflowStart not allowed");
        }

        @Override
        public void add(final CheckPoint checkpoint) {
            throw new IllegalStateException("add not allowed");
        }

        @Override
        public void workflowEnd(final String workflowClassName) {
            throw new IllegalStateException("workflowEnd not allowed");
        }

        @Override
        public void endInstrument() {
            throw new IllegalStateException("endInstrument not allowed");
        }
    }

    private class InitialState extends IllegalState implements CheckpointCollector {
        @Override
        public void startInstrument() {
            state = beforeWorkflowState;
        }
    }

    private class InWorkflowState extends IllegalState implements CheckpointCollector {
        @Override
        public void add(final CheckPoint checkpoint) {
            final Workflow workflow = workflowMap.get(checkpoint.workflowClassName());
            final int jumpNo = checkpoint.jumpNo();
            if (jumpNo == 0) {
                ArrayList<Call> calls = new ArrayList<>();
                calls.add(Call.ofCheckPoint(checkpoint));
                workflow
                        .methodMap()
                        .put(Method.ofCheckPoint(checkpoint),
                                calls
                        );
                expectedJumpNo = 1;
            } else {
                if (jumpNo != expectedJumpNo) {
                    throw new IllegalStateException("expected entry no " + expectedJumpNo + " but got " + jumpNo);
                }
                List<Call> calls = workflow
                        .methodMap()
                        .get(Method.ofCheckPoint(checkpoint));
                calls.add(Call.ofCheckPoint(checkpoint));
                expectedJumpNo++;
            }
        }

        @Override
        public void workflowEnd(final String workflowClassName) {
            workflowMap
                    .put(
                            workflowClassName,
                            immutableWorkflow(workflowMap.get(workflowClassName))
                    );
            state = beforeWorkflowState;
        }
    }


    private class BeforeWorkflowState extends IllegalState implements CheckpointCollector {
        @Override
        public void workflowStart(final String workflowClassName, final String superWorkflowClassName) {
            workflowMap.put(
                    workflowClassName,
                    new Workflow(
                            workflowClassName,
                            superWorkflowClassName,
                            new HashMap<>()
                    )
            );
            state = inWorkflowState;
        }

        @Override
        public void endInstrument() {
            state = finalState;
        }
    }


    private class FinalState extends IllegalState implements CheckpointCollector {
    }
}
