package org.copperengine.core.persistent.cassandra.loadtest;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyResponseSender {

    private static final Logger logger = LoggerFactory.getLogger(DummyResponseSender.class);

    private final ScheduledExecutorService exec;
    private final ProcessingEngine engine;

    public DummyResponseSender(ScheduledExecutorService exec, ProcessingEngine engine) {
        super();
        this.exec = exec;
        this.engine = engine;
    }

    public void foo(final String cid, final int delay, final TimeUnit timeUnit) {
        if (delay == 0) {
            engine.notify(new Response<String>(cid, "foo" + cid, null), new Acknowledge.BestEffortAcknowledge());
        }
        else {
            exec.schedule(new Runnable() {
                @Override
                public void run() {
                    logger.debug("notify for cid={}", cid);
                    engine.notify(new Response<String>(cid, "foo" + cid, null), new Acknowledge.BestEffortAcknowledge());
                }
            }, delay, timeUnit);
        }
    }
}
