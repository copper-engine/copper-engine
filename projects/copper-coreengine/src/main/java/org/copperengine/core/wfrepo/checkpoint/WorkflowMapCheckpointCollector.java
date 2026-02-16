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

public class WorkflowMapCheckpointCollector implements CheckpointCollector {

    private final Map<String, Workflow> workflowMap = new HashMap<>();

    @Override
    public void workflowStart(String workflowClassName, String superWorkflowClassName) {
        CheckpointCollector.super.workflowStart(workflowClassName, superWorkflowClassName);
        workflowMap.put(
                workflowClassName,
                new Workflow(
                        workflowClassName,
                        superWorkflowClassName,
                        new HashMap<>()
                )
        );
    }

    @Override
    public void add(CheckPoint checkpoint) {
        CheckpointCollector.super.add(checkpoint);
        final Workflow workflow = workflowMap.get(checkpoint.workflowClassName());
        if (checkpoint.jumpNo() == 0) {
            ArrayList<Call> calls = new ArrayList<>();
            calls.add(Call.ofCheckPoint(checkpoint));
            workflow
                    .methodMap()
                    .put(Method.ofCheckPoint(checkpoint),
                            calls
                    );
        } else {
            List<Call> calls = workflow
                    .methodMap()
                    .get(Method.ofCheckPoint(checkpoint));
            calls.add(Call.ofCheckPoint(checkpoint));
        }
    }

    @Override
    public void workflowEnd(String workflowClassName) {
        workflowMap
                .put(
                        workflowClassName,
                        immutableWorkflow(workflowMap.get(workflowClassName))
                );
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
}
