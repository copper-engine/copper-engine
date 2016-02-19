/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.core.persistent.cassandra.loadtest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.persistent.PersistentScottyEngine;

public class PermanentLoadCreator {

    private static final String WF_CLASS = "org.copperengine.core.persistent.cassandra.loadtest.workflows.LoadTestWorkflow";

    private LoadTestCassandraEngineFactory factory;
    private final AtomicInteger counter = new AtomicInteger();
    private final String payload;

    public PermanentLoadCreator(int payloadSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < payloadSize; i++) {
            sb.append(i % 10);
        }
        payload = sb.toString();
    }

    public synchronized PermanentLoadCreator start() throws Exception {
        if (factory != null)
            return this;

        factory = new LoadTestCassandraEngineFactory();
        factory.getEngine().startup();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                factory.destroyEngine();
            }
        });
        return this;
    }

    public PermanentLoadCreator startThread() {
        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    work();
                }
            }
        }.start();
        return this;
    }

    public void work() {
        try {
            final PersistentScottyEngine engine = factory.getEngine();
            List<String> cids = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                final String cid = engine.createUUID();
                final LoadTestData data = new LoadTestData(cid, payload);
                final WorkflowInstanceDescr<LoadTestData> wfid = new WorkflowInstanceDescr<LoadTestData>(WF_CLASS, data, cid, 1, null);
                engine.run(wfid);
                cids.add(cid);
            }
            for (String cid : cids) {
                factory.getBackchannel().wait(cid, 5, TimeUnit.MINUTES);
                int value = counter.incrementAndGet();
                if (value % 10000 == 0) {
                    System.out.println(new Date() + " - " + value + " workflow instances processed so far.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new PermanentLoadCreator(4096).start().startThread().startThread().startThread();
            System.out.println("Started!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
