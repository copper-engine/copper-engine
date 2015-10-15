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
package org.copperengine.monitoring.server.monitoring;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

import org.copperengine.monitoring.core.data.MonitoringDataAccessor;
import org.copperengine.monitoring.core.data.MonitoringDataAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provide thread save access to monitoring data by serialize the access with
 * {@link java.util.concurrent.ArrayBlockingQueue}.
 * Accessing monitoring data is not blocking so monitoring can't block copper core functionality.
 */
public class MonitoringDataAccessQueue {

    public static final String IGNORE_WARN_TEXT = "could not process monitoring data. total ignored:";
    final ArrayBlockingQueue<Runnable> queue;
    AtomicLong ignored = new AtomicLong();
    private static final Logger logger = LoggerFactory.getLogger(MonitoringDataAccessQueue.class);

    /**
     * Contains the data for monitoring.
     * Should only be accessed via the {@link MonitoringDataAccessQueue}
     */
    private final MonitoringDataAccessor monitoringDataAccesor;
    private final MonitoringDataAdder monitoringDataAdder;
    private final MonitoringQueueThread thread;
    private final int queueCapacity;

    public MonitoringDataAccessQueue(MonitoringDataAccessor monitoringDataAccesor, MonitoringDataAdder monitoringDataAdder) {
        this(1000, monitoringDataAccesor, monitoringDataAdder);
    }

    public MonitoringDataAccessQueue(int queueCapacity, MonitoringDataAccessor monitoringDataAccesor, MonitoringDataAdder monitoringDataAdder) {
        this.monitoringDataAccesor = monitoringDataAccesor;
        this.monitoringDataAdder = monitoringDataAdder;
        this.queueCapacity = queueCapacity;
        queue = new ArrayBlockingQueue<Runnable>(queueCapacity);
        (thread = new MonitoringQueueThread(this)).start();
    }

    static class MonitoringQueueThread extends Thread {

        // allow for gc to clean MonitoringDataAccessQueue
        WeakReference<MonitoringDataAccessQueue> queue;
        volatile boolean run = true;

        MonitoringQueueThread(MonitoringDataAccessQueue queue) {
            super("monitoringEventQueue");
            this.queue = new WeakReference<MonitoringDataAccessQueue>(queue);
        }

        public void shutdown() {
            run = false;
            interrupt();
        }

        @Override
        public void run() {
            while (run) {
                work();
            }
        }

        ArrayList<Runnable> elements = new ArrayList<Runnable>(100);

        public void work() {
            elements.clear();
            MonitoringDataAccessQueue theQueue = this.queue.get();
            if (theQueue == null)
                return;
            int capacity = theQueue.queueCapacity;
            ArrayBlockingQueue<Runnable> q = theQueue.queue;
            theQueue = null;
            elements.clear();
            // only if thread should still be running wait for new data to arrive
            // else drain the queue until all elements are consumed
            if (run) {
                try {
                    elements.add(q.take());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            q.drainTo(elements, capacity);
            for (Runnable runnable : elements) {
                runnable.run();
            }
        }

    }

    public boolean offer(MonitoringDataAwareRunnable runnable) {
        runnable.setMonitoringDataAccesor(monitoringDataAccesor);
        runnable.setMonitoringDataAdder(monitoringDataAdder);
        boolean result = queue.offer(runnable);
        if (!result && !runnable.dropSilently) {
            logger.warn(IGNORE_WARN_TEXT + ignored.incrementAndGet());
        }
        return result;
    }

    /**
     * @param runnable
     * @see java.util.concurrent.BlockingQueue#put
     */
    public void put(MonitoringDataAwareRunnable runnable) {
        runnable.setMonitoringDataAccesor(monitoringDataAccesor);
        runnable.setMonitoringDataAdder(monitoringDataAdder);
        try {
            queue.put(runnable);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public <T> T callAndWait(MonitoringDataAwareCallable<T> callable) {
        callable.setMonitoringDataAccesor(monitoringDataAccesor);
        callable.setMonitoringDataAdder(monitoringDataAdder);
        try {
            FutureTask<T> futureTask = new FutureTask<T>(callable);
            queue.put(futureTask);
            try {
                return futureTask.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof RuntimeException)
                    throw (RuntimeException) e.getCause();
                throw new RuntimeException(e.getCause());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.thread.shutdown();
    }

}
