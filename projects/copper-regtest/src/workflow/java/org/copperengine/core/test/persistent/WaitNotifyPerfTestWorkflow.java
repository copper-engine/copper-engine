package org.copperengine.core.test.persistent;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.test.MockAdapter;
import org.copperengine.core.util.Backchannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitNotifyPerfTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(WaitNotifyPerfTestWorkflow.class);

    private transient Backchannel backchannel;
    private transient MockAdapter mockAdapter;

    @AutoWire
    public void setBackchannel(Backchannel backchannel) {
        this.backchannel = backchannel;
    }

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @Override
    public void main() throws Interrupt {
        logger.info("Starting....");
        for (int i = 0; i < 100; i++) {
            logger.info("Wait/notify...");
            final String cid = getEngine().createUUID();
            mockAdapter.foo("foo", cid, 0);
            final long startTS = System.currentTimeMillis();
            wait(WaitMode.ALL, 1000, cid);
            final long et = System.currentTimeMillis() - startTS;
            System.out.println("Wait took " + et + " msec");
        }
        logger.info("Finished!");
        backchannel.notify(getId(), "Finished!");
    }
}
