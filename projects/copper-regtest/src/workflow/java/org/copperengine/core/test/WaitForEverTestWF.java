package org.copperengine.core.test;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.test.backchannel.BackChannelQueue;
import org.copperengine.core.test.backchannel.WorkflowResult;

public class WaitForEverTestWF extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;

    private transient BackChannelQueue backChannelQueue;

    @AutoWire
    public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
        this.backChannelQueue = backChannelQueue;
    }

    @Override
    public void main() throws Interrupt {
        // this call should wait for ever!
        this.wait(WaitMode.ALL, Workflow.NO_TIMEOUT, getEngine().createUUID());

        // so this call should never happen!
        backChannelQueue.enqueue(new WorkflowResult(getData(), null));
    }

}
