package org.copperengine.core.test.persistent.jmx;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(JmxTestWorkflow.class);
    
    private transient JmxTestAdapter jmxTestAdapter;
    
    @AutoWire
    public void setJmxTestAdapter(JmxTestAdapter jmxTestAdapter) {
        this.jmxTestAdapter = jmxTestAdapter;
    }

    @Override
    public void main() throws Interrupt {
        String cid = getEngine().createUUID();
        logger.info("Calling foo...");
        jmxTestAdapter.foo(cid);
        logger.info("Waiting...");
        wait(WaitMode.ALL,60000,cid);
        logger.info("Waking up again...");

        cid = getEngine().createUUID();
        logger.info("Calling foo...");
        jmxTestAdapter.foo(cid);
        logger.info("Waiting...");
        wait(WaitMode.ALL,60000,cid);
        logger.info("Waking up again...");
        
        logger.info("Finished!");
    }

}
