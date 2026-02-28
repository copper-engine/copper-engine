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
 * <p>
 * This class tracks workflows, methods, and calls, enabling organization and immutability of workflow structures.
 * It operates as a finite state machine with distinct states to enforce specific actions during the checkpoint collection process.
 * <p>
 * The jumpNo start with 0 for each method and is incremented for each call.
 * <p>
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
    public void addCheckpointInfo(CheckpointInfo checkpointInfo) {
        CheckpointCollector.super.addCheckpointInfo(checkpointInfo);
        synchronized (lock) {
            state.addCheckpointInfo(checkpointInfo);
        }
    }

    @Override
    public void addVariableInfo(VariableInfo variableInfo) {
        CheckpointCollector.super.addVariableInfo(variableInfo);
        synchronized (lock) {
            state.addVariableInfo(variableInfo);
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
            Map<Method, Info> methodInfoMap
    ) {
        public Workflow withImmutableMethodInfoMap() {
            return new Workflow(name, superWorkflowName, Map.copyOf(methodInfoMap));
        }
    }

    public record Info(
            List<Call> calls,
            List<Variable> variables
    ) {
    }

    public record Variable(
            String name,
            int index
    ) {
    }

    public record Method(
            String methodName,
            String methodDescriptor
    ) {
        static Method ofCheckPoint(final CheckpointInfo checkPointInfo) {
            return new Method(
                    checkPointInfo.methodName(),
                    checkPointInfo.methodDescriptor()
            );
        }
    }

    public record Call(
            int jumpNo,
            String ownerWorkflowClassName,
            Method interruptable
    ) {
        static Call ofCheckPoint(final CheckpointInfo checkPointInfo) {
            return new Call(
                    checkPointInfo.jumpNo(),
                    checkPointInfo.ownerWorkflowClassName(),
                    new Method(
                            checkPointInfo.interruptableMethodName(),
                            checkPointInfo.interruptableMethodDescriptor()
                    )
            );
        }
    }

    private static Workflow immutableWorkflow(final Workflow workflow) {
        final Map<Method, Info> methodInfoMap = workflow.methodInfoMap();
        methodInfoMap
                .forEach((method, info) ->
                        methodInfoMap
                                .put(method, new Info(
                                        List.copyOf(info.calls()),
                                        List.copyOf(info.variables())
                                ))
                );
        return workflow.withImmutableMethodInfoMap();
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
        public void addCheckpointInfo(final CheckpointInfo checkpointInfo) {
            throw new IllegalStateException("add not allowed");
        }

        @Override
        public void addVariableInfo(final VariableInfo variableInfo) {
            throw new IllegalStateException("addVariableInfo not allowed");
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
        public void addCheckpointInfo(final CheckpointInfo checkpointInfo) {
            final Workflow workflow = workflowMap.get(checkpointInfo.workflowClassName());
            final Method method = Method.ofCheckPoint(checkpointInfo);
            final Map<Method, Info> methodInfoMap = workflow.methodInfoMap();
            assureMethodInfo(methodInfoMap, method)
                    .calls()
                    .add(Call.ofCheckPoint(checkpointInfo));

            final int jumpNo = checkpointInfo.jumpNo();
            if (jumpNo == 0) {
                expectedJumpNo = 1;
            } else {
                if (jumpNo != expectedJumpNo) {
                    throw new IllegalStateException("expected entry no " + expectedJumpNo + " but got " + jumpNo);
                }
                expectedJumpNo++;
            }
        }

        @Override
        public void addVariableInfo(final VariableInfo variableInfo) {
            final Workflow workflow = workflowMap.get(variableInfo.methodInfo().workflowClassName());
            final Method method = new Method(
                    variableInfo.methodInfo().methodName(),
                    variableInfo.methodInfo().methodDescriptor()
            );
            final Map<Method, Info> methodInfoMap = workflow.methodInfoMap();

            assureMethodInfo(methodInfoMap, method)
                    .variables()
                    .add(new Variable(variableInfo.name(), variableInfo.index()));
        }

        private Info assureMethodInfo(final Map<Method, Info> methodInfoMap, final Method method) {
            if (!methodInfoMap.containsKey(method)) {
                final Info newInfo = new Info(
                        new ArrayList<>(),
                        new ArrayList<>()
                );
                methodInfoMap
                        .put(
                                method,
                                newInfo
                        );
                return newInfo;
            } else {
                return methodInfoMap.get(method);
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
