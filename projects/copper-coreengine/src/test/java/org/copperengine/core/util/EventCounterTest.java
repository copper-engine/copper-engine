package org.copperengine.core.util;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class EventCounterTest {

    @Test
    public void simpleTest() {
        final EventCounter eventCounter = new EventCounter(5);
        long systemTS = System.currentTimeMillis();
        for (int i=0; i<10; i++) {
            eventCounter.countEvent(systemTS);
        }
        assertEquals(1, eventCounter.eventCounters.size());
        assertEquals(10, eventCounter.getNumberOfEvents(5, systemTS));
    }

    @Test
    public void complexTest() {
        final EventCounter eventCounter = new EventCounter(5);
        long systemTS = System.currentTimeMillis();
        for (int x=0; x<10; x++) {
            systemTS += 60000L;
            for (int i=0; i<x+1; i++) {
                eventCounter.countEvent(systemTS);
            }
        }
        assertEquals(5,eventCounter.eventCounters.size());
        assertEquals(10, eventCounter.getNumberOfEvents(1, systemTS));
        assertEquals(10+9, eventCounter.getNumberOfEvents(2, systemTS));
        assertEquals(10+9+8, eventCounter.getNumberOfEvents(3, systemTS));
        assertEquals(10+9+8+7, eventCounter.getNumberOfEvents(4, systemTS));
        assertEquals(10+9+8+7+6, eventCounter.getNumberOfEvents(5, systemTS));
        assertEquals(10+9+8+7+6, eventCounter.getNumberOfEvents(6, systemTS));
        assertEquals(10+9+8+7+6, eventCounter.getNumberOfEvents(7, systemTS));
        assertEquals(10+9+8+7+6, eventCounter.getNumberOfEvents(8, systemTS));
    }


    @Test
    public void concurrencyTest() throws Exception {
        final int size = 5;
        final int numbOfThreads = Runtime.getRuntime().availableProcessors();
        final EventCounter eventCounter = new EventCounter(size) {
            @Override
            void checkClockReset(long now) {
                // no check for this unit test
            }
        };
        final AtomicLong expectedValue = new AtomicLong();
        final long systemTS = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(numbOfThreads);
        for (int y=0; y<numbOfThreads; y++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int x=0; x<size; x++) {
                            final long targetTS = systemTS + x*60000L;
                            final int max = 500000 + new Random().nextInt(1000000);
                            for (int i=0; i<max; i++) {
                                eventCounter.countEvent(targetTS);
                                expectedValue.incrementAndGet();
                            }
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(20, TimeUnit.SECONDS);
        assertEquals(expectedValue.get(), eventCounter.getNumberOfEvents(size, systemTS + (long)(size-1)*60000L));
    }    

}
