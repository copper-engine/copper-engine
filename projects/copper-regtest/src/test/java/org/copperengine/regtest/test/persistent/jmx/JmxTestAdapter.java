/*
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

package org.copperengine.regtest.test.persistent.jmx;

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
