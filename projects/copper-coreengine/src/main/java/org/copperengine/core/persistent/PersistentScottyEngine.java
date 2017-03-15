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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.CopperException;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.EngineState;
import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.ProcessingState;
import org.copperengine.core.Response;
import org.copperengine.core.WaitHook;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.common.AbstractProcessingEngine;
import org.copperengine.core.common.ProcessorPool;
import org.copperengine.core.common.ProcessorPoolManager;
import org.copperengine.core.internal.WorkflowAccessor;
import org.copperengine.management.DBStorageMXBean;
import org.copperengine.management.PersistentProcessingEngineMXBean;
import org.copperengine.management.ProcessorPoolMXBean;
import org.copperengine.management.model.EngineType;
import org.copperengine.management.model.WorkflowInfo;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * COPPER processing engine that offers persistent workflow processing.
 *
 * @author austermann
 */
public class PersistentScottyEngine extends AbstractProcessingEngine implements PersistentProcessingEngine, PersistentProcessingEngineMXBean {

    private static final Logger logger = LoggerFactory.getLogger(PersistentScottyEngine.class);

    private ScottyDBStorageInterface dbStorage;
    private ProcessorPoolManager<? extends PersistentProcessorPool> processorPoolManager;
    private final Map<String, Workflow<?>> workflowMap = new ConcurrentHashMap<String, Workflow<?>>();
    private final Map<String, List<WaitHook>> waitHookMap = new HashMap<String, List<WaitHook>>();
    private final AtomicLong sequenceIdFactory = new AtomicLong(System.currentTimeMillis() * 10000L);

    /**
     * @deprecated without effect - will be removed in future release
     */
    public void setNotifyProcessorPoolsOnResponse(boolean notifyProcessorPoolsOnResponse) {
    }

    public void setDbStorage(ScottyDBStorageInterface dbStorage) {
        this.dbStorage = dbStorage;
    }

    public ScottyDBStorageInterface getDbStorage() {
        return dbStorage;
    }

    public void setProcessorPoolManager(ProcessorPoolManager<? extends PersistentProcessorPool> processorPoolManager) {
        this.processorPoolManager = processorPoolManager;
    }

    @Override
    public void notify(Response<?> response, Acknowledge ack) {
        if (logger.isTraceEnabled())
            logger.trace("notify(" + response + ")");
        try {
            if (response.getResponseId() == null) {
                response.setResponseId(createUUID());
            }
            if (response.getSequenceId() == null) {
                response.setSequenceId(sequenceIdFactory.incrementAndGet());
            }
            startupBlocker.pass();
            dbStorage.notify(response, ack);
        } catch (Exception e) {
            throw new CopperRuntimeException("notify failed", e);
        }

    }

    @Override
    public synchronized void shutdown() {
        if (engineState != EngineState.STARTED) {
            logger.debug("engine is not running - shutdown aborted");
            return;
        }
        logger.info("Engine is shutting down...");
        engineState = EngineState.SHUTTING_DOWN;
        processorPoolManager.shutdown();
        dbStorage.shutdown();
        super.shutdown();
        logger.info("Engine is stopped");
        engineState = EngineState.STOPPED;
    }

    @Override
    public synchronized void startup() {
        if (engineState != EngineState.RAW)
            throw new IllegalStateException();
        try {
            logger.info("starting up...");
            super.startup();

            processorPoolManager.setEngine(this);

            wfRepository.start();
            dbStorage.startup();

            processorPoolManager.startup();
            startupBlocker.unblock();
            engineState = EngineState.STARTED;

            logger.info("Engine is running");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperRuntimeException("startup failed", e);
        }
    }

    @Override
    public void registerCallbacks(Workflow<?> w, WaitMode mode, long timeoutMsec, String... correlationIds) {
        if (logger.isTraceEnabled())
            logger.trace("registerCallbacks(" + w + ", " + mode + ", " + timeoutMsec + ", " + Arrays.asList(correlationIds) + ")");

        if (correlationIds.length == 0)
            throw new IllegalArgumentException("No correlationids given");
        PersistentWorkflow<?> pw = (PersistentWorkflow<?>) w;
        if (processorPoolManager.getProcessorPool(pw.getProcessorPoolId()) == null) {
            logger.error("Unkown processor pool '" + pw.getProcessorPoolId() + "' - using default pool instead");
            pw.setProcessorPoolId(PersistentProcessorPool.DEFAULT_POOL_ID);
        }
        pw.registerCall = new RegisterCall(w, mode, timeoutMsec > 0 ? timeoutMsec : null, correlationIds, getAndRemoveWaitHooks(pw));
        WorkflowAccessor.setTimeoutTS(pw, pw.registerCall.timeoutTS);
    }

    @Override
    protected String run(Workflow<?> wf) throws CopperException {
        return run(wf, null);
    }

    private void notifyProcessorPool(String ppoolId) {
        PersistentProcessorPool pp = processorPoolManager.getProcessorPool(ppoolId);
        if (pp == null) {
            pp = processorPoolManager.getProcessorPool(PersistentProcessorPool.DEFAULT_POOL_ID);
        }
        if (pp != null) {
            pp.doNotify();
        }
    }

    @Override
    public void run(List<Workflow<?>> list) throws CopperException {
        run(list, null);
    }

    /**
     * Enqueues the specified list of workflow instances into the engine for execution.
     *
     * @param list
     *        the list of workflow instances to run
     * @param con
     *        connection used for the inserting the workflow to the database
     * @throws CopperException
     *         if the engine can not run the workflow, e.g. in case of a unkown processor pool id
     */
    public void run(List<Workflow<?>> list, Connection con) throws CopperException {
        if (logger.isTraceEnabled()) {
            for (Workflow<?> w : list)
                logger.trace("run(" + w + ")");
        }
        try {
            startupBlocker.pass();

            Set<String> ppoolIds = new HashSet<String>();
            for (Workflow<?> wf : list) {
                if (!(wf instanceof PersistentWorkflow<?>)) {
                    throw new IllegalArgumentException(wf.getClass() + " is no instance of PersistentWorkflow");
                }
                if (wf.getId() == null) {
                    wf.setId(createUUID());
                }
                if (wf.getProcessorPoolId() == null) {
                    wf.setProcessorPoolId(PersistentProcessorPool.DEFAULT_POOL_ID);
                }

                if (processorPoolManager.getProcessorPool(wf.getProcessorPoolId()) == null) {
                    logger.error("Unkown processor pool '" + wf.getProcessorPoolId() + "' - using default pool instead");
                    wf.setProcessorPoolId(PersistentProcessorPool.DEFAULT_POOL_ID);
                }

                ppoolIds.add(wf.getProcessorPoolId());
            }
            dbStorage.insert(list, con);
            for (String ppoolId : ppoolIds) {
                notifyProcessorPool(ppoolId);
            }
            trackWfiStarted();
        } catch (RuntimeException e) {
            throw e;
        } catch (CopperException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperException("run failed", e);
        }
    }

    /**
     * Enqueues the specified workflow instance into the engine for execution.
     *
     * @param wf
     *        the workflow instance to run
     * @param con
     *        connection used for the inserting the workflow to the database
     * @throws CopperException
     *         if the engine can not run the workflow, e.g. in case of a unkown processor pool id
     */
    public String run(Workflow<?> wf, Connection con) throws CopperException {
        if (logger.isTraceEnabled())
            logger.trace("run(" + wf + ")");
        if (!(wf instanceof PersistentWorkflow<?>)) {
            throw new IllegalArgumentException(wf.getClass() + " is no instance of PersistentWorkflow");
        }
        try {
            startupBlocker.pass();

            if (wf.getId() == null) {
                wf.setId(createUUID());
            }
            if (wf.getProcessorPoolId() == null) {
                wf.setProcessorPoolId(PersistentProcessorPool.DEFAULT_POOL_ID);
            }
            if (processorPoolManager.getProcessorPool(wf.getProcessorPoolId()) == null) {
                logger.error("Unkown processor pool '" + wf.getProcessorPoolId() + "' - using default pool instead");
                wf.setProcessorPoolId(PersistentProcessorPool.DEFAULT_POOL_ID);
            }
            dbStorage.insert(wf, con);
            notifyProcessorPool(wf.getProcessorPoolId());
            trackWfiStarted();

            return wf.getId();
        } catch (RuntimeException e) {
            throw e;
        } catch (CopperException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperException("run failed", e);
        }

    }

    @Override
    public void restart(String workflowInstanceId) throws Exception {
        dbStorage.restart(workflowInstanceId);
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
        logger.info("queryWorkflowInstances returned " + rv.size() + " instance(s)");
        return rv;
    }

    @Override
    public WorkflowInfo queryWorkflowInstance(String id) {
        return convert2Wfi(workflowMap.get(id));
    }

    void register(Workflow<?> wf) {
        if (logger.isTraceEnabled())
            logger.trace("register(" + wf.getId() + ")");
        Workflow<?> existingWF = workflowMap.put(wf.getId(), wf);
        assert existingWF == null;
    }

    void unregister(Workflow<?> wf) {
        Workflow<?> existingWF = workflowMap.remove(wf.getId());
        assert existingWF != null;
        if (existingWF != null && existingWF.getProcessingState() == ProcessingState.FINISHED) {
            statisticsCollector.submit(getEngineId() + "." + wf.getClass().getSimpleName() + ".ExecutionTime", 1, System.currentTimeMillis() - wf.getCreationTS().getTime(), TimeUnit.MILLISECONDS);
        }
        getAndRemoveWaitHooks(wf); // Clean up...
    }

    @Override
    public int getNumberOfWorkflowInstances() {
        return workflowMap.size();
    }

    @Override
    public void restartAll() throws Exception {
        dbStorage.restartAll();
    }


    @Override
    public void deleteBroken(String workflowInstanceId) throws Exception {
        dbStorage.deleteBroken(workflowInstanceId);
    }

    @Override
    public String run(WorkflowInstanceDescr<?> wfInstanceDescr, Connection con) throws CopperException {
        try {
            return this.run(createWorkflowInstance(wfInstanceDescr), con);
        } catch (CopperException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperException("run failed", e);
        }
    }

    @Override
    public void runBatch(List<WorkflowInstanceDescr<?>> wfInstanceDescr, Connection con) throws CopperException {
        try {
            List<Workflow<?>> wfList = new ArrayList<Workflow<?>>(wfInstanceDescr.size());
            for (WorkflowInstanceDescr<?> wfInsDescr : wfInstanceDescr) {
                wfList.add(createWorkflowInstance(wfInsDescr));
            }
            run(wfList, con);
        } catch (CopperException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperException("run failed", e);
        }
    }

    @Override
    public void notify(Response<?> response, Connection c) throws CopperRuntimeException {
        final List<Response<?>> list = new ArrayList<Response<?>>(1);
        list.add(response);
        this.notify(list, c);
    }

    @Override
    public void notify(List<Response<?>> responses, Connection c) throws CopperRuntimeException {
        try {
            for (Response<?> r : responses) {
                if (r.getResponseId() == null) {
                    r.setResponseId(createUUID());
                }
                if (r.getSequenceId() == null) {
                    r.setSequenceId(sequenceIdFactory.incrementAndGet());
                }
            }
            dbStorage.notify(responses, c);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperRuntimeException(e);
        }
    }

    @Override
    public void addWaitHook(String wfInstanceId, WaitHook waitHook) {
        if (wfInstanceId == null)
            throw new NullPointerException();
        if (waitHook == null)
            throw new NullPointerException();

        synchronized (waitHookMap) {
            if (!workflowMap.containsKey(wfInstanceId)) {
                throw new CopperRuntimeException("Unkown workflow instance with id '" + wfInstanceId + "'");
            }
            List<WaitHook> l = waitHookMap.get(wfInstanceId);
            if (l == null) {
                l = new ArrayList<WaitHook>();
                waitHookMap.put(wfInstanceId, l);
            }
            l.add(waitHook);
        }
    }

    private List<WaitHook> getAndRemoveWaitHooks(Workflow<?> wf) {
        synchronized (waitHookMap) {
            List<WaitHook> l = waitHookMap.remove(wf.getId());
            return l == null ? Collections.<WaitHook>emptyList() : l;
        }
    }

    @Override
    public List<ProcessorPoolMXBean> getProcessorPools() {
        final List<ProcessorPoolMXBean> result = new ArrayList<ProcessorPoolMXBean>();
        for (ProcessorPool pp : processorPoolManager.processorPools()) {
            if (pp instanceof ProcessorPoolMXBean) {
                result.add((ProcessorPoolMXBean) pp);
            }
        }
        return result;
    }

    @Override
    public EngineType getEngineType() {
        return EngineType.persistent;
    }

    @Override
    public DBStorageMXBean getDBStorage() {
        return (DBStorageMXBean) (dbStorage instanceof DBStorageMXBean ? dbStorage : null);
    }

    @Override
    public List<WorkflowInfo> queryActiveWorkflowInstances(final String className, final int max) {
        List<WorkflowInfo> rv = new ArrayList<WorkflowInfo>();
        try {
            List<Workflow<?>> wfs = dbStorage.queryAllActive(className, max);
            for (Workflow<?> wf : wfs) {
                WorkflowInfo wfi = convert2Wfi(wf);
                rv.add(wfi);
            }
        } catch (Exception e) {
            logger.error("queryWorkflowInstances failed: " + e.getMessage(), e);
        }
        logger.info("queryWorkflowInstances returned " + rv.size() + " instance(s)");
        return rv;
    }

    @Override
    public WorkflowInfo queryActiveWorkflowInstance(final String id) {
        // first, get from map
        WorkflowInfo wfi = convert2Wfi(workflowMap.get(id));
        if (wfi == null) {
            // try get from db
            try {
                Workflow<?> wf = dbStorage.read(id);
                wfi = convert2Wfi(wf);
            } catch (Exception e) {
                logger.error("queryActiveWorkflowInstance failed: id=" + id + ", " + e.getMessage(), e);
            }
        }
        return wfi;
    }

    @Override
    public List<String> getWorkflowInstanceStates() {
        return Arrays.asList(ProcessingState.ENQUEUED.name(), ProcessingState.DEQUEUED.name(), ProcessingState.RUNNING.name(), ProcessingState.WAITING.name(), ProcessingState.FINISHED.name(), ProcessingState.ERROR.name(), ProcessingState.INVALID.name());
    }

    @Override
    public List<WorkflowInfo> queryWorkflowInstances(final WorkflowInstanceFilter filter) {
        try {
            final List<WorkflowInfo> rv = new ArrayList<WorkflowInfo>();
            if (filter.getState() != null && (filter.getState().equals(ProcessingState.RUNNING.name()) || filter.getState().equals(ProcessingState.DEQUEUED.name()))) {
                rv.addAll(filter(filter, workflowMap.values()));
            }
            else {
                final List<Workflow<?>> wfs = dbStorage.queryWorkflowInstances(filter);
                for (Workflow<?> wf : wfs) {
                    final Workflow<?> inMemoryWF = workflowMap.get(wf.getId());
                    final WorkflowInfo wfi = convert2Wfi(inMemoryWF == null ? wf : inMemoryWF);
                    rv.add(wfi);
                }
            }
            logger.info("queryWorkflowInstances returned " + rv.size() + " instance(s)");
            return rv;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected WorkflowInfo convert2Wfi(Workflow<?> wf) {
        WorkflowInfo wfi = super.convert2Wfi(wf);
        ErrorData errorData = ((PersistentWorkflow<?>)wf).getErrorData();
        if (errorData != null) {
            org.copperengine.management.model.ErrorData x = new org.copperengine.management.model.ErrorData();
            x.setErrorTS(errorData.getErrorTS());
            x.setExceptionStackTrace(errorData.getExceptionStackTrace());
            wfi.setErrorData(x);
        }
        return wfi;
    }

}
