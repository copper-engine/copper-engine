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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class can be used to {@link #analyse(String, WorkflowMapCheckpointCollector.Method, Consumer, int, int)}
 * the workflow maps as a result of the {@link WorkflowMapCheckpointCollector}.
 *
 * It is designed to be used for generating a documentation of workflows.
 *
 */
public class WorkflowMapAnalyzer {

    private final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap;

    public WorkflowMapAnalyzer(
            final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap
    ) {
        this.workflowMap = workflowMap;
    }

    public record Consumable(
            String workflowClassName,
            WorkflowMapCheckpointCollector.Method method,
            WorkflowMapCheckpointCollector.Call call,
            int depth
    ) {
    }

    /**
     * Recursive method to call the consumer for each call in the workflow map.
     * @param workflowClassName workflow class name to be analyzed
     * @param method method to be analyzed
     * @param consumer consumer for Consumable objects, that are created for each call
     * @param depth current depth of the recursions
     * @param maxDepth maximum depth of the recursions
     */
    public void analyse(
            final String workflowClassName,
            final WorkflowMapCheckpointCollector.Method method,
            final Consumer<Consumable> consumer,
            final int depth,
            final int maxDepth
    ) {
        getCalls(workflowClassName, method)
                .forEach(call -> {
                            consumer.accept(
                                    new Consumable(workflowClassName, method, call, depth));
                            if (depth < maxDepth) {
                                analyse(
                                        call.ownerWorkflowClassName(),
                                        call.interruptable(),
                                        consumer,
                                        depth + 1,
                                        maxDepth
                                );
                            }
                        }

                );
    }

    private List<WorkflowMapCheckpointCollector.Call> getCalls(
            final String workflowClassName,
            final WorkflowMapCheckpointCollector.Method method
    ) {
        final WorkflowMapCheckpointCollector.Workflow workflow = workflowMap.get(workflowClassName);
        if (workflow == null) {
            return List.of();
        }
        final List<WorkflowMapCheckpointCollector.Call> calls = workflow.methodMap().get(method);
        if (calls != null) {
            return calls;
        }
        return getCalls(workflow.superWorkflowName(), method);
    }

}
