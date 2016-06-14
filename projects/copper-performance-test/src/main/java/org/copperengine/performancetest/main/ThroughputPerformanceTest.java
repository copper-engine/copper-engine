package org.copperengine.performancetest.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.persistent.PersistentPriorityProcessorPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThroughputPerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(ThroughputPerformanceTest.class);

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
            final int numberOfExtraProcessorPools = context.getConfigManager().getConfigInt(ConfigParameter.THROUGHPUTTEST_NUMBER_OF_EXTRA_PROC_POOLS);
            final int insertThreads = context.getConfigManager().getConfigInt(ConfigParameter.THROUGHPUTTEST_NUMBER_OF_INSERT_THREADS);
            final int insertBatchSize = context.getConfigManager().getConfigInt(ConfigParameter.THROUGHPUTTEST_BATCHS_SIZE);
            final int dataSize = context.getConfigManager().getConfigInt(ConfigParameter.THROUGHPUTTEST_DATA_SIZE);
            final int numbOfWfI = context.getConfigManager().getConfigInt(ConfigParameter.THROUGHPUTTEST_NUMBER_OF_WORKFLOW_INSTANCES);
            final String data = createTestData(dataSize);
            final PersistentProcessingEngine engine = context.getEngine();
            final Semaphore semaphore = new Semaphore(numbOfWfI);
            context.registerBean("semaphore", semaphore);

            for (int i = 0; i < numberOfExtraProcessorPools; i++) {
                final int procPoolNumbOfThreads = context.getConfigManager().getConfigInt(ConfigParameter.PROC_POOL_NUMB_OF_THREADS);
                final String ppoolId = "P" + i;
                logger.debug("Starting additional processor pool {} with {} threads", ppoolId, procPoolNumbOfThreads);
                final PersistentPriorityProcessorPool pool = new PersistentPriorityProcessorPool(ppoolId, context.getTransactionController(), procPoolNumbOfThreads);
                pool.setDequeueBulkSize(context.getConfigManager().getConfigInt(ConfigParameter.PROC_DEQUEUE_BULK_SIZE));
                context.getProcessorPoolManager().addProcessorPool(pool);
            }

            context.getConfigManager().log(logger, ConfigParameterGroup.throughput, ConfigParameterGroup.common, context.isCassandraTest() ? ConfigParameterGroup.cassandra : ConfigParameterGroup.rdbms);
            logger.debug("number of insert threads is {}", insertThreads);
            logger.debug("insert batch size is {}", insertBatchSize);
            logger.debug("numberOfExtraProcessorPools is {}", numberOfExtraProcessorPools);

            logger.info("Starting throughput performance test with {} workflow instances and data size {} chars ...", numbOfWfI, dataSize);
            semaphore.acquire(numbOfWfI);
            final long startTS = System.currentTimeMillis();
            ExecutorService pool = insertThreads >= 2 ? Executors.newFixedThreadPool(insertThreads) : null;
            List<WorkflowInstanceDescr<?>> batch = new ArrayList<>();
            for (int i = 0; i < numbOfWfI; i++) {
                String ppoolId = "P#DEFAULT";
                if (numberOfExtraProcessorPools > 0) {
                    ppoolId = "P" + (i % numberOfExtraProcessorPools);
                }
                batch.add(new WorkflowInstanceDescr<>("org.copperengine.performancetest.workflows.WaitNotifyPerfTestWorkflow", data, engine.createUUID(), 1, ppoolId));
                if (batch.size() == insertBatchSize) {
                    final List<WorkflowInstanceDescr<?>> __batch = batch;
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                engine.runBatch(__batch);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    if (pool != null)
                        pool.execute(r);
                    else
                        r.run();
                    batch = new ArrayList<>();
                }
            }
            if (!batch.isEmpty()) {
                engine.runBatch(batch);
            }
            if (pool != null) {
                pool.shutdown();
                pool.awaitTermination(10, TimeUnit.MINUTES);
            }

            logger.info("Workflow instances started, waiting...");
            semaphore.acquire(numbOfWfI);
            final long et = System.currentTimeMillis() - startTS;
            final long avgWaitNotifyPerSecond = numbOfWfI * 10L * 1000L / et;
            logger.info("Finished performance test with {} workflow instances in {} msec ==> {} wait/notify cycles per second", numbOfWfI, et, avgWaitNotifyPerSecond);

            Thread.sleep(5000); // drain the batcher
            logger.info("statistics:\n{}", context.getStatisticsCollector().print());

        } catch (Exception e) {
            logger.error("performance test failed", e);
        }
    }
}
