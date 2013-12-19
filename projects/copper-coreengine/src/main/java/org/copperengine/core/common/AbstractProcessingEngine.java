/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
import java.util.List;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.CopperException;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.DependencyInjector;
import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.EngineState;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowFactory;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.monitoring.NullRuntimeStatisticsCollector;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.util.Blocker;
import org.copperengine.management.ProcessingEngineMXBean;
import org.copperengine.management.WorkflowRepositoryMXBean;
import org.copperengine.management.model.WorkflowInfo;


/**
 * Abstract base implementation of the COPPER {@link ProcessingEngine} interface.
 * 
 * @author austermann
 *
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
		this.dependencyInjector = dependencyInjector;
	}

	public DependencyInjector getDependencyInjector() {
		return dependencyInjector;
	}

	@Override
	public String getDependencyInjectorType() {
		return (dependencyInjector != null) ? dependencyInjector.getType() : "UNKNOWN";
	}
	
	public EngineState getEngineState() {
		return engineState;
	}
	
	public void setEngineIdProvider(EngineIdProvider engineIdProvider) {
		this.engineIdProvider = engineIdProvider;
	}
	
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
		} 
		catch (InterruptedException e) {
			// ignore
		}
		
		return wfRepository.createWorkflowFactory(classname);
	}

	
	public synchronized void addShutdownObserver(Runnable observer) {
		shutdownObserver.add(observer);
	}
	
	@Override
	public void shutdown() throws CopperRuntimeException {
		for (Runnable observer : shutdownObserver) {
			observer.run();
		}
	}
	
	protected WorkflowInfo convert2Wfi(Workflow<?> wf) {
		if (wf == null) 
			return null;
		WorkflowInfo wfi = new WorkflowInfo();
		wfi.setId(wf.getId());
		wfi.setPriority(wf.getPriority());
		wfi.setProcessorPoolId(wf.getProcessorPoolId());
		wfi.setState(wf.getProcessingState().name());
		wfi.setTimeout(null); // TODO
		return wfi;
	}	

	protected abstract void run(Workflow<?> w) throws CopperException;

	protected abstract void run(List<Workflow<?>> w) throws CopperException;
	
	@Override
	public void run(String wfname, Object data) throws CopperException {
		try {
			Workflow<Object> wf = createWorkflowFactory(wfname).newInstance();
			wf.setData(data);
			run(wf);
		}
		catch(CopperException e) {
			throw e;
		}
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
			throw new CopperException("run failed",e);
		}
	}
	
	@Override
	public void run(WorkflowInstanceDescr<?> wfInstanceDescr) throws CopperException {
		try {
			run(createWorkflowInstance(wfInstanceDescr));
		}
		catch(CopperException e) {
			throw e;
		}
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
			throw new CopperException("run failed",e);
		}
	}

	protected Workflow<Object> createWorkflowInstance(WorkflowInstanceDescr<?> wfInstanceDescr) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		try {
			startupBlocker.pass();
		} 
		catch (InterruptedException e) {
			// ignore
		}
		
		final Workflow<Object> wf = wfRepository.createWorkflowFactory(wfInstanceDescr.getWfName() ,wfInstanceDescr.getVersion()).newInstance();
		if (wfInstanceDescr.getData() != null) wf.setData(wfInstanceDescr.getData());
		if (wfInstanceDescr.getId() != null) wf.setId(wfInstanceDescr.getId());
		if (wfInstanceDescr.getPriority() != null) wf.setPriority(wfInstanceDescr.getPriority());
		if (wfInstanceDescr.getProcessorPoolId() != null) wf.setProcessorPoolId(wfInstanceDescr.getProcessorPoolId());
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
		}
		catch(CopperException e) {
			throw e;
		}
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
			throw new CopperException("run failed",e);
		}
	}
	
	@Override
	public WorkflowRepositoryMXBean getWorkflowRepository() {
		return (WorkflowRepositoryMXBean) ((this.wfRepository instanceof WorkflowRepositoryMXBean) ? this.wfRepository : null);
	}
	
	@Override
	@Deprecated
	public final void notify(Response<?> response) throws CopperRuntimeException {
		Acknowledge.BestEffortAcknowledge ack = new Acknowledge.BestEffortAcknowledge(); 
		notify(response, ack);
	}

}
