/*
 * Copyright 2002-2011 SCOOP Software GmbH
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
package de.scoopgmbh.copper.persistent;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.CopperException;
import de.scoopgmbh.copper.CopperRuntimeException;
import de.scoopgmbh.copper.DependencyInjector;
import de.scoopgmbh.copper.EngineState;
import de.scoopgmbh.copper.PersistentProcessingEngine;
import de.scoopgmbh.copper.ProcessingState;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.AbstractProcessingEngine;
import de.scoopgmbh.copper.common.ProcessorPoolManager;
import de.scoopgmbh.copper.management.PersistentProcessingEngineMXBean;
import de.scoopgmbh.copper.management.StatisticsCollectorMXBean;
import de.scoopgmbh.copper.management.WorkflowInfo;
import de.scoopgmbh.copper.monitoring.NullRuntimeStatisticsCollector;
import de.scoopgmbh.copper.monitoring.RuntimeStatisticsCollector;

/**
 * COPPER processing engine that offers persistent workflow processing. 
 *  
 * @author austermann
 *
 */
public class PersistentScottyEngine extends AbstractProcessingEngine implements PersistentProcessingEngine, PersistentProcessingEngineMXBean {

	private static final Logger logger = Logger.getLogger(PersistentScottyEngine.class);
	
	private ScottyDBStorageInterface dbStorage;
	private ProcessorPoolManager<PersistentProcessorPool> processorPoolManager;
	private DependencyInjector dependencyInjector;
	private boolean notifyProcessorPoolsOnResponse = false;
	private final Map<String, Workflow<?>> workflowMap = new ConcurrentHashMap<String, Workflow<?>>();
	private RuntimeStatisticsCollector statisticsCollector = new NullRuntimeStatisticsCollector();
	
	public void setStatisticsCollector(RuntimeStatisticsCollector statisticsCollector) {
		this.statisticsCollector = statisticsCollector;
	}
	
	/**
	 * If true, the engine notifies all processor pools about a new reponse available.
	 * This may lead to shorter latency times, but may also increase CPU load or database I/O,
	 * so use with care
	 * 
	 * @param notifyProcessorPoolsOnResponse
	 */
	public void setNotifyProcessorPoolsOnResponse(boolean notifyProcessorPoolsOnResponse) {
		this.notifyProcessorPoolsOnResponse = notifyProcessorPoolsOnResponse;
	}
	
	public void setDbStorage(ScottyDBStorageInterface dbStorage) {
		this.dbStorage = dbStorage;
	}
	
	public void setDependencyInjector(DependencyInjector dependencyInjector) {
		this.dependencyInjector = dependencyInjector;
	}
	
	public DependencyInjector getDependencyInjector() {
		return dependencyInjector;
	}
	
	public ScottyDBStorageInterface getDbStorage() {
		return dbStorage;
	}
	
	public void setProcessorPoolManager(ProcessorPoolManager<PersistentProcessorPool> processorPoolManager) {
		this.processorPoolManager = processorPoolManager;
	}
	
	@Override
	public void notify(Response<?> response) {
		if (logger.isTraceEnabled()) logger.trace("notify("+response+")");
		try {
			startupBlocker.pass();
			dbStorage.notify(response,null);
			if (notifyProcessorPoolsOnResponse) {
				for (PersistentProcessorPool ppp : processorPoolManager.processorPools()) {
					ppp.doNotify();
				}
			}
		} 
		catch (Exception e) {
			throw new CopperRuntimeException("notify failed",e);
		}

	}

	@Override
	public synchronized void shutdown() {
		if (engineState != EngineState.STARTED) throw new IllegalStateException();
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
		if (engineState != EngineState.RAW) throw new IllegalStateException();
		try {
			logger.info("starting up...");
				
			processorPoolManager.setEngine(this);
			dependencyInjector.setEngine(this);
			
			wfRepository.start();
			dbStorage.startup();
			
			processorPoolManager.startup();
			startupBlocker.unblock();
			engineState = EngineState.STARTED;
			
			logger.info("Engine is running");
		} 
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
			throw new CopperRuntimeException("startup failed",e);
		}
	}

	@Override
	public void registerCallbacks(Workflow<?> w, WaitMode mode, long timeoutMsec, String... correlationIds) {
		if (logger.isTraceEnabled()) logger.trace("registerCallbacks("+w+", "+mode+", "+timeoutMsec+", "+Arrays.asList(correlationIds)+")");
		if (correlationIds.length == 0) throw new IllegalArgumentException("No correlationids given");
		PersistentWorkflow<?> pw = (PersistentWorkflow<?>)w;
		if (processorPoolManager.getProcessorPool(pw.getProcessorPoolId()) == null) {
			logger.fatal("Unkown processor pool '"+pw.getProcessorPoolId()+"' - using default pool instead");
			pw.setProcessorPoolId(PersistentProcessorPool.DEFAULT_POOL_ID);
		}
		pw.registerCall = new RegisterCall(w, mode, timeoutMsec >= 0 ? timeoutMsec : null, correlationIds);
	}

	@Override
	public void run(Workflow<?> wf) throws CopperException {
		run(wf,null);
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
		run(list,null);
	}

	/**
	 * Enqueues the specified list of workflow instances into the engine for execution.  
	 * @param w the list of workflow instances to run
	 * @param con connection used for the inserting the workflow to the database 
	 * @throws CopperException if the engine can not run the workflow, e.g. in case of a unkown processor pool id
	 */
	public void run(List<Workflow<?>> list, Connection con) throws CopperException {
		if (logger.isTraceEnabled()) {
			for (Workflow<?> w : list)
				logger.trace("run("+w+")");
		}
		try {
			startupBlocker.pass();

			Set<String> ppoolIds = new HashSet<String>();
			for (Workflow<?> wf : list) {
				if (!(wf instanceof PersistentWorkflow<?>)) {
					throw new IllegalArgumentException(wf.getClass()+" is no instance of PersistentWorkflow");
				}
				if (wf.getId() == null) {
					wf.setId(createUUID());
				}
				if (wf.getProcessorPoolId() == null) {
					wf.setProcessorPoolId(PersistentProcessorPool.DEFAULT_POOL_ID);
				}

				if (processorPoolManager.getProcessorPool(wf.getProcessorPoolId()) == null) {
					logger.fatal("Unkown processor pool '"+wf.getProcessorPoolId()+"' - using default pool instead");
					wf.setProcessorPoolId(PersistentProcessorPool.DEFAULT_POOL_ID);
				}
				
				ppoolIds.add(wf.getProcessorPoolId());
			}
			dbStorage.insert(list, con);
			for (String ppoolId : ppoolIds) {
				notifyProcessorPool(ppoolId);
			}
		}
		catch(Exception e) {
			throw new CopperException("run failed",e);
		}
	}

	/**
	 * Enqueues the specified workflow instance into the engine for execution.  
	 * @param w the workflow instance to run
	 * @param con connection used for the inserting the workflow to the database 
	 * @throws CopperException if the engine can not run the workflow, e.g. in case of a unkown processor pool id
	 */
	public void run(Workflow<?> wf, Connection con) throws CopperException {
		if (logger.isTraceEnabled()) logger.trace("run("+wf+")");
		if (!(wf instanceof PersistentWorkflow<?>)) {
			throw new IllegalArgumentException(wf.getClass()+" is no instance of PersistentWorkflow");
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
				logger.fatal("Unkown processor pool '"+wf.getProcessorPoolId()+"' - using default pool instead");
				wf.setProcessorPoolId(PersistentProcessorPool.DEFAULT_POOL_ID);
			}
			dbStorage.insert(wf, con);
			notifyProcessorPool(wf.getProcessorPoolId());
		}
		catch(Exception e) {
			throw new CopperException("run failed",e);
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
		logger.info("queryWorkflowInstances returned "+rv.size()+" instance(s)");
		return rv;
	}

	@Override
	public WorkflowInfo queryWorkflowInstance(String id) {
		return convert2Wfi(workflowMap.get(id));
	}

	
	void register(Workflow<?> wf) {
		if (logger.isTraceEnabled()) logger.trace("register("+wf.getId()+")");
		Workflow<?> existingWF = workflowMap.put(wf.getId(),wf);
		assert existingWF == null;
	}
	
	void unregister(Workflow<?> wf) { 
		Workflow<?> existingWF = workflowMap.remove(wf.getId());
		assert existingWF != null;
		if (existingWF != null && existingWF.getProcessingState() == ProcessingState.FINISHED) {
			statisticsCollector.submit(getEngineId()+"."+wf.getClass().getSimpleName()+".ExecutionTime", 1, System.currentTimeMillis()-wf.getCreationTS().getTime(), TimeUnit.MILLISECONDS);
		}
	}
	
	public int getNumberOfWorkflowInstances() {
		return workflowMap.size();
	}

	@Override
	public void restartAll() throws Exception {
		dbStorage.restartAll();
	}

}
