package org.copperengine.core.persistent.hybrid;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HybridDBStorage implements ScottyDBStorageInterface {

    private static final Logger logger = LoggerFactory.getLogger(HybridDBStorage.class);
    private static final Acknowledge.BestEffortAcknowledge ACK = new Acknowledge.BestEffortAcknowledge();

    private Blocker startupBlocker = new Blocker(true);
    private final Map<String, Queue<QueueElement>> ppoolId2queueMap;
    private final CorrelationIdMap correlationIdMap = new CorrelationIdMap();
    private final Serializer serializer;
    private final WorkflowRepository wfRepo;
    private final Storage cassandra;
    private final Object[] mutexArray = new Object[2003];
    private final Set<String> currentlyProcessingEarlyResponses = new HashSet<>();
    private boolean started = false;

    public HybridDBStorage(Serializer serializer, WorkflowRepository wfRepo, Storage cassandra) {
        this.ppoolId2queueMap = new ConcurrentHashMap<>();
        this.serializer = serializer;
        this.wfRepo = wfRepo;
        this.cassandra = cassandra;
        for (int i = 0; i < mutexArray.length; i++) {
            mutexArray[i] = new Object();
        }
    }

    @Override
    public void insert(Workflow<?> wf, Acknowledge ack) throws DuplicateIdException, Exception {
        if (wf == null)
            throw new NullPointerException();

        startupBlocker.pass();

        WorkflowInstance cw = new WorkflowInstance();
        cw.id = wf.getId();
        cw.serializedWorkflow = serializer.serializeWorkflow(wf);
        cw.ppoolId = wf.getProcessorPoolId();
        cw.prio = wf.getPriority();
        cw.creationTS = wf.getCreationTS();
        cw.state = ProcessingState.ENQUEUED;

        cassandra.safeWorkflowInstance(cw);

        final Queue<QueueElement> queue = findQueue(wf.getProcessorPoolId());
        queue.add(new QueueElement(wf.getId(), wf.getPriority()));

        if (ack != null)
            ack.onSuccess();

    }

    private Queue<QueueElement> findQueue(final String ppoolId) {
        Queue<QueueElement> queue = ppoolId2queueMap.get(ppoolId);
        if (queue != null)
            return queue;
        synchronized (ppoolId2queueMap) {
            queue = ppoolId2queueMap.get(ppoolId);
            if (queue != null)
                return queue;
            queue = new PriorityQueue<QueueElement>(100, new QueueElementComparator());
            ppoolId2queueMap.put(ppoolId, queue);
            return queue;
        }
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
    public void finish(Workflow<?> w, Acknowledge callback) {
        try {
            startupBlocker.pass();

            // TODO mit Futures arbeiten
            cassandra.deleteWorkflowInstance(w.getId());
            if (callback != null)
                callback.onSuccess();
        } catch (Exception e) {
            logger.error("finish failed", e);
            if (callback != null)
                callback.onException(e);
        }
    }

    @Override
    public List<Workflow<?>> dequeue(String ppoolId, int max) throws Exception {
        logger.debug("dequeue({},{})", ppoolId, max);

        startupBlocker.pass();

        final Queue<QueueElement> queue = findQueue(ppoolId);
        if (queue.isEmpty())
            return Collections.emptyList();

        final List<Workflow<?>> wfList = new ArrayList<>(max);
        while (wfList.size() < max) {
            final QueueElement element = queue.poll();
            if (element == null)
                break;

            synchronized (findMutex(element.wfId)) {
                correlationIdMap.removeAll4Workflow(element.wfId);
                try {
                    final WorkflowInstance cw = cassandra.readCassandraWorkflow(element.wfId);
                    final Workflow<?> wf = convert2workflow(cw);
                    wfList.add(wf);
                } catch (Exception e) {
                    logger.error("Unable to read workflow instance " + element.wfId + " - setting state to INVALID", e);
                    cassandra.updateWorkflowInstanceState(element.wfId, ProcessingState.INVALID);
                    // TODO - what happens if even this fails?
                }
            }
        }
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

        if (cw.cid2ResponseMap != null) {
            for (Entry<String, String> e : cw.cid2ResponseMap.entrySet()) {
                if (e.getValue() != null) {
                    Response<?> r = serializer.deserializeResponse(e.getValue());
                    wf.putResponse(r);
                }
            }
        }
        return wf;
    }

    @Override
    public void registerCallback(RegisterCall rc, Acknowledge callback) throws Exception {
        logger.debug("registerCallback({})", rc);

        startupBlocker.pass();

        WorkflowInstance cw = new WorkflowInstance();
        cw.id = rc.workflow.getId();
        cw.state = ProcessingState.WAITING;
        cw.prio = rc.workflow.getPriority();
        cw.creationTS = rc.workflow.getCreationTS();
        cw.serializedWorkflow = serializer.serializeWorkflow(rc.workflow);
        cw.waitMode = rc.waitMode;
        cw.timeout = rc.timeout != null ? new Date(rc.timeout) : null;
        cw.ppoolId = rc.workflow.getProcessorPoolId();
        cw.cid2ResponseMap = new HashMap<String, String>();
        for (String cid : rc.correlationIds) {
            cw.cid2ResponseMap.put(cid, null);
        }

        cassandra.safeWorkflowInstance(cw);

        correlationIdMap.addCorrelationIds(rc.workflow.getId(), rc.correlationIds);

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
        for (String cid : rc.correlationIds) {
            Response<?> response = serializer.deserializeResponse(cassandra.readEarlyResponse(cid));
            if (response != null) {
                logger.debug("found early response with correlationId {} for workflow {} - doing notify...", cid, cw.id);
                notify(response, ACK);
                cassandra.deleteEarlyResponse(cid);
            }
        }

        callback.onSuccess();
    }

    @Override
    public void notify(Response<?> response, Acknowledge ack) throws Exception {
        logger.debug("notify({})", response);

        startupBlocker.pass();

        final String cid = response.getCorrelationId();
        final String wfId = correlationIdMap.getWorkflowId(cid);

        if (wfId != null) {
            // we have to take care of concurrent notifies for the same workflow instance
            // but we don't want to block everything - it's sufficient to block this workflows id (more or less...)
            synchronized (findMutex(wfId)) {
                // check if this workflow instance has just been dequeued - in this case we do not find the
                // correlationId any more...
                if (correlationIdMap.getWorkflowId(cid) != null) {
                    WorkflowInstance cw = cassandra.readCassandraWorkflow(wfId);
                    if (cw.cid2ResponseMap.containsKey(cid)) {
                        cw.cid2ResponseMap.put(cid, serializer.serializeResponse(response));
                    }
                    final boolean enqueue = cw.state == ProcessingState.WAITING && (cw.waitMode == WaitMode.FIRST || cw.waitMode == WaitMode.ALL && cw.cid2ResponseMap.size() == 1 || cw.waitMode == WaitMode.ALL && allResponsesAvailable(cw));

                    if (enqueue) {
                        cw.state = ProcessingState.ENQUEUED;
                    }

                    cassandra.safeWorkflowInstance(cw);

                    if (enqueue) {
                        final Queue<QueueElement> queue = findQueue(cw.ppoolId);
                        queue.add(new QueueElement(cw.id, cw.prio));
                    }

                    ack.onSuccess();

                    return;
                }

            }
        }
        handleEarlyResponse(response, ack);
    }

    private void handleEarlyResponse(Response<?> response, Acknowledge ack) throws Exception {
        synchronized (currentlyProcessingEarlyResponses) {
            currentlyProcessingEarlyResponses.add(response.getCorrelationId());
        }
        cassandra.safeEarlyResponse(response.getCorrelationId(), serializer.serializeResponse(response));

        // TODO mit Futures arbeiten -
        synchronized (currentlyProcessingEarlyResponses) {
            currentlyProcessingEarlyResponses.remove(response.getCorrelationId());
            currentlyProcessingEarlyResponses.notifyAll();
        }
        ack.onSuccess();
    }

    private boolean allResponsesAvailable(WorkflowInstance cw) {
        for (Entry<String, String> e : cw.cid2ResponseMap.entrySet()) {
            if (e.getValue() == null)
                return false;
        }
        return true;
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
            cassandra.initialize(new HybridDBStorageAccessor() {
                @Override
                public void registerCorrelationId(String correlationId, String wfId) {
                    this.registerCorrelationId(correlationId, wfId);
                }

                @Override
                public void enqueue(String wfId, String ppoolId, int prio) {
                    this.enqueue(wfId, ppoolId, prio);
                }
            });
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

            cassandra.updateWorkflowInstanceState(w.getId(), ProcessingState.ERROR);
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

        WorkflowInstance cw = cassandra.readCassandraWorkflow(workflowInstanceId);
        if (cw == null)
            throw new CopperRuntimeException("No workflow found with id " + workflowInstanceId);
        if (cw.state != ProcessingState.ERROR)
            throw new CopperRuntimeException("Workflow found with id " + workflowInstanceId + " is not in state ERROR");
        enqueue(cw.id, cw.ppoolId, cw.prio);
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
    public Workflow<?> read(String workflowInstanceId) throws Exception {
        return convert2workflow(cassandra.readCassandraWorkflow(workflowInstanceId));
    }

    private void enqueue(String wfId, String ppoolId, int prio) {
        findQueue(ppoolId).add(new QueueElement(wfId, prio));
    }

    private void registerCorrelationId(String correlationId, String wfId) {
        correlationIdMap.addCorrelationId(wfId, correlationId);
    }

    Object findMutex(String id) {
        long hash = id.hashCode();
        hash = Math.abs(hash);
        int x = (int) (hash % mutexArray.length);
        return mutexArray[x];
    }
}
