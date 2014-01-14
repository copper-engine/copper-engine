/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.core.test;

import java.util.ArrayList;
import java.util.List;

import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowFactory;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentPerformanceTestInputChannel implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PersistentPerformanceTestInputChannel.class);

    private PersistentScottyEngine engine;
    private int max = 0;
    private int batchSize = 500;

    public void setEngine(PersistentScottyEngine engine) {
        this.engine = engine;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        // generate some data to work on
        // the workflow process instance will obtain this data
        final int SIZE = 64;
        StringBuilder dataSB = new StringBuilder(SIZE);
        for (int i = 0; i < SIZE; i++) {
            int pos = (int) (Math.random() * 70.0);
            dataSB.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890!§$%&/()=?".substring(pos, pos + 1));
        }
        final String data = dataSB.toString();
        logger.info("data=" + data);

        try {
            // create the workflow 'PersistentSpock2GTestWF'
            WorkflowFactory<String> wfFactory = engine.createWorkflowFactory("org.copperengine.core.test.PersistentSpock2GTestWF");

            // warm up the engine by processing 2 workflow calls
            logger.info("Warming up...");
            List<Workflow<?>> x = new ArrayList<Workflow<?>>(100);
            for (int i = 0; i < 2; i++) {
                Workflow<String> wf = wfFactory.newInstance();
                wf.setData(data);
                x.add(wf);
            }
            engine.run(x);
            Thread.sleep(5000);

            // load the engine with workflows
            logger.info("Test starts now");
            final long startTS = System.currentTimeMillis();
            for (int k = 0; k < max; k++) {
                List<Workflow<?>> list = new ArrayList<Workflow<?>>(100);
                for (int i = 0; i < batchSize; i++) {
                    Workflow<String> wf = wfFactory.newInstance();
                    wf.setData(data);
                    wf.setProcessorPoolId("P#" + (k % 6 + 1));
                    list.add(wf);
                }
                engine.run(list);
            }
            logger.info("all " + (max * batchSize) + " request(s) created");

            // wait for workflow responses
            Counter.doWait(max * batchSize - 50);
            long et = System.currentTimeMillis() - startTS;
            logger.info("Done Waiting - et = " + (et / 1000L));

            Thread.sleep(15000);
            // logger.info(engine.getDbStorage().getStatistics());
        } catch (Exception e) {
            logger.error("run failed", e);
        }
    }

    public void startup() {
        new Thread(this).start();
    }

    public void shutdown() {

    }
}
