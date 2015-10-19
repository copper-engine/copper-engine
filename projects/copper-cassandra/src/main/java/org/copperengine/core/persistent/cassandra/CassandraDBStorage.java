package org.copperengine.core.persistent.cassandra;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.internal.WorkflowAccessor;
import org.copperengine.core.persistent.RegisterCall;
import org.copperengine.core.persistent.ScottyDBStorageInterface;
import org.copperengine.core.persistent.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraDBStorage implements ScottyDBStorageInterface, InternalStorageAccessor {

    private static final Logger logger = LoggerFactory.getLogger(CassandraDBStorage.class);
    private static final Acknowledge.BestEffortAcknowledge ACK = new Acknowledge.BestEffortAcknowledge();

    private final Map<String, Queue<QueueElement>> ppoolId2queueMap;
    private final Map<String, String> correlationId2wfIdMap;
    private final Serializer serializer;
    private final WorkflowRepository wfRepo;
    private final Cassandra cassandra;

    public CassandraDBStorage(Serializer serializer, WorkflowRepository wfRepo, Cassandra cassandra) {
        this.ppoolId2queueMap = new ConcurrentHashMap<>();
        this.correlationId2wfIdMap = new ConcurrentHashMap<>();
        this.serializer = serializer;
        this.wfRepo = wfRepo;
        this.cassandra = cassandra;
    }

    @Override
    public void insert(Workflow<?> wf, Acknowledge ack) throws DuplicateIdException, Exception {
        if (wf == null)
            throw new NullPointerException();

        CassandraWorkflow cw = new CassandraWorkflow();
        cw.id = wf.getId();
        cw.serializedWorkflow = serializer.serializeWorkflow(wf);
        cw.ppoolId = wf.getProcessorPoolId();
        cw.prio = wf.getPriority();
        cw.creationTS = wf.getCreationTS();

        cassandra.safeWorkflowInstance(cw);

        final Queue<QueueElement> queue = findQueue(wf.getProcessorPoolId());
        queue.add(new QueueElement(wf.getId(), wf.getPriority()));

        if (ack != null)
            ack.onSuccess();

    }

    private Queue<QueueElement> findQueue(final String ppoolId) {
        synchronized (ppoolId2queueMap) {
            Queue<QueueElement> queue = ppoolId2queueMap.get(ppoolId);
            if (queue == null) {
                queue = new PriorityQueue<QueueElement>(100, new QueueElementComparator());
                ppoolId2queueMap.put(ppoolId, queue);
            }
            return queue;
        }
    }

    @Override
    public void insert(List<Workflow<?>> wfs, Acknowledge ack) throws DuplicateIdException, Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(Workflow<?> wf, Connection con) throws DuplicateIdException, Exception {
        insert(wf, ACK);
    }

    @Override
    public void insert(List<Workflow<?>> wfs, Connection con) throws DuplicateIdException, Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void finish(Workflow<?> w, Acknowledge callback) {
        try {
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
        final Queue<QueueElement> queue = findQueue(ppoolId);
        if (queue.isEmpty())
            return Collections.emptyList();

        final List<Workflow<?>> wfList = new ArrayList<>(max);
        while (wfList.size() < max) {
            final QueueElement element = queue.poll();
            if (element == null)
                break;

            CassandraWorkflow cw = cassandra.readCassandraWorkflow(element.wfId);

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

            wfList.add(wf);
        }
        return wfList;
    }

    @Override
    public void notify(Response<?> response, Acknowledge ack) throws Exception {
        // TODO take care of concurrent notifies for the same wf

        final String cid = response.getCorrelationId();
        final String wfId = correlationId2wfIdMap.get(cid);
        if (wfId == null) {
            // TODO handle early response
        }
        CassandraWorkflow cw = cassandra.readCassandraWorkflow(wfId);
        if (cw.cid2ResponseMap.containsKey(cid)) {
            cw.cid2ResponseMap.put(cid, serializer.serializeResponse(response));
        }
        cassandra.safeWorkflowInstance(cw);

        if (cw.waitMode == WaitMode.FIRST || cw.waitMode == WaitMode.ALL && cw.cid2ResponseMap.size() == 1 || cw.waitMode == WaitMode.ALL && allResponsesAvailable(cw)) {
            Queue<QueueElement> queue = findQueue(cw.ppoolId);
            queue.add(new QueueElement(cw.id, cw.prio));
        }

        ack.onSuccess();
    }

    private boolean allResponsesAvailable(CassandraWorkflow cw) {
        for (Entry<String, String> e : cw.cid2ResponseMap.entrySet()) {
            if (e.getValue() == null)
                return false;
        }
        return true;
    }

    @Override
    public void notify(List<Response<?>> response, Acknowledge ack) throws Exception {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void notify(List<Response<?>> responses, Connection c) throws Exception {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void registerCallback(RegisterCall rc, Acknowledge callback) throws Exception {
        CassandraWorkflow cw = new CassandraWorkflow();
        cw.id = rc.workflow.getId();
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

        for (String cid : rc.correlationIds) {
            correlationId2wfIdMap.put(cid, rc.workflow.getId());
        }
        callback.onSuccess();
    }

    @Override
    public void startup() {
        try {
            cassandra.initialize(this);
        } catch (RuntimeException e) {
            logger.error("startup failed", e);
            throw e;

        } catch (Exception e) {
            logger.error("startup failed", e);
            throw new CopperRuntimeException("startup failed", e);
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void error(Workflow<?> w, Throwable t, Acknowledge callback) {
        finish(w, callback); // TODO
    }

    @Override
    public void restart(String workflowInstanceId) throws Exception {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void setRemoveWhenFinished(boolean removeWhenFinished) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void restartAll() throws Exception {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Workflow<?> read(String workflowInstanceId) throws Exception {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void enqueue(String wfId, String ppoolId, int prio) {
        findQueue(ppoolId).add(new QueueElement(wfId, prio));
    }

    @Override
    public void registerCorrelationId(String correlationId, String wfId) {
        correlationId2wfIdMap.put(correlationId, wfId);
    }

}
