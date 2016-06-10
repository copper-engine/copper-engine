package org.copperengine.performancetest.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.WorkflowInstanceDescr;
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
            final int dataSize = 1000;
            final int numbOfWfI = 50;
            final String data = createTestData(dataSize);
            final PersistentProcessingEngine engine = context.getEngine();
            final Random random = new Random();

            logger.info("Starting latency performance test with {} workflow instances and data size {} chars ...", numbOfWfI, dataSize);
            final long startTS = System.currentTimeMillis();
            List<WorkflowInstanceDescr<?>> batch = new ArrayList<>();
            for (int i = 0; i < numbOfWfI; i++) {
                String wfiId = engine.run(new WorkflowInstanceDescr<>("org.copperengine.performancetest.workflows.SavepointPerfTestWorkflow", data));
                context.getBackchannel().wait(wfiId, 1, TimeUnit.MINUTES);
                Thread.sleep(random.nextInt(100) + 5);
            }
            if (!batch.isEmpty()) {
                engine.runBatch(batch);
            }

            logger.info("Workflow instances started, waiting...");
            final long et = System.currentTimeMillis() - startTS;
            logger.info("Finished performance test with {} workflow instances in {} msec", numbOfWfI, et);

            Thread.sleep(5000); // drain the batcher, etc.
            logger.info("statistics:\n{}", context.getStatisticsCollector().print());

        } catch (Exception e) {
            logger.error("performance test failed", e);
        }
    }
}
