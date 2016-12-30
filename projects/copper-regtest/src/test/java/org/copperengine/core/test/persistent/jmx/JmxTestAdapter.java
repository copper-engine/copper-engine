package org.copperengine.core.test.persistent.jmx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxTestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(JmxTestAdapter.class);
    
    private final ProcessingEngine engine;
    private volatile CountDownLatch latch = new CountDownLatch(0);
    private final List<String> cids = new ArrayList<>();

    public JmxTestAdapter(ProcessingEngine engine) {
        this.engine = engine;
    }

    public void foo(String correlationId) {
        try {
            latch.await();
            synchronized (cids) {
                cids.add(correlationId);   
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void blockFoo() {
        logger.info("blockFoo()");
        latch = new CountDownLatch(1);
    }

    public void unblockFoo() {
        logger.info("unblockFoo()");
        latch.countDown();
    }

    public void createResponses() {
        logger.info("createResponses()");
        synchronized (cids) {
            for (String cid : cids) {
                engine.notify(new Response<>(cid, cid, null), new Acknowledge.BestEffortAcknowledge());
            }
            logger.info("created {} responses", cids.size());
            cids.clear();
        }
    }

}
