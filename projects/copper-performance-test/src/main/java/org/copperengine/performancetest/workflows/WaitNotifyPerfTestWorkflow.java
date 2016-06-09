package org.copperengine.performancetest.workflows;

import java.util.concurrent.Semaphore;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.performancetest.impl.MockAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitNotifyPerfTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(WaitNotifyPerfTestWorkflow.class);

    private transient Semaphore semaphore;
    private transient MockAdapter mockAdapter;

    @AutoWire
    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @Override
    public void main() throws Interrupt {
        logger.debug("Starting....");
        for (int i = 0; i < 10; i++) {
            final String cid = getEngine().createUUID();
            mockAdapter.foo("foo", cid, 50);
            inner: for (;;) {
                wait(WaitMode.ALL, 10000, cid);
                Response<Object> r = getAndRemoveResponse(cid);
                if (r.isTimeout()) {
                    logger.warn("Timeout");
                }
                else {
                    break inner;
                }
            }
        }
        logger.debug("Finished!");
        semaphore.release();
    }
}
