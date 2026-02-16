package org.copperengine.core.wfrepo.checkpoint;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
