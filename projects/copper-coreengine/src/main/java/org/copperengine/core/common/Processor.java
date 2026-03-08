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

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.Workflow;
import org.copperengine.core.instrument.Transformed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A COPPER Processor is a thread executing {@link Workflow} instances
 * or delegates execution to virtual threads.
 *
 * @author austermann
 */
public abstract class Processor extends Thread {

    protected static final Logger logger = LoggerFactory.getLogger(Processor.class);
    protected final Queue<Workflow<?>> queue;
    protected volatile boolean shutdown = false;
    protected final ProcessingEngine engine;
    protected ProcessingHook processingHook = new MDCProcessingHook();
    private final int maxNumberOfDelegates;

    private boolean idle = false;

    private final ExecutorService executorService;
    private final Semaphore semaphore;

    public Processor(
            final String name,
            final Queue<Workflow<?>> queue,
            final int prio,
            final ProcessingEngine engine,
            final int maxNumberOfDelegates
    ) {
        super(name);
        this.queue = queue;
        this.setPriority(prio);
        this.engine = engine;
        this.maxNumberOfDelegates = maxNumberOfDelegates;
        if (isDelegating()) {
            semaphore = new Semaphore(maxNumberOfDelegates);
            executorService = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            semaphore = null;
            executorService = null;
        }
    }

    public Processor(String name, Queue<Workflow<?>> queue, int prio, final ProcessingEngine engine) {
        this(name, queue, prio, engine, 0);
    }

    public void setProcessingHook(ProcessingHook processingHook) {
        this.processingHook = processingHook;
    }

    public synchronized void shutdown() {
        if (shutdown)
            return;
        logger.info("Stopping processor '" + getName() + "'...");
        if (isDelegating()) {
            logger.info("Stopping executor service ...");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(3, TimeUnit.SECONDS)){
                    logger.warn("executor service not terminated");
                };
            } catch (InterruptedException e) {
                logger.debug("Ignore exception", e);
            }
        }
        shutdown = true;
        interrupt();
    }

    @Override
    public void run() {
        logger.info("started");
        while (!shutdown) {
            try {
                Workflow<?> wf = null;
                synchronized (queue) {
                    wf = queue.poll();
                    if (wf == null) {
                        logger.trace("queue is empty - waiting");
                        idle = true;
                        queue.wait();
                        idle = false;
                        logger.trace("waking up again...");
                        wf = queue.poll();
                    }
                }
                if (!shutdown && wf != null) {
                    if (wf.getClass().getAnnotation(Transformed.class) == null) {
                        throw new RuntimeException(wf.getClass().getName() + " has not been transformed");
                    }
                    if (isDelegating()) {
                        semaphore.acquire();
                    }
                    preProcess(wf);
                    try {
                        if (isDelegating()) {
                            final var delegatedWorkflow = wf;
                            executorService.submit(() -> process(delegatedWorkflow));
                        } else {
                            process(wf);
                        }
                    } finally {
                        if (isDelegating()) {
                            semaphore.release();
                        }
                        postProcess(wf);
                    }
                }
            } catch (InterruptedException e) {
                // ignore
            } catch (Throwable t) {
                logger.error("", t);
                t.printStackTrace();
            }
        }
        logger.info("stopped");
    }

    protected void postProcess(Workflow<?> wf) {
        if (processingHook != null) {
            processingHook.postProcess(wf);
        }
    }

    protected void preProcess(Workflow<?> wf) {
        if (processingHook != null) {
            processingHook.preProcess(wf);
        }
    }

    protected abstract void process(Workflow<?> wf);

    private boolean isDelegating() {
        return maxNumberOfDelegates > 0;
    }

    public boolean isIdle() {
        synchronized (queue) {
            return idle && semaphore.availablePermits() == maxNumberOfDelegates;
        }
    }

    public int getNumberOfDelegantes() {
        return maxNumberOfDelegates - semaphore.availablePermits();
    }
}
