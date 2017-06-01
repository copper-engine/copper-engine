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
package org.copperengine.core.persistent.hybrid;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.ProcessingState;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.internal.WorkflowAccessor;
import org.copperengine.core.persistent.RegisterCall;
import org.copperengine.core.persistent.ScottyDBStorageInterface;
import org.copperengine.core.persistent.Serializer;
import org.copperengine.core.util.Blocker;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

public class HybridDBStorage implements ScottyDBStorageInterface {

    private static final Logger logger = LoggerFactory.getLogger(HybridDBStorage.class);
    private static final Acknowledge.BestEffortAcknowledge ACK = new Acknowledge.BestEffortAcknowledge();

    private final Executor executor;
    private final TimeoutManager timeoutManager;
    private final Blocker startupBlocker = new Blocker(true);
    private final Map<String, ConcurrentSkipListSet<QueueElement>> ppoolId2queueMap;
    private final CorrelationIdMap correlationIdMap = new CorrelationIdMap();
    private final Serializer serializer;
    private final WorkflowRepository wfRepo;
    private final Storage storage;
    private final Object[] mutexArray = new Object[2003];
    private final Set<String> currentlyProcessingEarlyResponses = new HashSet<>();
    private boolean started = false;

    public HybridDBStorage(Serializer serializer, WorkflowRepository wfRepo, Storage storage, TimeoutManager timeoutManager, final Executor executor) {
        this.ppoolId2queueMap = new ConcurrentHashMap<>();
        this.serializer = serializer;
        this.wfRepo = wfRepo;
        this.storage = storage;
        this.timeoutManager = timeoutManager;
        this.executor = executor;
        for (int i = 0; i < mutexArray.length; i++) {
            mutexArray[i] = new Object();
        }
    }

    @Override
    public void insert(Workflow<?> wf, Acknowledge ack) throws DuplicateIdException, Exception {
        if (wf == null)
            throw new NullPointerException();

        logger.debug("insert({})", wf.getId());

        startupBlocker.pass();

        WorkflowInstance cw = new WorkflowInstance();
        cw.id = wf.getId();
        cw.serializedWorkflow = serializer.serializeWorkflow(wf);
        cw.ppoolId = wf.getProcessorPoolId();
        cw.prio = wf.getPriority();
        cw.creationTS = wf.getCreationTS();
        cw.state = ProcessingState.ENQUEUED;
        cw.classname = wf.getClass().getName();

        storage.safeWorkflowInstance(cw, true);

        _enqueue(wf.getId(), wf.getProcessorPoolId(), wf.getPriority());

        if (ack != null)
            ack.onSuccess();

    }

    @Override
    public void insert(List<Workflow<?>> wfs, Acknowledge ack) throws DuplicateIdException, Exception {
        for (Workflow<?> wf : wfs) {
            insert(wf, ACK);
        }
        ack.onSuccess();
    }

    @Override
    public void insert(Workflow<?> wf, Connection con) throws DuplicateIdException, Exception {
        insert(wf, ACK);
    }

    @Override
    public void insert(List<Workflow<?>> wfs, Connection con) throws DuplicateIdException, Exception {
        for (Workflow<?> wf : wfs) {
            insert(wf, ACK);
        }
    }

    @Override
    public void finish(final Workflow<?> w, final Acknowledge _callback) {
        logger.debug("finish({})", w.getId());
        final Acknowledge callback = _callback != null ? _callback : ACK;
        try {
            startupBlocker.pass();

            final String wfId = w.getId();
            correlationIdMap.removeAll4Workflow(wfId);
            final ListenableFuture<Void> future = storage.deleteWorkflowInstance(w.getId());
            future.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        future.get();
                        callback.onSuccess();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error("finish(" + wfId + ") failed", e);
                        callback.onException(e);
                    }
                }
            }, executor);
        } catch (Exception e) {
            logger.error("finish failed", e);
            callback.onException(e);
        }
    }

    @Override
    public List<Workflow<?>> dequeue(final String ppoolId, final int max) throws Exception {
        logger.debug("dequeue({},{})", ppoolId, max);
        final long startTS = System.currentTimeMillis();

        startupBlocker.pass();

        final List<Workflow<?>> wfList = new ArrayList<>(max);
        while (wfList.size() < max) {
            // block if we read the first element - since we don't want to return an empty list
            final QueueElement element = wfList.isEmpty() ? _take(ppoolId) : _poll(ppoolId);
            if (element == null)
                break;

            synchronized (findMutex(element.wfId)) {
                try {
                    correlationIdMap.removeAll4Workflow(element.wfId);
                    final WorkflowInstance wi = storage.readWorkflowInstance(element.wfId);
                    if (wi == null) {
                        logger.warn("No workflow instance with id {} found in database", element.wfId);
                        // TODO try again later?
                    }
                    else {
                        Workflow<?> wf = null;
                        try {
                            wf = convert2workflow(wi);
                        } catch (Exception e) {
                            logger.error("Unable to deserialize workflow instance " + element.wfId + " - setting state to INVALID", e);
                            storage.updateWorkflowInstanceState(element.wfId, ProcessingState.INVALID);
                        }
                        if (wf != null) {
                            timeoutManager.unregisterTimeout(wi.timeout, wi.id);
                            wfList.add(wf);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Fatal error: dequeue failed for workflow instance " + element.wfId, e);
                }
            }
        }
        logger.debug("dequeue({},{}) finished, returning {} elements in {} msec", ppoolId, max, wfList.size(), (System.currentTimeMillis() - startTS));
        return wfList;
    }

    private Workflow<?> convert2workflow(WorkflowInstance cw) throws Exception {
        if (cw == null)
            return null;

        Workflow<?> wf = serializer.deserializeWorkflow(cw.serializedWorkflow, wfRepo);
        wf.setId(cw.id);
        wf.setProcessorPoolId(cw.ppoolId);
        wf.setPriority(cw.prio);
        WorkflowAccessor.setCreationTS(wf, cw.creationTS);
        WorkflowAccessor.setLastActivityTS(wf, cw.lastModTS);

        if (cw.cid2ResponseMap != null) {
            for (Entry<String, String> e : cw.cid2ResponseMap.entrySet()) {
                if (e.getValue() != null) {
                    Response<?> r = serializer.deserializeResponse(e.getValue());
                    wf.putResponse(r);
                }
                else {
                    wf.putResponse(new Response<>(e.getKey())); // Set timeout response
                }
            }
        }
        return wf;
    }

    @Override
    public void registerCallback(RegisterCall rc, Acknowledge callback) throws Exception {
        logger.debug("registerCallback({})", rc);

        startupBlocker.pass();

        final String wfId = rc.workflow.getId();
        final WorkflowInstance cw = new WorkflowInstance();
        cw.id = wfId;
        cw.state = ProcessingState.WAITING;
        cw.prio = rc.workflow.getPriority();
        cw.creationTS = rc.workflow.getCreationTS();
        cw.serializedWorkflow = serializer.serializeWorkflow(rc.workflow);
        cw.waitMode = rc.waitMode;
        cw.timeout = rc.timeout != null && rc.timeout > 0 ? new Date(System.currentTimeMillis() + rc.timeout) : null;
        cw.ppoolId = rc.workflow.getProcessorPoolId();
        cw.cid2ResponseMap = new HashMap<String, String>();
        for (String cid : rc.correlationIds) {
            cw.cid2ResponseMap.put(cid, null);
        }
        cw.classname = rc.workflow.getClass().getName();

        storage.safeWorkflowInstance(cw, false);

        correlationIdMap.addCorrelationIds(wfId, rc.correlationIds);

        // check for early responses
        //
        // 1st make sure that all currently working threads writing early responses do NOT write a response with one of
        // our correlationIds
        synchronized (currentlyProcessingEarlyResponses) {
            for (;;) {
                boolean didWait = false;
                for (String cid : rc.correlationIds) {
                    if (currentlyProcessingEarlyResponses.contains(cid)) {
                        currentlyProcessingEarlyResponses.wait();
                        didWait = true;
                    }
                }
                if (!didWait)
                    break;
            }
        }
        // 2nd read early responses and connect them to the workflow instance
        boolean enqueued = false;
        for (String cid : rc.correlationIds) {
            Response<?> response = serializer.deserializeResponse(storage.readEarlyResponse(cid));
            if (response != null) {
                logger.debug("found early response with correlationId {} for workflow {} - doing notify...", cid, wfId);
                if (notifyInternal(response, ACK)) {
                    enqueued = true;
                }
                storage.deleteEarlyResponse(cid);
            }
        }

        if (cw.timeout != null && !enqueued) {
            timeoutManager.registerTimeout(cw.timeout, wfId, new Runnable() {
                @Override
                public void run() {
                    onTimeout(wfId);
                }
            });
        }

        callback.onSuccess();
    }

    @Override
    public void notify(Response<?> response, Acknowledge ack) throws Exception {
        logger.debug("notify({})", response);

        startupBlocker.pass();

        notifyInternal(response, ack);
    }

    /**
     * 
     * @param response
     * @param ack
     * @return true, if the corresponding workflow instance has been enqueued due to this response
     * @throws Exception
     */
    private boolean notifyInternal(Response<?> response, Acknowledge ack) throws Exception {
        logger.debug("notifyInternal({})", response);

        try {
            final String cid = response.getCorrelationId();
            final String wfId = correlationIdMap.getWorkflowId(cid);

            if (wfId != null) {
                // we have to take care of concurrent notifies for the same workflow instance
                // but we don't want to block everything - it's sufficient to block this workflows id (more or less...)
                synchronized (findMutex(wfId)) {
                    // check if this workflow instance has just been dequeued - in this case we do not find the
                    // correlationId any more...
                    if (correlationIdMap.getWorkflowId(cid) != null) {
                        WorkflowInstance cw = storage.readWorkflowInstance(wfId);
                        if (cw.cid2ResponseMap.containsKey(cid)) {
                            cw.cid2ResponseMap.put(cid, serializer.serializeResponse(response));
                        }
                        final boolean timeoutOccured = cw.timeout != null && cw.timeout.getTime() <= System.currentTimeMillis();
                        final boolean enqueue = cw.state == ProcessingState.WAITING && (timeoutOccured || cw.waitMode == WaitMode.FIRST || cw.waitMode == WaitMode.ALL && cw.cid2ResponseMap.size() == 1 || cw.waitMode == WaitMode.ALL && allResponsesAvailable(cw));

                        if (enqueue) {
                            cw.state = ProcessingState.ENQUEUED;
                        }

                        storage.safeWorkflowInstance(cw, false);

                        if (enqueue) {
                            _enqueue(cw.id, cw.ppoolId, cw.prio);
                        }

                        ack.onSuccess();
                        return enqueue;
                    }
                }
            }
        } catch (Exception e) {
            ack.onException(e);
            throw e;
        }

        handleEarlyResponse(response, ack);
        return false;
    }

    private void onTimeout(final String wfId) {
        logger.debug("onTimeout(wfId={})", wfId);
        try {
            synchronized (findMutex(wfId)) {
                // check if this workflow instance has just been dequeued - in this case we do not find the
                // correlationId any more...
                if (correlationIdMap.containsWorkflowId(wfId)) {
                    final WorkflowInstance cw = storage.readWorkflowInstance(wfId);
                    logger.debug("workflow instance={}", cw);
                    final boolean enqueue = cw.state == ProcessingState.WAITING;

                    if (enqueue) {
                        cw.state = ProcessingState.ENQUEUED;
                        storage.safeWorkflowInstance(cw, false);
                        _enqueue(cw.id, cw.ppoolId, cw.prio);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("onTimeout failed for wfId " + wfId, e);
        }
    }

    private void handleEarlyResponse(final Response<?> response, final Acknowledge ack) throws Exception {
        synchronized (currentlyProcessingEarlyResponses) {
            currentlyProcessingEarlyResponses.add(response.getCorrelationId());
        }
        final ListenableFuture<Void> future = storage.safeEarlyResponse(response.getCorrelationId(), serializer.serializeResponse(response));
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    future.get();
                    ack.onSuccess();
                }
                catch (Exception e) {
                    logger.error("safeEarlyResponse failed", e);
                    ack.onException(e);
                }
                finally {
                    synchronized (currentlyProcessingEarlyResponses) {
                        currentlyProcessingEarlyResponses.remove(response.getCorrelationId());
                        currentlyProcessingEarlyResponses.notifyAll();
                    }
                }
            }
        }, executor);
    }

    @Override
    public void notify(List<Response<?>> responses, Acknowledge ack) throws Exception {
        for (Response<?> r : responses) {
            notify(r, ACK);
        }
        ack.onSuccess();
    }

    @Override
    public void notify(List<Response<?>> responses, Connection c) throws Exception {
        for (Response<?> r : responses) {
            notify(r, ACK);
        }
    }

    @Override
    public synchronized void startup() {
        if (started)
            return;

        logger.info("Starting up...");
        try {
            storage.initialize(new HybridDBStorageAccessor() {
                @Override
                public void registerCorrelationId(String correlationId, String wfId) {
                    _registerCorrelationId(correlationId, wfId);
                }

                @Override
                public void enqueue(String wfId, String ppoolId, int prio) {
                    _enqueue(wfId, ppoolId, prio);
                }
            }, Runtime.getRuntime().availableProcessors());
        } catch (RuntimeException e) {
            logger.error("startup failed", e);
            throw e;

        } catch (Exception e) {
            logger.error("startup failed", e);
            throw new CopperRuntimeException("startup failed", e);
        }

        started = true;
        startupBlocker.unblock();

        logger.info("Startup finished!");
    }

    @Override
    public void shutdown() {
        // empty
    }

    @Override
    public void error(Workflow<?> w, Throwable t, Acknowledge callback) {
        try {
            startupBlocker.pass();
            correlationIdMap.removeAll4Workflow(w.getId());
            storage.updateWorkflowInstanceState(w.getId(), ProcessingState.ERROR);
            if (callback != null)
                callback.onSuccess();
        } catch (Exception e) {
            logger.error("error failed", e);
            if (callback != null)
                callback.onException(e);
        }
    }

    @Override
    public void restart(String workflowInstanceId) throws Exception {
        startupBlocker.pass();

        WorkflowInstance cw = storage.readWorkflowInstance(workflowInstanceId);
        if (cw == null)
            throw new CopperRuntimeException("No workflow found with id " + workflowInstanceId);
        if (cw.state != ProcessingState.ERROR)
            throw new CopperRuntimeException("Workflow found with id " + workflowInstanceId + " is not in state ERROR");
        _enqueue(cw.id, cw.ppoolId, cw.prio);
    }

    @Override
    public void setRemoveWhenFinished(boolean removeWhenFinished) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restartAll() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBroken(String workflowInstanceId) throws Exception {
        throw new UnsupportedOperationException();
        // TODO: Implement this here...
    }

    @Override
    public Workflow<?> read(String workflowInstanceId) throws Exception {
        return convert2workflow(storage.readWorkflowInstance(workflowInstanceId));
    }

    void _enqueue(String wfId, String ppoolId, int prio) {
        logger.trace("enqueue(wfId={}, ppoolId={}, prio={})", wfId, ppoolId, prio);
        ConcurrentSkipListSet<QueueElement> queue = _findQueue(ppoolId);
        synchronized (queue) {
            boolean rv = queue.add(new QueueElement(wfId, prio));
            assert rv : "queue already contains workflow id " + wfId;
            queue.notify();
        }
    }

    QueueElement _poll(String ppoolId) {
        logger.trace("_poll({})", ppoolId);
        ConcurrentSkipListSet<QueueElement> queue = _findQueue(ppoolId);
        QueueElement qe = queue.pollFirst();
        if (qe != null) {
            logger.debug("dequeued for ppoolId={}: wfId={}", ppoolId, qe.wfId);
        }
        return qe;
    }

    QueueElement _take(String ppoolId) throws InterruptedException {
        logger.trace("_take({})", ppoolId);
        ConcurrentSkipListSet<QueueElement> queue = _findQueue(ppoolId);
        synchronized (queue) {
            for (;;) {
                QueueElement qe = queue.pollFirst();
                if (qe != null) {
                    logger.debug("dequeued for ppoolId={}: wfId={}", ppoolId, qe.wfId);
                    return qe;
                }
                queue.wait(10L);
            }
        }
    }

    private ConcurrentSkipListSet<QueueElement> _findQueue(final String ppoolId) {
        ConcurrentSkipListSet<QueueElement> queue = ppoolId2queueMap.get(ppoolId);
        if (queue != null)
            return queue;
        synchronized (ppoolId2queueMap) {
            queue = ppoolId2queueMap.get(ppoolId);
            if (queue != null)
                return queue;
            queue = new ConcurrentSkipListSet<>(new QueueElementComparator());
            ppoolId2queueMap.put(ppoolId, queue);
            return queue;
        }
    }

    private void _registerCorrelationId(String correlationId, String wfId) {
        correlationIdMap.addCorrelationId(wfId, correlationId);
    }

    private boolean allResponsesAvailable(WorkflowInstance cw) {
        for (Entry<String, String> e : cw.cid2ResponseMap.entrySet()) {
            if (e.getValue() == null)
                return false;
        }
        return true;
    }

    private Object findMutex(String id) {
        long hash = id.hashCode();
        hash = Math.abs(hash);
        int x = (int) (hash % mutexArray.length);
        return mutexArray[x];
    }

    @Override
    public List<Workflow<?>> queryAllActive(final String className, final int max) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public int queryQueueSize(String processorPoolId) throws Exception {
        final ConcurrentSkipListSet<QueueElement> queue = ppoolId2queueMap.get(Objects.requireNonNull(processorPoolId));
        return queue == null ? 0 : queue.size();
    }

    @Override
    public List<Workflow<?>> queryWorkflowInstances(WorkflowInstanceFilter filter) throws Exception {
        List<Workflow<?>> resultList = new ArrayList<>();
        List<WorkflowInstance> list = storage.queryWorkflowInstances(filter);
        for (WorkflowInstance wi : list) {
            try {
                resultList.add(convert2workflow(wi));
            }
            catch(Exception e) {
                logger.error("Failed to convert workflow instance "+wi.id, e);
            }
        }
        return resultList;
    }

}
