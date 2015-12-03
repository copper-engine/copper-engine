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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.Batcher;
import org.copperengine.core.persistent.txn.DatabaseTransaction;
import org.copperengine.core.persistent.txn.TransactionController;
import org.copperengine.management.BatcherMXBean;
import org.copperengine.management.DatabaseDialectMXBean;
import org.copperengine.management.ScottyDBStorageMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link ScottyDBStorageInterface}.
 * 
 * @author austermann
 */
public class ScottyDBStorage implements ScottyDBStorageInterface, ScottyDBStorageMXBean {

    private static final Logger logger = LoggerFactory.getLogger(ScottyDBStorage.class);

    private DatabaseDialect dialect;
    private TransactionController transactionController;

    private Batcher batcher;
    private long deleteStaleResponsesIntervalMsec = 60L * 60L * 1000L;

    private Thread enqueueThread;
    private ScheduledExecutorService scheduledExecutorService;
    private volatile boolean shutdown = false;
    private boolean checkDbConsistencyAtStartup = false;

    private CountDownLatch enqueueThreadTerminated = new CountDownLatch(1);

    public ScottyDBStorage() {

    }

    public void setCheckDbConsistencyAtStartup(boolean checkDbConsistencyAtStartup) {
        this.checkDbConsistencyAtStartup = checkDbConsistencyAtStartup;
    }

    public void setTransactionController(TransactionController transactionController) {
        this.transactionController = transactionController;
    }

    public void setDialect(DatabaseDialect dialect) {
        this.dialect = dialect;
    }

    protected <T> T run(final DatabaseTransaction<T> txn) throws Exception {
        return transactionController.run(txn);
    }

    public void setBatcher(Batcher batcher) {
        this.batcher = batcher;
    }

    public void setDeleteStaleResponsesIntervalMsec(long deleteStaleResponsesIntervalMsec) {
        this.deleteStaleResponsesIntervalMsec = deleteStaleResponsesIntervalMsec;
    }

    private void resumeBrokenBusinessProcesses() throws Exception {
        logger.info("resumeBrokenBusinessProcesses");
        run(new DatabaseTransaction<Void>() {
            @Override
            public Void run(Connection con) throws Exception {
                dialect.resumeBrokenBusinessProcesses(con);
                return null;
            }
        });
    }

    public void insert(final Workflow<?> wf, final Acknowledge ack) throws Exception {
        logger.trace("insert({})", wf);
        try {
            run(new DatabaseTransaction<Void>() {
                @Override
                public Void run(Connection con) throws Exception {
                    dialect.insert(wf, con);
                    return null;
                }
            });
            ack.onSuccess();
        } catch (Exception e) {
            ack.onException(e);
            throw e;
        }
    }

    public void insert(final List<Workflow<?>> wfs, final Acknowledge ack) throws Exception {
        logger.trace("insert(wfs.size={})", wfs.size());
        try {
            run(new DatabaseTransaction<Void>() {
                @Override
                public Void run(Connection con) throws Exception {
                    dialect.insert(wfs, con);
                    return null;
                }
            });
            ack.onSuccess();
        } catch (Exception e) {
            ack.onException(e);
            throw e;
        }
    }

    @Override
    public List<Workflow<?>> dequeue(final String ppoolId, final int max) throws Exception {
        if (max <= 0)
            return Collections.emptyList();

        while (true) {
            List<Workflow<?>> ret = run(new DatabaseTransaction<List<Workflow<?>>>() {
                @Override
                public List<Workflow<?>> run(Connection con) throws Exception {
                    return dialect.dequeue(ppoolId, max, con);
                }
            });
            if (!ret.isEmpty()) {
                return ret;
            }
            waitForEnqueue();
        }
    }

    final int waitForEnqueueMSec = 500;
    Object enqueueSignal = new Object();

    private void waitForEnqueue() throws InterruptedException {
        synchronized (enqueueSignal) {
            enqueueSignal.wait(waitForEnqueueMSec);
        }
    }

    private void signalEnqueue() {
        synchronized (enqueueSignal) {
            enqueueSignal.notify();
        }
    }

    Object queueStateSignal = new Object();

    private void waitForQueueState(int waitTime) throws InterruptedException {
        synchronized (queueStateSignal) {
            queueStateSignal.wait(waitTime);
        }
    }

    private void signalQueueState() {
        synchronized (queueStateSignal) {
            queueStateSignal.notify();
        }
    }

    protected List<List<String>> splitt(Collection<String> keySet, int n) {
        if (keySet.isEmpty())
            return Collections.emptyList();

        List<List<String>> r = new ArrayList<List<String>>(keySet.size() / n + 1);
        List<String> l = new ArrayList<String>(n);
        for (String s : keySet) {
            l.add(s);
            if (l.size() == n) {
                r.add(l);
                l = new ArrayList<String>(n);
            }
        }
        if (l.size() > 0) {
            r.add(l);
        }
        return r;
    }

    public void notify(final List<Response<?>> response, Acknowledge ack) throws Exception {
        for (Response<?> r : response)
            notify(r, ack);
    }

    public synchronized void startup() {
        try {
            dialect.startup();

            checkDbConsistencyAtStartup();
            deleteStaleResponse();
            resumeBrokenBusinessProcesses();

            enqueueThread = new Thread("ENQUEUE") {
                @Override
                public void run() {
                    updateQueueState();
                }
            };
            enqueueThread.start();

            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

            scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        deleteStaleResponse();
                    } catch (Exception e) {
                        logger.error("deleteStaleResponse failed", e);
                    }
                }
            }, deleteStaleResponsesIntervalMsec, deleteStaleResponsesIntervalMsec, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new Error("Unable to startup", e);
        }
    }

    private void checkDbConsistencyAtStartup() {
        if (!checkDbConsistencyAtStartup) {
            return;
        }

        logger.info("doing checkDbConsistencyAtStartup...");
        try {
            run(new DatabaseTransaction<Void>() {
                @Override
                public Void run(Connection con) throws Exception {
                    dialect.checkDbConsistency(con);
                    return null;
                }
            });
            logger.info("finished checkDbConsistencyAtStartup");
        } catch (Exception e) {
            logger.error("checkDbConsistencyAtStartup failed", e);
        }
    }

    private void deleteStaleResponse() throws Exception {
        if (logger.isTraceEnabled())
            logger.trace("deleteStaleResponse()");

        int n = 0;
        final int MAX_ROWS = 20000;
        do {
            if (shutdown)
                break;
            n = run(new DatabaseTransaction<Integer>() {
                @Override
                public Integer run(Connection con) throws Exception {
                    return dialect.deleteStaleResponse(con, MAX_ROWS);
                }
            });
        } while (n == MAX_ROWS);
    }

    public synchronized void shutdown() {
        if (shutdown)
            return;

        shutdown = true;

        scheduledExecutorService.shutdown();

        shutdownEnqueueThread();

        dialect.shutdown();
    }

    private void shutdownEnqueueThread() {
        enqueueThread.interrupt();
        try {
            enqueueThreadTerminated.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("await interrupted", e);
        }
    }

    private void updateQueueState() {
        final int max = 5000;
        final int lowTraffic = 100;
        logger.info("started");
        int sleepTime = 0;
        int sleepTimeMaxIdle = 2000;
        int sleepTimeMaxLowTraffic = 500;
        while (!shutdown) {
            int x = 0;
            try {
                x = run(new DatabaseTransaction<Integer>() {
                    @Override
                    public Integer run(Connection con) throws Exception {
                        return dialect.updateQueueState(max, con);
                    }
                });
            } catch (Exception e) {
                logger.error("updateQueueState failed", e);
            }
            if (x > 0) {
                signalEnqueue();
            }
            if (x == 0) {
                sleepTime = Math.max(10, Math.min(3 * sleepTime / 2, sleepTimeMaxIdle));
            } else if (x < lowTraffic) {
                sleepTime = Math.max(10, Math.min(11 * sleepTime / 10, sleepTimeMaxLowTraffic));
            } else {
                sleepTime = 0;
            }
            if (sleepTime > 0) {
                try {
                    waitForQueueState(sleepTime);
                } catch (InterruptedException e) {
                }
            }
        }
        logger.info("finished");
        enqueueThreadTerminated.countDown();
    }

    @Override
    public void insert(Workflow<?> wf, Connection con) throws Exception {
        if (con == null)
            insert(wf, new Acknowledge.BestEffortAcknowledge());
        else
            dialect.insert(wf, con);
    }

    @Override
    public void insert(List<Workflow<?>> wfs, Connection con) throws Exception {
        if (con == null)
            insert(wfs, new Acknowledge.BestEffortAcknowledge());
        else
            dialect.insert(wfs, con);
    }

    public void restart(final String workflowInstanceId) throws Exception {
        run(new DatabaseTransaction<Void>() {
            @Override
            public Void run(Connection con) throws Exception {
                dialect.restart(workflowInstanceId, con);
                return null;
            }
        });
    }

    @Override
    public void restartAll() throws Exception {
        run(new DatabaseTransaction<Void>() {
            @Override
            public Void run(Connection con) throws Exception {
                dialect.restartAll(con);
                return null;
            }
        });
    }

    @Override
    public void setRemoveWhenFinished(boolean removeWhenFinished) {
        dialect.setRemoveWhenFinished(removeWhenFinished);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void runSingleBatchCommand(final BatchCommand cmd) throws Exception {
        try {
            run(new DatabaseTransaction<Void>() {
                @Override
                public Void run(Connection con) throws Exception {
                    cmd.executor().doExec(Collections.singletonList(cmd), con);
                    return null;
                }
            });
            cmd.callback().commandCompleted();
        } catch (Exception e) {
            cmd.callback().unhandledException(e);
            throw e;
        }
    }

    @Override
    public void notify(List<Response<?>> responses, Connection c) throws Exception {
        dialect.notify(responses, c);
    }

    @Override
    public void error(Workflow<?> w, Throwable t, final Acknowledge callback) {
        if (logger.isTraceEnabled())
            logger.trace("error(" + w.getId() + "," + t.toString() + ")");
        try {
            executeBatchCommand(dialect.createBatchCommand4error(w, t, DBProcessingState.ERROR, callback));
        } catch (Exception e) {
            logger.error("error failed", e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void executeBatchCommand(BatchCommand cmd) throws Exception {
        if (batcher != null) {
            batcher.submitBatchCommand(cmd);
        } else {
            runSingleBatchCommand(cmd);
        }
    }

    public void registerCallback(final RegisterCall rc, final Acknowledge callback) throws Exception {
        if (logger.isTraceEnabled())
            logger.trace("registerCallback(" + rc + ")");
        if (rc == null)
            throw new NullPointerException();
        executeBatchCommand(dialect.createBatchCommand4registerCallback(rc, this, callback));
    }

    public void notify(final Response<?> response, final Acknowledge callback) throws Exception {
        if (logger.isTraceEnabled())
            logger.trace("notify(" + response + ")");
        if (response == null)
            throw new NullPointerException();
        Acknowledge notify = new Acknowledge() {
            @Override
            public void onSuccess() {
                signalQueueState();
                callback.onSuccess();
            }

            @Override
            public void onException(Throwable t) {
                callback.onException(t);
            }
        };
        executeBatchCommand(dialect.createBatchCommand4Notify(response, notify));
    }

    public void finish(final Workflow<?> w, final Acknowledge callback) {
        if (logger.isTraceEnabled())
            logger.trace("finish(" + w.getId() + ")");
        try {
            executeBatchCommand(dialect.createBatchCommand4Finish(w, callback));
        } catch (Exception e) {
            logger.error("finish failed", e);
            error(w, e, callback);
        }
    }

    @Override
    public BatcherMXBean getBatcherMXBean() {
        return (BatcherMXBean) (batcher instanceof BatcherMXBean ? batcher : null);
    }

    @Override
    public DatabaseDialectMXBean getDatabaseDialectMXBean() {
        return (DatabaseDialectMXBean) (dialect instanceof DatabaseDialectMXBean ? dialect : null);
    }

    @Override
    public String getDescription() {
        return "Default RDBMS storage";
    }

    @Override
    public Workflow<?> read(final String workflowInstanceId) throws Exception {
        return run(new DatabaseTransaction<Workflow<?>>() {
            @Override
            public Workflow<?> run(Connection con) throws Exception {
                return dialect.read(workflowInstanceId, con);
            }
        });
    }

}
