package org.copperengine.core.persistent.hybrid;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class HybridDBStorageTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test_enqueue_dequeue_serial() {
        final String ppoolId = "DEFAULT";
        final int max = 100;
        final HybridDBStorage dbStorage = new HybridDBStorage(new StandardJavaSerializer(), Mockito.mock(WorkflowRepository.class), Mockito.mock(Storage.class), Mockito.mock(TimeoutManager.class), Mockito.mock(Executor.class));
        for (int i = 0; i < max; i++) {
            dbStorage._enqueue(Integer.toString(i), ppoolId, max - i);
        }
        for (int i = 0; i < max; i++) {
            QueueElement qe = dbStorage._dequeue(ppoolId);
            Assert.assertNotNull(qe);
        }
    }

    @Test
    public void test_enqueue_dequeue_parallel() throws Exception {
        final int numberOfThreads = Runtime.getRuntime().availableProcessors();
        final String ppoolId = "DEFAULT";
        final int max = 10000;
        final HybridDBStorage dbStorage = new HybridDBStorage(new StandardJavaSerializer(), Mockito.mock(WorkflowRepository.class), Mockito.mock(Storage.class), Mockito.mock(TimeoutManager.class), Mockito.mock(Executor.class));
        ExecutorService exec = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < max; i++) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    dbStorage._enqueue(UUID.randomUUID().toString(), ppoolId, 1);
                }
            });
        }
        exec.shutdown();
        exec.awaitTermination(10000, TimeUnit.MILLISECONDS);

        final AtomicInteger counter = new AtomicInteger(0);
        exec = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    for (;;) {
                        QueueElement qe = dbStorage._dequeue(ppoolId);
                        if (qe == null) {
                            break;
                        }
                        else {
                            counter.incrementAndGet();
                        }
                    }
                }
            });
        }
        exec.shutdown();
        exec.awaitTermination(10000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(max, counter.intValue());
    }
}
