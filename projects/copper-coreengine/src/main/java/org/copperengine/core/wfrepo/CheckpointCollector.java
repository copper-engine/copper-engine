package org.copperengine.core.wfrepo;

import org.slf4j.LoggerFactory;

/**
 * A CheckpointCollector is called while loading and instrumenting COPPER workflows
 * to collect workflow start, all workflow checkpoints (wait, resubmit, savepoint) and workflow end.
 * and interruptabel method TODO
 * <p>
 * The first and last call are {@link #startInstrument()} and {@link #endInstrument()}.
 * <p>
 * For each workflow the first and last call
 * are {@link #workflowStart(String)} and {@link #workflowEnd(String)}.
 * For every checkpoint call {@link #add(CheckPoint)} is called.
 * <p>
 *
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
