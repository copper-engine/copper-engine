/**
 * Copyright 2002-2017 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
