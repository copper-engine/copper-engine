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
package test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.copperengine.core.Auditor;
import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.wfrepo.checkpoint.WorkflowMapCheckpointCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OrderBaseWorkflow extends Workflow<Void> implements Auditor {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(OrderBaseWorkflow.class);
    private Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap;


    @AutoWire
    public void setWorkflowMap(final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap) {
        this.workflowMap = workflowMap;
    }

    @Override
    public void interrupt() {
        auditVariables(jumpNos(), workflowMap);
    }

    private void auditVariables(
            final List<Integer> jumpNos,
            final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap
    ) {
        final var infoBuilder = new StringBuilder().append("\n");

        getStackInfo(jumpNos, workflowMap, null, -1, new Consumer<Variable>() {
            @Override
            public void accept(final Variable variable) {
                if (!"this".equals(variable.name())) {
                    infoBuilder
                            .append("  ".repeat(variable.depthIndex()))
                            .append(variable.name())
                            .append("=").append(variable.value())
                            .append("\n");
                }
            }
        });
        log.info("Workflow stack: {}", infoBuilder.toString());
    }

    record Variable(int depthIndex, String name, String value) {

    }

    private WorkflowMapCheckpointCollector.Info getMainInfo(
            final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap
    ) {
        return workflowMap
                .get(this.getClass().getName().replace('.', '/'))
                .methodInfoMap()
                .get(
                        new WorkflowMapCheckpointCollector.Method(
                                "main",
                                "()V"
                        )
                );
    }

    private void getStackInfo(
            final List<Integer> jumpNos,
            final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap,
            final WorkflowMapCheckpointCollector.Info info,
            final int depthIndex,
            final Consumer<Variable> consumer) {
        final WorkflowMapCheckpointCollector.Info
                currentInfo = (info == null
                ? getMainInfo(workflowMap)
                : getNextInfo(jumpNos, workflowMap, info, depthIndex));
        logJump(depthIndex + 1, currentInfo, consumer);
        if (jumpNos.size() > depthIndex + 2) {
            getStackInfo(jumpNos, workflowMap, currentInfo, depthIndex + 1, consumer);
        }
    }

    private static WorkflowMapCheckpointCollector.Info getNextInfo(final List<Integer> jumpNos, final Map<String, WorkflowMapCheckpointCollector.Workflow> workflowMap, final WorkflowMapCheckpointCollector.Info info, final int depthIndex) {
        WorkflowMapCheckpointCollector.Info currentInfo;
        final WorkflowMapCheckpointCollector.Call call = info
                .calls()
                .get(jumpNos.get(depthIndex));

        WorkflowMapCheckpointCollector.Workflow workflow =
                workflowMap.get(
                        call.ownerWorkflowClassName()
                );

        do {
            currentInfo = workflow
                    .methodInfoMap()
                    .get(call.interruptable());
            if (workflow.superWorkflowName() != null) {
                workflow =
                        workflowMap.get(workflow.superWorkflowName());
            }
        } while (currentInfo == null);
        return currentInfo;
    }

    private void logJump(
            final int depthIndex,
            final WorkflowMapCheckpointCollector.Info info,
            final Consumer<Variable> consumer) {
        info
                .variables()
                .forEach(variable -> {
                    if (__stack.get(depthIndex).locals.length > variable.index()) {
                        consumer.accept(new Variable(depthIndex, variable.name(), __stack.get(depthIndex).locals[variable.index()] + ""));
                    }
                });
    }

    protected void callWithRetry(final String correlationId) throws Interrupt {
        final var state = "retry";
        for (int retryCount = 1; retryCount < 3; retryCount++) {
            wait(WaitMode.FIRST, 100, correlationId);
        }
    }

    private List<Integer> jumpNos() {
        return get__stack()
                .stream()
                .map(e -> e.jumpNo)
                .toList();
    }
}
