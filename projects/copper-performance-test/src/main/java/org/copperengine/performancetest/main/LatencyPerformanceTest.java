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
package org.copperengine.performancetest.main;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.management.model.MeasurePointData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatencyPerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(LatencyPerformanceTest.class);

    protected String createTestData(int size) {
        StringBuilder sb = new StringBuilder(size);
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            sb.append(r.nextInt(2) == 0 ? "0" : "1");
        }
        return sb.toString();
    }

    public void run() {
        try (PerformanceTestContext context = new PerformanceTestContext()) {
            final int dataSize = context.getConfigManager().getConfigInt(ConfigParameter.LATENCY_DATA_SIZE);
            final int numbOfWfI = context.getConfigManager().getConfigInt(ConfigParameter.LATENCY_NUMBER_OF_WORKFLOW_INSTANCES);
            final String data = createTestData(dataSize);
            final PersistentProcessingEngine engine = context.getEngine();
            final Random random = new Random();

            context.getConfigManager().log(logger, ConfigParameterGroup.latency, ConfigParameterGroup.common, context.isCassandraTest() ? ConfigParameterGroup.cassandra : ConfigParameterGroup.rdbms);
            logger.info("Starting latency performance test with {} workflow instances and data size {} chars ...", numbOfWfI, dataSize);
            final long startTS = System.currentTimeMillis();
            for (int i = 0; i < numbOfWfI; i++) {
                String wfiId = engine.run(new WorkflowInstanceDescr<>("org.copperengine.performancetest.workflows.SavepointPerfTestWorkflow", data));
                context.getBackchannel().wait(wfiId, 1, TimeUnit.MINUTES);
                Thread.sleep(random.nextInt(100) + 5);
            }
            final long et = System.currentTimeMillis() - startTS;
            final MeasurePointData mp = context.getStatisticsCollector().query("savepoint.latency");
            final double avgLatency = (mp.getElapsedTimeMicros() / mp.getCount()) / 1000.0;
            logger.info("Finished performance test with {} workflow instances in {} msec, avg latency is {} msec", numbOfWfI, et, avgLatency);

            Thread.sleep(5000); // drain the batcher, etc.
            logger.info("statistics:\n{}", context.getStatisticsCollector().print());

        } catch (Exception e) {
            logger.error("performance test failed", e);
        }
    }
}
