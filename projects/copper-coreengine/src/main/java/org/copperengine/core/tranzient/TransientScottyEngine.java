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
package org.copperengine.core.tranzient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.CopperException;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.EngineState;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.ProcessingState;
import org.copperengine.core.Response;
import org.copperengine.core.WaitHook;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.common.AbstractProcessingEngine;
import org.copperengine.core.common.ProcessorPool;
import org.copperengine.core.common.ProcessorPoolManager;
import org.copperengine.core.common.TicketPoolManager;
import org.copperengine.core.internal.WorkflowAccessor;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.management.ProcessingEngineMXBean;
import org.copperengine.management.ProcessorPoolMXBean;
import org.copperengine.management.model.EngineType;
import org.copperengine.management.model.WorkflowInfo;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transient implementation of a COPPER {@link ProcessingEngine}.
 * A transient engine may run instances of {@link Workflow} or {@link PersistentWorkflow}.
 * Anyhow, all workflow instances will only reside in the local JVM heap.
 * 
 * @author austermann
 */
public class TransientScottyEngine extends AbstractProcessingEngine implements ProcessingEngine, ProcessingEngineMXBean {

    private static final Logger logger = LoggerFactory.getLogger(TransientScottyEngine.class);

    private final Map<String, CorrelationSet> correlationMap = new HashMap<String, CorrelationSet>(50000);
    private final Map<String, Workflow<?>> workflowMap = new ConcurrentHashMap<String, Workflow<?>>(50000);
    private ProcessorPoolManager<TransientProcessorPool> poolManager;
    private TimeoutManager timeoutManager;
    private EarlyResponseContainer earlyResponseContainer;
    private TicketPoolManager ticketPoolManager;
    private final AtomicLong sequenceIdFactory = new AtomicLong(System.currentTimeMillis() * 10000L);

    public void setTicketPoolManager(TicketPoolManager ticketPoolManager) {
        if (ticketPoolManager == null)
            throw new NullPointerException();
        this.ticketPoolManager = ticketPoolManager;
    }

    public void setTimeoutManager(TimeoutManager timeoutManager) {
        if (timeoutManager == null)
            throw new NullPointerException();
        this.timeoutManager = timeoutManager;
    }

    public void setPoolManager(ProcessorPoolManager<TransientProcessorPool> poolManager) {
        if (poolManager == null)
            throw new NullPointerException();
        this.poolManager = poolManager;
    }

    public void setEarlyResponseContainer(EarlyResponseContainer earlyResponseContainer) {
        if (earlyResponseContainer == null)
            throw new NullPointerException();
        this.earlyResponseContainer = earlyResponseContainer;
    }

    @Override
    public void notify(Response<?> response, Acknowledge ack) {
        logger.debug("notify({})", response);
        try {
            if (response == null)
                throw new NullPointerException();

            if (response.getSequenceId() == null) {
                response.setSequenceId(sequenceIdFactory.incrementAndGet());
            }

            try {
                startupBlocker.pass();
            } catch (InterruptedException e) {
                // ignore
            }

            synchronized (correlationMap) {
                final CorrelationSet cs = correlationMap.get(response.getCorrelationId());
                if (cs == null) {
                    if (response.isEarlyResponseHandling()) {
                        earlyResponseContainer.put(response);
                    }
                    ack.onSuccess();
                    return;
                }
                final Workflow<?> wf = workflowMap.get(cs.getWorkflowId());
                if (wf == null) {
                    logger.error("Workflow with id " + cs.getWorkflowId() + " not found");
                    ack.onException(new CopperException("Workflow with id " + cs.getWorkflowId() + " not found"));
                    return;
                }
                cs.getMissingCorrelationIds().remove(response.getCorrelationId());
                if (cs.getTimeoutTS() != null && !response.isTimeout())
                    timeoutManager.unregisterTimeout(cs.getTimeoutTS(), response.getCorrelationId());
                wf.putResponse(response);

                boolean doEnqueue = false;
                if (cs.getMode() == WaitMode.FIRST) {
                    if (!cs.getMissingCorrelationIds().isEmpty() && cs.getTimeoutTS() != null && !response.isTimeout()) {
                        timeoutManager.unregisterTimeout(cs.getTimeoutTS(), cs.getMissingCorrelationIds());
                    }
                    doEnqueue = true;
                }

                if (cs.getMissingCorrelationIds().isEmpty()) {
                    doEnqueue = true;
                }

                if (doEnqueue) {
                    for (String correlationId : cs.getCorrelationIds()) {
                        correlationMap.remove(correlationId);
                    }
                    enqueue(wf);
                }
            }
            ack.onSuccess();
        } catch (RuntimeException e) {
            ack.onException(e);
            throw e;
        }
    }

    @Override
    protected String run(Workflow<?> w) throws DuplicateIdException {
        try {
            startupBlocker.pass();
        } catch (InterruptedException e) {
            // ignore
        }

        ticketPoolManager.obtain(w);
        try {
            boolean newId = false;
            if (w.getId() == null) {
                w.setId(createUUID());
                newId = true;
            }

            if (w.getProcessorPoolId() == null) {
                w.setProcessorPoolId(TransientProcessorPool.DEFAULT_POOL_ID);
            }
            synchronized (workflowMap) {
                if (!newId && workflowMap.containsKey(w.getId()))
                    throw new DuplicateIdException("engine already contains a workflow with id '" + w.getId() + "'");
                workflowMap.put(w.getId(), w);
            }
            injectDependencies(w);
            enqueue(w);
            trackWfiStarted();
            return w.getId();
        } catch (DuplicateIdException e) {
            ticketPoolManager.release(w);
            throw e;
        } catch (Exception e) {
            String message = "run/enqeue of workflow with id '" + w.getId() + "' failed.";
            logger.warn(message);
            workflowMap.remove(w.getId());
            ticketPoolManager.release(w);
            throw new CopperRuntimeException(message, e);
        }
    }

    private void enqueue(Workflow<?> w) {
        TransientProcessorPool pool = poolManager.getProcessorPool(w.getProcessorPoolId());
        if (pool == null) {
            logger.error("Unable to find processor pool " + w.getProcessorPoolId() + " - using default processor pool");
            pool = poolManager.getProcessorPool(TransientProcessorPool.DEFAULT_POOL_ID);
        }
        pool.enqueue(w);
    }

    @Override
    public synchronized void shutdown() {
        if (engineState != EngineState.STARTED)
            throw new IllegalStateException();

        logger.info("Engine is shutting down...");
        engineState = EngineState.SHUTTING_DOWN;
        wfRepository.shutdown();
        timeoutManager.shutdown();
        earlyResponseContainer.shutdown();
        poolManager.shutdown();
        super.shutdown();
        logger.info("Engine is stopped");
        engineState = EngineState.STOPPED;
    }

    @Override
    public synchronized void startup() {
        if (engineState != EngineState.RAW)
            throw new IllegalStateException();

        logger.info("Engine is starting up...");
        super.startup();
        wfRepository.start();
        timeoutManager.setEngine(this);
        poolManager.setEngine(this);
        timeoutManager.startup();
        earlyResponseContainer.startup();
        poolManager.startup();
        engineState = EngineState.STARTED;
        logger.info("Engine is running");
        startupBlocker.unblock();
    }

    @Override
    public void registerCallbacks(Workflow<?> w, WaitMode mode, long timeoutMsec, String... correlationIds) {
        if (logger.isDebugEnabled())
            logger.debug("registerCallbacks(" + w + ", " + mode + ", " + timeoutMsec + ", " + Arrays.asList(correlationIds) + ")");
        if (correlationIds.length == 0)
            throw new IllegalArgumentException("No correlationids given");
        
        boolean doEnqueue = false;
        CorrelationSet cs = new CorrelationSet(w, correlationIds, mode, timeoutMsec > 0 ? System.currentTimeMillis() + timeoutMsec : null);
        synchronized (correlationMap) {
            for (String cid : correlationIds) {
                List<Response<?>> earlyResponses = earlyResponseContainer.get(cid);
                if (earlyResponses != null && !earlyResponses.isEmpty()) {
                    for (Response<?> earlyResponse : earlyResponses) {
                        w.putResponse(earlyResponse);
                    }
                    cs.getMissingCorrelationIds().remove(cid);
                }
            }
            if (cs.getMissingCorrelationIds().isEmpty() || (cs.getMissingCorrelationIds().size() < correlationIds.length && mode == WaitMode.FIRST)) {
                doEnqueue = true;
            } else {
                for (String cid : cs.getCorrelationIds()) {
                    correlationMap.put(cid, cs);
                }
                if (cs.getTimeoutTS() != null) {
                    if (mode == WaitMode.FIRST)
                        timeoutManager.registerTimeout(cs.getTimeoutTS().longValue(), cs.getMissingCorrelationIds().get(0));
                    else
                        timeoutManager.registerTimeout(cs.getTimeoutTS().longValue(), cs.getMissingCorrelationIds());
                }
            }
        }
        if (doEnqueue) {
            enqueue(w);
        } else {
            WorkflowAccessor.setProcessingState(w, ProcessingState.WAITING);
            WorkflowAccessor.setTimeoutTS(w, cs.getTimeoutTS() != null ? new Date(cs.getTimeoutTS()) : null);
        }
    }

    /**
     * For internal use only!!
     * 
     * @param id
     *        workflow instance id
     */
    public void removeWorkflow(String id) {
        final Workflow<?> wf = workflowMap.remove(id);
        if (wf != null) {
            WorkflowAccessor.setProcessingState(wf, ProcessingState.FINISHED);
            ticketPoolManager.release(wf);
            statisticsCollector.submit(getEngineId() + "." + wf.getClass().getSimpleName() + ".ExecutionTime", 1, System.currentTimeMillis() - wf.getCreationTS().getTime(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void run(List<Workflow<?>> list) {
        for (Workflow<?> w : list) {
            run(w);
        }
    }

    @Override
    public String getState() {
        return getEngineState().name();
    }

    @Override
    public List<WorkflowInfo> queryWorkflowInstances() {
        List<WorkflowInfo> rv = new ArrayList<WorkflowInfo>();
        for (Workflow<?> wf : workflowMap.values()) {
            WorkflowInfo wfi = convert2Wfi(wf);
            rv.add(wfi);
        }
        return rv;
    }

    @Override
    public WorkflowInfo queryWorkflowInstance(String id) {
        return convert2Wfi(workflowMap.get(id));
    }

    @Override
    public int getNumberOfWorkflowInstances() {
        return workflowMap.size();
    }

    @Override
    public void addWaitHook(String wfInstanceId, WaitHook waitHook) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ProcessorPoolMXBean> getProcessorPools() {
        final List<ProcessorPoolMXBean> result = new ArrayList<ProcessorPoolMXBean>();
        for (ProcessorPool pp : poolManager.processorPools()) {
            if (pp instanceof ProcessorPoolMXBean) {
                result.add((ProcessorPoolMXBean) pp);
            }
        }
        return result;
    }

    @Override
    public EngineType getEngineType() {
        return EngineType.tranzient;
    }

    @Override
    public List<WorkflowInfo> queryActiveWorkflowInstances(final String className, final int max) {
        final List<WorkflowInfo> rv = new ArrayList<WorkflowInfo>();
        for (Workflow<?> wf : workflowMap.values()) {
            if (rv.size() >= max) {
                break;
            }
            if (wf.getClass().getName().equals(className)) {
                rv.add(convert2Wfi(wf));
            }
        }
        return rv;
    }

    @Override
    public WorkflowInfo queryActiveWorkflowInstance(final String id) {
        // Same as this one for transient engine
        return queryWorkflowInstance(id);
    }

    @Override
    public List<String> getWorkflowInstanceStates() {
        return Arrays.asList(ProcessingState.ENQUEUED.name(), ProcessingState.RUNNING.name(), ProcessingState.WAITING.name());
    }

    @Override
    public List<WorkflowInfo> queryWorkflowInstances(final WorkflowInstanceFilter filter) {
        return filter(filter, workflowMap.values());
    }

    @Override
    public long countWorkflowInstances(final WorkflowInstanceFilter filter) {
        WorkflowInstanceFilter noLimitfilter = new WorkflowInstanceFilter(filter.getStates(), filter.getLastModTS(), filter.getCreationTS(), filter.getProcessorPoolId(), filter.getWorkflowClassname(), Integer.MAX_VALUE, 0);
        return filter(noLimitfilter, workflowMap.values()).size();
    }
}
