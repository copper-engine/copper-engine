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
package de.scoopgmbh.copper.common;

import java.util.ArrayList;
import java.util.List;

import de.scoopgmbh.copper.CopperRuntimeException;
import de.scoopgmbh.copper.EngineIdProvider;
import de.scoopgmbh.copper.EngineIdProviderBean;
import de.scoopgmbh.copper.EngineState;
import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.WorkflowFactory;
import de.scoopgmbh.copper.util.Blocker;


public abstract class AbstractProcessingEngine implements ProcessingEngine {

	private IdFactory idFactory = new AtomicLongIdFactory();
	protected WorkflowRepository wfRepository;
	protected EngineState engineState = EngineState.RAW;
	protected Blocker startupBlocker = new Blocker(true);
	private List<Runnable> shutdownObserver = new ArrayList<Runnable>();
	private EngineIdProvider engineIdProvider = new EngineIdProviderBean("default"); 
	
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
	

}
