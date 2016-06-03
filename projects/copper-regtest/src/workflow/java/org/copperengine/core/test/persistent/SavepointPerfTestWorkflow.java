package org.copperengine.core.test.persistent;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.util.Backchannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavepointPerfTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SavepointPerfTestWorkflow.class);

    private transient Backchannel backchannel;

    @AutoWire
    public void setBackchannel(Backchannel backchannel) {
        this.backchannel = backchannel;
    }

    @Override
    public void main() throws Interrupt {
        logger.info("Starting....");
        for (int i = 0; i < 100; i++) {
            logger.info("Savepoint...");
            final long startTS = System.currentTimeMillis();
            savepoint();
            final long et = System.currentTimeMillis() - startTS;
            System.out.println("Savepoint took " + et + " msec");
        }
        logger.info("Finished!");
        backchannel.notify(getId(), "Finished!");
    }
}
