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
package org.copperengine.core.persistent;

import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.copperengine.core.ProcessingState;
import org.copperengine.core.Workflow;
import org.copperengine.core.common.PriorityProcessorPool;
import org.copperengine.core.common.WfPriorityQueue;
import org.copperengine.core.internal.WorkflowAccessor;
import org.copperengine.core.persistent.txn.TransactionController;
import org.copperengine.management.PersistentPriorityProcessorPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link PriorityProcessorPool} interface for use in the {@link PersistentScottyEngine}.
 * 
 * @author austermann
 */
public class PersistentPriorityProcessorPool extends PriorityProcessorPool implements PersistentProcessorPool, PersistentPriorityProcessorPoolMXBean {

    public static final int DEFAULT_DEQUEUE_SIZE = 2000;

    private static final Logger logger = LoggerFactory.getLogger(PersistentPriorityProcessorPool.class);

    private TransactionController transactionController;

    private Thread thread;
    private volatile boolean shutdown = false;
    private final Object mutexEnqueue = new Object();
    private final Object mutexQueueSize = new Object();

    private volatile int lowerThreshold = 3000;
    private volatile int upperThreshold = 6000;
    private volatile int upperThresholdReachedWaitMSec = 50;
    private volatile int emptyQueueWaitMSec = 50;
    private volatile int _dequeueBulkSize = DEFAULT_DEQUEUE_SIZE;
    private Integer oldDequeueBulkSize = null;

    /**
     * Creates a new {@link PersistentPriorityProcessorPool} with as many worker threads as processors available on the
     * corresponding environment. <code>id</code> and <code>transactionControler</code> need to be initialized later
     * using the setter.
     */
    public PersistentPriorityProcessorPool() {
        super();
        processorFactory = new PersistentProcessorFactory();
    }

    /**
     * Creates a new {@link PersistentPriorityProcessorPool} with as many worker threads as processors available on the
     * corresponding environment.
     */
    public PersistentPriorityProcessorPool(String id, TransactionController transactionController) {
        super(id);
        this.transactionController = transactionController;
        processorFactory = new PersistentProcessorFactory(transactionController);
    }

    public PersistentPriorityProcessorPool(String id, TransactionController transactionController, int numberOfThreads) {
        super(id, numberOfThreads);
        this.transactionController = transactionController;
        processorFactory = new PersistentProcessorFactory(transactionController);
    }

    public void setTransactionController(TransactionController transactionController) {
        this.transactionController = transactionController;
        ((PersistentProcessorFactory) processorFactory).setTransactionController(transactionController);
    }

    @Override
    protected Queue<Workflow<?>> createQueue() {
        return new WfPriorityQueue() {
            private boolean notifiedLowerThreshold = false;

            @Override
            public Workflow<?> poll() {
                Workflow<?> wf = super.poll();
                if (!notifiedLowerThreshold && size() < lowerThreshold) {
                    signalQueueSizeBelowLowerThreshold();
                    notifiedLowerThreshold = true;
                }
                if (notifiedLowerThreshold && size() > lowerThreshold) {
                    notifiedLowerThreshold = false;
                }
                return wf;
            }

        };
    }

    @Override
    public synchronized void startup() {
        super.startup();
        if (transactionController == null)
            throw new NullPointerException("property transactionController is null");
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                PersistentPriorityProcessorPool.this.run();
            }
        }, getId() + "#DBReader");
        thread.start();
    }

    @Override
    public synchronized void shutdown() {
        super.shutdown();
        shutdown = true;
        thread.interrupt();
    }

    private void run() {
        logger.info("started");
        final PersistentScottyEngine engine = (PersistentScottyEngine) getEngine();
        final ScottyDBStorageInterface dbStorage = engine.getDbStorage();
        while (!shutdown) {
            try {
                while (!shutdown) {
                    int queueSize = 0;
                    synchronized (queue) {
                        queueSize = queue.size();
                    }
                    if (queueSize < upperThreshold) {
                        break;
                    }
                    logger.trace("Queue size {} >= upper threshold {}. Waiting...", queueSize, upperThreshold);
                    wait4QueueSizeBelowLowerThreshold();
                }
                List<Workflow<?>> rv;
                final int dequeueBulkSize = _dequeueBulkSize;
                if (dequeueBulkSize > 0) {
                    logger.trace("Dequeueing elements from DB...");
                    rv = dbStorage.dequeue(getId(), dequeueBulkSize);
                } else {
                    logger.trace("dequeueBulkSize is zero - dequeue suspended.");
                    rv = Collections.emptyList();
                }

                if (shutdown)
                    break;
                if (rv.isEmpty()) {
                    logger.trace("Dequeue returned nothing. Waiting...");
                    doWait(emptyQueueWaitMSec);
                } else {
                    logger.trace("Dequeue returned {} elements.", rv.size());
                    for (Workflow<?> wf : rv) {
                        WorkflowAccessor.setProcessingState(wf, ProcessingState.DEQUEUED);
                        engine.register(wf);
                    }
                    synchronized (queue) {
                        queue.addAll(rv);
                        queue.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                logger.info("interrupted");
            } catch (Exception e) {
                logger.error("dequeue failed", e);
            }
        }
        logger.info("stopped");
    }

    @Override
    public void doNotify() {
        logger.trace("doNotify");
        synchronized (mutexEnqueue) {
            mutexEnqueue.notify();
        }
    }

    private void doWait(long t) throws InterruptedException {
        logger.trace("doWait({})", t);
        synchronized (mutexEnqueue) {
            mutexEnqueue.wait(t);
        }
    }

    public void setLowerThreshold(int lowerThreshold) {
        if (lowerThreshold < 0 || lowerThreshold > upperThreshold)
            throw new IllegalArgumentException();
        this.lowerThreshold = lowerThreshold;
    }

    public int getLowerThreshold() {
        return lowerThreshold;
    }

    public void setUpperThreshold(int upperThreshold) {
        if (upperThreshold < 1 || upperThreshold < lowerThreshold)
            throw new IllegalArgumentException();
        this.upperThreshold = upperThreshold;
    }

    public int getUpperThreshold() {
        return upperThreshold;
    }

    public int getUpperThresholdReachedWaitMSec() {
        return upperThresholdReachedWaitMSec;
    }

    public void setUpperThresholdReachedWaitMSec(int upperThresholdReachedWaitMSec) {
        if (upperThresholdReachedWaitMSec <= 0)
            throw new IllegalArgumentException();
        this.upperThresholdReachedWaitMSec = upperThresholdReachedWaitMSec;
    }

    public int getEmptyQueueWaitMSec() {
        return emptyQueueWaitMSec;
    }

    public void setEmptyQueueWaitMSec(int emptyQueueWaitMSec) {
        if (emptyQueueWaitMSec <= 0)
            throw new IllegalArgumentException();
        this.emptyQueueWaitMSec = emptyQueueWaitMSec;
    }

    public int getDequeueBulkSize() {
        return _dequeueBulkSize;
    }

    public void setDequeueBulkSize(int dequeueBulkSize) {
        if (dequeueBulkSize < 0)
            throw new IllegalArgumentException();
        this._dequeueBulkSize = dequeueBulkSize;
    }

    protected TransactionController getTransactionController() {
        return transactionController;
    }

    @Override
    public synchronized void suspendDequeue() {
        if (oldDequeueBulkSize != null) {
            throw new IllegalStateException();
        }
        oldDequeueBulkSize = _dequeueBulkSize;
        _dequeueBulkSize = 0;
        logger.info("dequeue suspended");
    }

    @Override
    public synchronized void resumeDequeue() {
        if (oldDequeueBulkSize == null) {
            throw new IllegalStateException();
        }
        _dequeueBulkSize = oldDequeueBulkSize == 0 ? DEFAULT_DEQUEUE_SIZE : oldDequeueBulkSize;
        oldDequeueBulkSize = null;
        logger.info("dequeue resumed");
    }

    private void signalQueueSizeBelowLowerThreshold() {
        synchronized (mutexQueueSize) {
            mutexQueueSize.notify();
        }
    }

    private void wait4QueueSizeBelowLowerThreshold() throws InterruptedException {
        synchronized (mutexQueueSize) {
            mutexQueueSize.wait(upperThresholdReachedWaitMSec);
        }
    }


    @Override
    public int getQueueSize() {
        try {
            final PersistentScottyEngine engine = (PersistentScottyEngine) getEngine();
            final ScottyDBStorageInterface dbStorage = engine.getDbStorage();
            return dbStorage.queryQueueSize(getId());
        }
        catch(RuntimeException e) {
            throw e;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }    
}
