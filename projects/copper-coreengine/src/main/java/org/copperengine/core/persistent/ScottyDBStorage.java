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
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.audit.AuditTrail;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.Batcher;
import org.copperengine.core.persistent.txn.DatabaseTransaction;
import org.copperengine.core.persistent.txn.TransactionController;
import org.copperengine.management.BatcherMXBean;
import org.copperengine.management.DatabaseDialectMXBean;
import org.copperengine.management.ScottyDBStorageMXBean;
import org.copperengine.management.model.AuditTrailInfo;
import org.copperengine.management.model.AuditTrailInstanceFilter;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link ScottyDBStorageInterface}.
 * 
 * @author austermann
 */
public class ScottyDBStorage implements ScottyDBStorageInterface, ScottyDBStorageMXBean {

    private static final Logger logger = LoggerFactory.getLogger(ScottyDBStorage.class);

    private final IdCache cidStore4responses = new IdCache(10000, 10, TimeUnit.SECONDS);
    private final QueueNotifier queueState = new QueueNotifier();
    private final Object enqueueSignal = new Object();
    private int waitForEnqueueMSec = 500;
    private volatile int clocksAllowedDeltaMSec = 100;
    private int clocksCheckIntervalSeconds = 60;

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
    
    public void setClocksAllowedDeltaMSec(int clocksAllowedDeltaMSec) {
        if (clocksAllowedDeltaMSec <= 0) 
            throw new IllegalArgumentException();
        this.clocksAllowedDeltaMSec = clocksAllowedDeltaMSec;
    }
    
    public void setClocksCheckIntervalSeconds(int clocksCheckIntervalSeconds) {
        if (clocksCheckIntervalSeconds <= 0) 
            throw new IllegalArgumentException();
        this.clocksCheckIntervalSeconds = clocksCheckIntervalSeconds;
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

    /**
     * Sets the time period after which early responses without a corresponding wait-call are removed from the database.
     * 
     * @param deleteStaleResponsesIntervalMsec
     *        time period in milliseconds
     */
    public void setDeleteStaleResponsesIntervalMsec(long deleteStaleResponsesIntervalMsec) {
        this.deleteStaleResponsesIntervalMsec = deleteStaleResponsesIntervalMsec;
    }

    public void setWaitForEnqueueMSec(int waitForEnqueueMSec) {
        this.waitForEnqueueMSec = waitForEnqueueMSec;
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

    @Override
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
            signalEnqueue();
        } catch (Exception e) {
            ack.onException(e);
            throw e;
        }
    }

    @Override
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
            signalEnqueue();
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

    private void waitForEnqueue() throws InterruptedException {
        logger.trace("waitForEnqueue...");
        synchronized (enqueueSignal) {
            enqueueSignal.wait(waitForEnqueueMSec);
        }
        logger.trace("waitForEnqueue DONE");
    }

    private void signalEnqueue() {
        logger.trace("signalEnqueue");
        synchronized (enqueueSignal) {
            enqueueSignal.notify();
        }
    }

    private void waitForQueueState(int waitTime) throws InterruptedException {
        queueState.waitForQueueState(waitTime);
    }

    private void signalQueueState() {
        queueState.signalQueueState();
    }

    @Override
    public void notify(final List<Response<?>> response, Acknowledge ack) throws Exception {
        for (Response<?> r : response)
            notify(r, ack);
    }

    @Override
    public synchronized void startup() {
        try {
            if (enqueueThread != null)
                return;
            
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
            
            scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    checkClocksAreSynchronized();
                }
            }, Math.min(5, clocksCheckIntervalSeconds), clocksCheckIntervalSeconds, TimeUnit.SECONDS);
            
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

        int n;
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

    @Override
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
            logger.trace("Starting updateQueueState...");
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
            logger.trace("updateQueueState returned x={}", x);
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
                } catch (InterruptedException ignore) {
                }
            }
        }
        logger.info("finished");
        enqueueThreadTerminated.countDown();
    }

    @Override
    public void insert(Workflow<?> wf, Connection con) throws Exception {
        if (con == null) {
            insert(wf, new Acknowledge.BestEffortAcknowledge());
        }
        else {
            dialect.insert(wf, con);
            signalEnqueue();
        }
    }

    @Override
    public void insert(List<Workflow<?>> wfs, Connection con) throws Exception {
        if (con == null) {
            insert(wfs, new Acknowledge.BestEffortAcknowledge());
        }
        else {
            dialect.insert(wfs, con);
            signalEnqueue();
        }
    }

    @Override
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
    public void restartFiltered(WorkflowInstanceFilter filter) throws Exception {
        run(new DatabaseTransaction<Void>() {
            @Override
            public Void run(Connection con) throws Exception {
                dialect.restartFiltered(filter, con);
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
    public void deleteBroken(final String workflowInstanceId) throws Exception {
        run(new DatabaseTransaction<Void>() {
            @Override
            public Void run(Connection con) throws Exception {
                dialect.deleteBroken(workflowInstanceId, con);
                return null;
            }
        });
    }

    @Override
    public void deleteWaiting(final String workflowInstanceId) throws Exception {
        run(new DatabaseTransaction<Void>() {
            @Override
            public Void run(Connection con) throws Exception {
                dialect.deleteWaiting(workflowInstanceId, con);
                return null;
            }
        });
    }

    @Override
    public void deleteFiltered(WorkflowInstanceFilter filter) throws Exception {
        run(new DatabaseTransaction<Void>() {
            @Override
            public Void run(Connection con) throws Exception {
                dialect.deleteFiltered(filter, con);
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
        logger.trace("notify(responses.size={})", responses.size());
        dialect.notify(responses, c);
        // TODO "signalQueueState();" is missing here - but we don't know when the transaction is commited, so we cannot
        // trigger it here
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

    @Override
    public void registerCallback(final RegisterCall rc, final Acknowledge callback) throws Exception {
        logger.trace("registerCallback({})", rc);
        if (rc == null)
            throw new NullPointerException();

        Acknowledge ack = new Acknowledge() {
            @Override
            public void onException(Throwable t) {
                if (callback != null)
                    callback.onException(t);
            }

            @Override
            public void onSuccess() {
                logger.trace("registerCallback successfully finished for {}", rc);

                // Sometimes the responses arrive _before_ wait is called in the workflow
                // In this case, we want the queue to be updated immediately to have short latency times
                if (cidStore4responses.contains(rc.correlationIds)) {
                    signalQueueState();
                }

                if (callback != null) {
                    callback.onSuccess();
                }
            }
        };

        PersistentWorkflow<?> pw = (PersistentWorkflow<?>) rc.workflow;
        if (pw.responseIdList != null) {
            for (String responseId : pw.responseIdList) {
                cidStore4responses.remove(responseId);
            }
        }

        executeBatchCommand(dialect.createBatchCommand4registerCallback(rc, this, ack));
    }

    @Override
    public void notify(final Response<?> response, final Acknowledge callback) throws Exception {
        logger.trace("notify({})", response);

        if (response == null)
            throw new NullPointerException();

        Acknowledge notify = new Acknowledge() {
            @Override
            public void onSuccess() {
                logger.trace("notify successfully finished for response {}", response);
                if (response.isEarlyResponseHandling())
                    cidStore4responses.put(response.getResponseId(), response.getCorrelationId());
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

    @Override
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

    @Override
    public List<Workflow<?>> queryAllActive(final String className, final int max) throws Exception {
        return run(new DatabaseTransaction<List<Workflow<?>>>() {
            @Override
            public List<Workflow<?>> run(Connection con) throws Exception {
                return dialect.queryAllActive(className, con, max);
            }
        });
    }

    void checkClocksAreSynchronized() {
        try {
            run(new DatabaseTransaction<Void>() {
                @Override
                public Void run(final Connection con) throws Exception {
                    final Timestamp appServerTS = new Timestamp(System.currentTimeMillis());
                    final long now = System.nanoTime();
                    final Date dbServerTS = dialect.readDatabaseClock(con);
                    final long etMSec = (System.nanoTime() - now) / 1000000L;
                    if (dbServerTS == null) {
                        logger.debug("readDatabaseClock not implemented for the dialect {} -> no check if DB and App server clocks are in sync", dialect);
                    }
                    else {
                        if (Math.abs(appServerTS.getTime() - dbServerTS.getTime()) > (etMSec+clocksAllowedDeltaMSec)) {
                            logger.warn("*** ATTENTION! App server and DB server clocks are not in sync: app={} db={} - This might cause timeout handling malfunction! ***", appServerTS, dbServerTS);
                        }
                        else {
                            logger.debug("App server and DB server clocks are (more or less) in sync: app={} db={}", appServerTS, dbServerTS);
                        }
                    }
                    return null;
                }
            });
        }
        catch(Exception e) {
            logger.error("checkClocksAreSynchronized failed", e);
        }

    }

    @Override
    public int queryQueueSize(final String processorPoolId) throws Exception {
        return run(new DatabaseTransaction<Integer>() {
            @Override
            public Integer run(Connection con) throws Exception {
                return dialect.queryQueueSize(processorPoolId, 50000, con);
            }
        });
    }

    @Override
    public String queryObjectState(String id) throws Exception {
        return run(new DatabaseTransaction<String>() {
            @Override
            public String run(Connection con) throws Exception {
                return dialect.queryObjectState(id, con);
            }
        });
    }

    @Override
    public List<Workflow<?>> queryWorkflowInstances(final WorkflowInstanceFilter filter) throws Exception {
        return run(new DatabaseTransaction<List<Workflow<?>>>() {
            @Override
            public List<Workflow<?>> run(Connection con) throws Exception {
                return dialect.queryWorkflowInstances(filter,con);
            }
        });
    }

    @Override
    public int countWorkflowInstances(final WorkflowInstanceFilter filter) throws Exception {
        return run(new DatabaseTransaction<Integer>() {
            @Override
            public Integer run(Connection con) throws Exception {
                return dialect.countWorkflowInstances(filter,con);
            }
        });
    }

    @Override
    public List<AuditTrailInfo> queryAuditTrailInstances(final AuditTrailInstanceFilter filter) throws Exception {
        return run(new DatabaseTransaction<List<AuditTrailInfo>>() {
            @Override
            public List<AuditTrailInfo> run(Connection con) throws Exception {
                return dialect.queryAuditTrailInstances(filter, con);
            }
        });
    }

    @Override
    public String queryAuditTrailMessage(long id) throws Exception {
        return run(new DatabaseTransaction<String>() {
            @Override
            public String run(Connection con) throws Exception {
                return dialect.queryAuditTrailMessage(id, con);
            }
        });
    }

    @Override
    public int countAuditTrailInstances(final AuditTrailInstanceFilter filter) throws Exception {
        return run(new DatabaseTransaction<Integer>() {
            @Override
            public Integer run(Connection con) throws Exception {
                return dialect.countAuditTrailInstances(filter, con);
            }
        });
    }

}
