package org.copperengine.core.persistent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class QueueNotifier {

    private static final Logger logger = LoggerFactory.getLogger(QueueNotifier.class);

    private static final Object queueStateSignal = new Object();

    private boolean wakeup = false;

    void waitForQueueState(int waitTime) throws InterruptedException {
        logger.trace("waitForQueueState({})...", waitTime);
        synchronized (queueStateSignal) {
            if (!wakeup) {
                queueStateSignal.wait(waitTime);
            }
            wakeup = false;
        }
        logger.trace("waitForQueueState({}) DONE", waitTime);
    }

    void signalQueueState() {
        logger.trace("signalQueueState");
        synchronized (queueStateSignal) {
            wakeup = true;
            queueStateSignal.notify();
        }
    }
}
