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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.copperengine.core.CopperException;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.DependencyInjector;
import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.EngineState;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowDescription;
import org.copperengine.core.WorkflowFactory;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.monitoring.NullRuntimeStatisticsCollector;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.util.EventCounter;
import org.copperengine.core.util.Blocker;
import org.copperengine.management.ProcessingEngineMXBean;
import org.copperengine.management.WorkflowRepositoryMXBean;
import org.copperengine.management.model.EngineActivity;
import org.copperengine.management.model.HalfOpenTimeInterval;
import org.copperengine.management.model.WorkflowClassInfo;
import org.copperengine.management.model.WorkflowInfo;
import org.copperengine.management.model.WorkflowInstanceFilter;

/**
 * Abstract base implementation of the COPPER {@link ProcessingEngine} interface.
 * 
 * @author austermann
 */
public abstract class AbstractProcessingEngine implements ProcessingEngine, ProcessingEngineMXBean {

    private IdFactory idFactory = new AtomicLongIdFactory();
    protected WorkflowRepository wfRepository;
    protected volatile EngineState engineState = EngineState.RAW;
    protected Blocker startupBlocker = new Blocker(true);
    private List<Runnable> shutdownObserver = new ArrayList<Runnable>();
    private EngineIdProvider engineIdProvider = new EngineIdProviderBean("default");
    protected RuntimeStatisticsCollector statisticsCollector = new NullRuntimeStatisticsCollector();
    protected DependencyInjector dependencyInjector;
    protected Date startupTS;
    private final AtomicLong lastActivityTS = new AtomicLong(System.currentTimeMillis());
    private final EventCounter startedWorkflowInstances = new EventCounter(24*60);

    public void setStatisticsCollector(RuntimeStatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
    }

    public RuntimeStatisticsCollector getStatisticsCollector() {
        return statisticsCollector;
    }

    @Override
    public String getStatisticsCollectorType() {
        return (statisticsCollector != null) ? statisticsCollector.getClass().getSimpleName() : "UNKNOWN";
    }

    public void setDependencyInjector(DependencyInjector dependencyInjector) {
        if (dependencyInjector == null)
            throw new NullPointerException();
        this.dependencyInjector = dependencyInjector;
    }

    @Override
    public String getDependencyInjectorType() {
        return (dependencyInjector != null) ? dependencyInjector.getType() : "UNKNOWN";
    }

    @Override
    public EngineState getEngineState() {
        return engineState;
    }

    public void setEngineIdProvider(EngineIdProvider engineIdProvider) {
        this.engineIdProvider = engineIdProvider;
    }

    @Override
    public String getEngineId() {
        return engineIdProvider.getEngineId();
    }

    public final void setIdFactory(IdFactory idFactory) {
        this.idFactory = idFactory;
    }

    @Override
    public final String createUUID() {
        return idFactory.createId();
    }

    public void setWfRepository(WorkflowRepository wfRepository) {
        this.wfRepository = wfRepository;
    }

    public WorkflowRepository getWfRepository() {
        return wfRepository;
    }

    public final <E> WorkflowFactory<E> createWorkflowFactory(String classname) throws ClassNotFoundException {
        try {
            startupBlocker.pass();
        } catch (InterruptedException e) {
            // ignore
        }

        return wfRepository.createWorkflowFactory(classname);
    }

    @Override
    public synchronized void addShutdownObserver(Runnable observer) {
        shutdownObserver.add(observer);
    }
    
    @Override
    public void startup() {
        startupTS = new Date();
    }

    @Override
    public void shutdown() throws CopperRuntimeException {
        for (Runnable observer : shutdownObserver) {
            observer.run();
        }
        startupTS = null;
    }

    protected WorkflowInfo convert2Wfi(Workflow<?> wf) {
        if (wf == null)
            return null;

        WorkflowClassInfo wfci = new WorkflowClassInfo();
        wfci.setClassname(wf.getClass().getName());
        WorkflowDescription wfd = wf.getClass().getAnnotation(WorkflowDescription.class);
        if (wfd != null) {
            wfci.setAlias(wfd.alias());
            wfci.setMajorVersion(wfd.majorVersion());
            wfci.setMinorVersion(wfd.minorVersion());
            wfci.setPatchLevel(wfd.patchLevelVersion());
        }

        WorkflowInfo wfi = new WorkflowInfo();
        wfi.setWorkflowClassInfo(wfci);
        wfi.setId(wf.getId());
        wfi.setPriority(wf.getPriority());
        wfi.setProcessorPoolId(wf.getProcessorPoolId());
        if (wf.getProcessingState() != null) {
            wfi.setState(wf.getProcessingState().name());
        }
        wfi.setTimeout(wf.getTimeoutTS());
        wfi.setDataAsString(wf.prettyPrintData());
        wfi.setLastWaitStackTrace(wf.getLastWaitStackTrace());
        wfi.setCreationTS(wf.getCreationTS());
        wfi.setLastModTS(wf.getLastActivityTS());
        return wfi;
    }

    protected abstract String run(Workflow<?> w) throws CopperException;

    protected abstract void run(List<Workflow<?>> w) throws CopperException;

    @Override
    public String run(String wfname, Object data) throws CopperException {
        try {
            Workflow<Object> wf = createWorkflowFactory(wfname).newInstance();
            wf.setData(data);
            return run(wf);
        } catch (CopperException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperException("run failed", e);
        }
    }

    @Override
    public String run(WorkflowInstanceDescr<?> wfInstanceDescr) throws CopperException {
        try {
            return run(createWorkflowInstance(wfInstanceDescr));
        } catch (CopperException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperException("run failed", e);
        }
    }

    protected Workflow<Object> createWorkflowInstance(WorkflowInstanceDescr<?> wfInstanceDescr) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            startupBlocker.pass();
        } catch (InterruptedException e) {
            // ignore
        }

        final Workflow<Object> wf = wfRepository.createWorkflowFactory(wfInstanceDescr.getWfName(), wfInstanceDescr.getVersion()).newInstance();
        if (wfInstanceDescr.getData() != null)
            wf.setData(wfInstanceDescr.getData());
        if (wfInstanceDescr.getId() != null)
            wf.setId(wfInstanceDescr.getId());
        if (wfInstanceDescr.getPriority() != null)
            wf.setPriority(wfInstanceDescr.getPriority());
        if (wfInstanceDescr.getProcessorPoolId() != null)
            wf.setProcessorPoolId(wfInstanceDescr.getProcessorPoolId());
        return wf;
    }

    @Override
    public void runBatch(List<WorkflowInstanceDescr<?>> wfInstanceDescr) throws CopperException {
        try {
            List<Workflow<?>> wfList = new ArrayList<Workflow<?>>(wfInstanceDescr.size());
            for (WorkflowInstanceDescr<?> wfInsDescr : wfInstanceDescr) {
                wfList.add(createWorkflowInstance(wfInsDescr));
            }
            run(wfList);
        } catch (CopperException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperException("run failed", e);
        }
    }

    @Override
    public WorkflowRepositoryMXBean getWorkflowRepository() {
        return (WorkflowRepositoryMXBean) ((this.wfRepository instanceof WorkflowRepositoryMXBean) ? this.wfRepository : null);
    }

    public void injectDependencies(Workflow<?> wf) {
        wf.setEngine(this);
        dependencyInjector.inject(wf);
        trackActivity();
    }

    
    protected void trackActivity() {
        final long now = System.currentTimeMillis();
        if (now > lastActivityTS.get()) {
            lastActivityTS.set(now);
        }
    }
    
    protected void trackWfiStarted() {
        trackActivity();
        startedWorkflowInstances.countEvent();
    }
    
    @Override
    public EngineActivity queryEngineActivity(int minutesInHistory) {
        long countWfiLastNMinutes = startedWorkflowInstances.getNumberOfEvents(minutesInHistory);
        return new EngineActivity(new Date(this.lastActivityTS.get()), startupTS, countWfiLastNMinutes);
    }    
    
    protected List<WorkflowInfo> filter(final WorkflowInstanceFilter filter, Collection<Workflow<?>> workflowInstances) {
        int offset = filter.getOffset();
        final List<WorkflowInfo> resultList = new ArrayList<>();
        for(Workflow<?> wf : workflowInstances) {
            if (filter.getProcessorPoolId() != null && !filter.getProcessorPoolId().equals(wf.getProcessorPoolId()))
                continue;
            if (filter.getStates() != null && !filter.getStates().contains(wf.getProcessingState().name()))
                continue;
            if (filter.getWorkflowClassname() != null && !filter.getWorkflowClassname().equals(wf.getClass().getName()))
                continue;
            if (filter.getCreationTS() != null && !isWithin(filter.getCreationTS(), wf.getCreationTS()))
                continue;
            if (filter.getLastModTS() != null && !isWithin(filter.getLastModTS(), wf.getLastActivityTS()))
                continue;

            final WorkflowInfo x = convert2Wfi(wf);

            // data may has changed during conversion - so we filter it again
            if (filter.getProcessorPoolId() != null && !filter.getProcessorPoolId().equals(x.getProcessorPoolId()))
                continue;
            if (filter.getStates() != null && !filter.getStates().contains(x.getState()))
                continue;
            if (filter.getLastModTS() != null && !isWithin(filter.getLastModTS(), x.getLastModTS()))
                continue;

            if (offset > 0) {
                offset--;
            } else {
                resultList.add(x);

                if (resultList.size() >= filter.getMax())
                    break;
            }
        }

        return resultList;
    }

    protected long count(final WorkflowInstanceFilter filter, Collection<Workflow<?>> workflowInstances) {
        return workflowInstances.stream().filter((wf) -> {
            if (filter.getProcessorPoolId() != null && !filter.getProcessorPoolId().equals(wf.getProcessorPoolId()))
                return false;
            if (filter.getStates() != null && !filter.getStates().contains(wf.getProcessingState().name()))
                return false;
            if (filter.getWorkflowClassname() != null && !filter.getWorkflowClassname().equals(wf.getClass().getName()))
                return false;
            if (filter.getCreationTS() != null && !isWithin(filter.getCreationTS(), wf.getCreationTS()))
                return false;
            if (filter.getLastModTS() != null && !isWithin(filter.getLastModTS(), wf.getLastActivityTS()))
                return false;

            final WorkflowInfo x = convert2Wfi(wf);

            // data may has changed during conversion - so we filter it again
            if (filter.getProcessorPoolId() != null && !filter.getProcessorPoolId().equals(x.getProcessorPoolId()))
                return false;
            if (filter.getStates() != null && !filter.getStates().contains(x.getState()))
                return false;
            if (filter.getLastModTS() != null && !isWithin(filter.getLastModTS(), x.getLastModTS()))
                return false;

            return true;
        }).count();
    }

    private boolean isWithin(HalfOpenTimeInterval interval, Date ts) {
        return interval.getFrom().getTime() <= ts.getTime() && ts.getTime() < interval.getTo().getTime();
    }    
}
