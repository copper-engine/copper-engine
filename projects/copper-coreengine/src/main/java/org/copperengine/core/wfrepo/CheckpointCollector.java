package org.copperengine.core.wfrepo;

import org.slf4j.LoggerFactory;

/**
 * A CheckpointCollector is called while loading and instrumenting COPPER workflows
 * to collect instrumentation and workflow start, workflow end and all calls of interruptable methods.
 *
 * <p>
 * The first and last call are {@link #startInstrument()} and {@link #endInstrument()}.
 * </p><p>
 * For each workflow the first and last call
 * are {@link #workflowStart(String)} and {@link #workflowEnd(String)}.
 * For every call of an interruptable methods {@link #add(CheckPoint)} is called.
 * </p><p>
 * Implementations of the CheckpointCollector get this information of workflows
 * so that they can create meaningful documentation as BPMN, Activity Diagrams, etc.
 * </p>
 */
public interface CheckpointCollector {

    default void startInstrument() {
        LoggerFactory.getLogger(this.getClass()).info("startInstrument");
    }

    default void workflowStart(final String workflowClassName) {
        LoggerFactory.getLogger(this.getClass()).info("workflowStart[{}]", workflowClassName);
    }

    default void add(
            final CheckPoint checkpoint
    ) {
        LoggerFactory.getLogger(this.getClass()).info("addCheckpoint {}", checkpoint);
    }

    default void workflowEnd(final String workflowClassName) {
        LoggerFactory.getLogger(this.getClass()).info("workflowEnd[{}]", workflowClassName);
    }

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
     * @param workflowClassName             The fully qualified name of the workflow class.
     * @param methodName                    The name of the method where the CheckPoint exists.
     * @param methodDescriptor              The descriptor of the method where the CheckPoint exists.
     * @param jumpNo                        The jump number within the method indicating the
     *                                      specific point of execution.
     * @param interruptableMethodName       The name of the interruptable method associated
     *                                      with this CheckPoint.
     * @param interruptableMethodDescriptor The descriptor of the interruptable method
     *                                      associated with this CheckPoint.
     */
    record CheckPoint(
            String workflowClassName,
            String methodName,
            String methodDescriptor,
            int jumpNo,
            String interruptableMethodName,
            String interruptableMethodDescriptor
    ) {
    }
}
