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
package org.copperengine.core.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.Workflow;
import org.copperengine.core.internal.SuspendableQueue;
import org.copperengine.management.ProcessorPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ProcessorPool} implementation using a priority queue.
 * 
 * @author austermann
 */
public abstract class PriorityProcessorPool implements ProcessorPool, ProcessorPoolMXBean {

    private static final Logger logger = LoggerFactory.getLogger(PriorityProcessorPool.class);

    protected final SuspendableQueue<Workflow<?>> queue = new SuspendableQueue<Workflow<?>>(createQueue());
    private final List<Processor> workerThreads = new ArrayList<Processor>();

    private ProcessingEngine engine = null;
    private String id = null;
    private int numberOfThreads = Runtime.getRuntime().availableProcessors();
    private int threadPriority = Thread.NORM_PRIORITY;
    private int shutdownWaitIntervalMSec = 30000;

    private boolean started = false;
    private boolean shutdown = false;

    protected ProcessorFactory processorFactory;

    /**
     * Creates a new {@link PriorityProcessorPool} with as many worker threads as processors available on the
     * corresponding environment. <code>id</code> needs to be initialized later using the setter.
     */
    public PriorityProcessorPool() {
    }

    /**
     * Creates a new {@link PriorityProcessorPool} with as many worker threads as processors available on the
     * corresponding environment.
     */
    public PriorityProcessorPool(String id) {
        super();
        this.id = id;
    }

    public PriorityProcessorPool(String id, int numberOfThreads) {
        super();
        this.id = id;
        this.numberOfThreads = numberOfThreads;
    }

    /**
     * Creates a new instance of {@link WfPriorityQueue}
     */
    protected Queue<Workflow<?>> createQueue() {
        return new WfPriorityQueue();
    }

    /**
     * This processor pool wait up to the specified number of milliseconds until all of its Processors are terminated.
     * A value <= 0 means, that the processor pool will not wait at all.
     * 
     * @param shutdownWaitIntervalMSec
     *            wait interval in milliseconds
     */
    public void setShutdownWaitIntervalMSec(int shutdownWaitIntervalMSec) {
        this.shutdownWaitIntervalMSec = shutdownWaitIntervalMSec;
    }

    @Override
    public void setEngine(ProcessingEngine engine) {
        if (this.engine != null) {
            throw new IllegalArgumentException("engine is already set");
        }
        this.engine = engine;
    }

    public void setId(String id) {
        if (id != null) {
            throw new IllegalArgumentException("id is already set to " + this.id);
        }
        this.id = id;
    }

    public synchronized void setNumberOfThreads(int numberOfThreads) {
        if (numberOfThreads <= 0 || numberOfThreads >= 2048)
            throw new IllegalArgumentException();
        if (this.numberOfThreads != numberOfThreads) {
            logger.info("ProcessorPool " + id + ": Setting new number of processor threads");
            this.numberOfThreads = numberOfThreads;
            if (started) {
                updateThreads();
            }
        }
    }

    private void updateThreads() {
        if (numberOfThreads == workerThreads.size())
            return;
        while (numberOfThreads < workerThreads.size()) {
            Processor p = workerThreads.remove(workerThreads.size() - 1);
            p.shutdown();
            try {
                p.join(5000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        while (numberOfThreads > workerThreads.size()) {
            Processor p = processorFactory.newProcessor(id + "#" + workerThreads.size(), queue, threadPriority, engine);
            p.start();
            workerThreads.add(p);
        }
    }

    public synchronized int getNumberOfThreads() {
        return numberOfThreads;
    }

    public synchronized void setThreadPriority(int threadPriority) {
        if (threadPriority != this.threadPriority) {
            logger.info("ProcessorPool " + id + ": Setting new thread priority to " + threadPriority);
            this.threadPriority = threadPriority;
            for (Thread t : workerThreads) {
                t.setPriority(threadPriority);
            }
        }
    }

    public synchronized int getThreadPriority() {
        return threadPriority;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized void shutdown() {
        if (shutdown)
            return;

        logger.info("ProcessorPool " + id + ": Shutting down");

        shutdown = true;
        synchronized (queue) {
            queue.notifyAll();
        }

        for (Processor p : workerThreads) {
            p.shutdown();
        }

        final long endTS = System.currentTimeMillis() + shutdownWaitIntervalMSec;
        for (Processor p : workerThreads) {
            final long maxWait = endTS - System.currentTimeMillis();
            if (maxWait <= 0) {
                break;
            }
            try {
                p.join(maxWait);
            } catch (InterruptedException e) {
                logger.warn("Unexpected InterruptedException while waiting for 'join' to return", e);
            }
        }
    }

    public synchronized void startup() {
        if (id == null)
            throw new NullPointerException();
        if (engine == null)
            throw new NullPointerException();

        if (started)
            return;

        logger.info("ProcessorPool " + id + ": Starting up");

        started = true;
        updateThreads();
    }

    protected ProcessingEngine getEngine() {
        return engine;
    }

    @Override
    public int getMemoryQueueSize() {
        return queue.size();
    }

    @Override
    public void resume() {
        synchronized (queue) {
            queue.setSuspended(false);
            queue.notifyAll();
        }
    }

    @Override
    public void suspend() {
        synchronized (queue) {
            queue.setSuspended(true);
        }
    }

    public void setProcessorFactory(ProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

}
