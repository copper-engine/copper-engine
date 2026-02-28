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

import org.slf4j.LoggerFactory;

/**
 * A CheckpointCollector is called while loading and instrumenting COPPER workflows
 * to collect instrumentation and workflow start, workflow end and all calls of interruptable methods.
 *
 * <p>
 * The first and last call are {@link #startInstrument()} and {@link #endInstrument()}.
 * </p><p>
 * For each workflow the first and last call
 * are {@link #workflowStart(String, String)} and {@link #workflowEnd(String)}.
 * For every call of an interruptable methods {@link #addCheckpointInfo(CheckpointInfo)} is called.
 * </p><p>
 * Implementations of the CheckpointCollector get this information of workflows
 * so that they can create meaningful documentation as BPMN, Activity Diagrams, etc.
 * </p>
 */
public interface CheckpointCollector {

    /**
     * Called at the beginning of instrumentation.
     */
    default void startInstrument() {
        LoggerFactory.getLogger(this.getClass()).info("startInstrument");
    }

    /**
     * @param workflowClassName      The fully qualified name of the instrumented workflow class.
     *                               No .class suffix and using '/' as path separator.
     * @param superWorkflowClassName The fully qualified name of the extended workflow class.
     *                               No .class suffix and using '/' as path separator.
     */
    default void workflowStart(final String workflowClassName, final String superWorkflowClassName) {
        LoggerFactory.getLogger(this.getClass()).info("workflowStart[{} extends {}]", workflowClassName, superWorkflowClassName);
    }

    /**
     * Called once for each checkpointInfo.
     *
     * @param checkpointInfo Description of the checkpointInfo.
     */
    default void addCheckpointInfo(
            final CheckpointInfo checkpointInfo
    ) {
        LoggerFactory.getLogger(this.getClass()).info("addCheckpoint {}", checkpointInfo);
    }

    /**
     * Called once for each variable in instrumented methods.
     *
     * @param variableInfo Information where the variable is used.
     */
    default void addVariableInfo(VariableInfo variableInfo) {
        LoggerFactory.getLogger(this.getClass()).info("addVariableInfo {}", variableInfo);
    }

    /**
     * @param workflowClassName The fully qualified name of the instrumented workflow class.
     *                          No .class suffix and using '/' as path separator.
     */
    default void workflowEnd(final String workflowClassName) {
        LoggerFactory.getLogger(this.getClass()).info("workflowEnd[{}]", workflowClassName);
    }

    /**
     * Called after instrumentation.
     */
    default void endInstrument() {
        LoggerFactory.getLogger(this.getClass()).info("endInstrument");
    }

    /**
     * Represents a CheckPoint within a workflow, where the processing might be
     * interrupted and stored during workflow execution of COPPER workflows.
     * <p>
     * A CheckPoint is created for interruptable methods in a workflow and contains
     * metadata about the method and the specific point within the method where the
     * CheckPoint is recorded.
     *
     * @param methodDefinition The method definition where the CheckPoint exists.
     * @param jumpNo           The jump number within the method indicating the
     * @param interruptable    Call to the interruptable method.
     */
    record CheckpointInfo(
            MethodInfo methodDefinition,
            int jumpNo,
            MethodInfo interruptable
    ) {
    }

    /**
     * Represents metadata about a method within a workflow.
     * This can be a method definition or a method call.
     *
     * @param workflowClassName The fully qualified name of the class containing the method.
     *                          The name does not have a .class suffix and uses '/' as a path separator.
     * @param methodName        The name of the method being described.
     * @param methodDescriptor  A descriptor specifying the method's parameters and return type.
     */
    record MethodInfo(
            String workflowClassName,
            String methodName,
            String methodDescriptor
    ) {
    }

    /**
     * Represents metadata about a variable used in a workflow.
     * This record is used to store information about variables detected in a method,
     * including the variable's name, its index within the method, and the context of the
     * method it belongs to.
     *
     * @param name       The name of the variable as it appears in the method.
     * @param index      The index of the variable in the method's local variable table.
     * @param methodInfo Metadata about the method where the variable is defined or accessed.
     */
    record VariableInfo(
            String name,
            int index,
            MethodInfo methodInfo
    ) {
    }
}
