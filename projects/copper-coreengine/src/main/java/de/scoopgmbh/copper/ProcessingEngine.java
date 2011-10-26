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
package de.scoopgmbh.copper;

import java.util.List;

/**
 * Runtime container for COPPER workflows and main entry point for running COPPER workflow instances. 
 * The engine may be transient or persistent - this depends on the concrete implementation.
 * An engine is running in a single process (JVM). A process may run/contain several engines. 
 *  
 * @author auster
 *
 */
public interface ProcessingEngine {
	
	/**
	 * Starts up the engine. The invocation of this method blocks until the startup procedure is finished. 
	 * 
	 * @throws CopperRuntimeException
	 */
	public void startup() throws CopperRuntimeException;
	
	/**
	 * Triggers the shutdown of the engine. The engine tries to end running workflow instances gracefully.
	 * The invocation of this method returns immediately.
	 * 
	 * @throws CopperRuntimeException
	 */
	public void shutdown() throws CopperRuntimeException;
	
	/**
	 * Adds a shutdown observer/hook to the engine. During shutdown of the engine, {@link Runnable#run()} of each registered 
	 * observer is called subsequently. 
	 * @param observer
	 */
	public void addShutdownObserver(Runnable observer);
	
	/**
	 * Registers a workflow instance to we waiting for a number of correlation ids. 
	 * @param w the workflow instance waiting for one or more response
	 * @param mode the wait mode
	 * @param timeoutMsec the relative timeout in milliseconds or <code>0</code> for no an infinite timeout
	 * @param correlationIds the correlation ids of the expected responses
	 * @throws CopperRuntimeException
	 */
	public void registerCallbacks(Workflow<?> w, WaitMode mode, long timeoutMsec, String... correlationIds) throws CopperRuntimeException;
	
	/**
	 * Adds a response to the engine. The engine will subsequently try to find the corresponding workflow instance that is
	 * waiting for the response. Depending on the workflow instances waitmode and the number of open responses, the workflow 
	 * may or may not resumed.
	 * 
	 * @param response the reponse
	 * @throws CopperRuntimeException
	 */
	public void notify(Response<?> response) throws CopperRuntimeException;
	
	/**
	 * Creates a Universally Unique Identifier (UUID). The UUID may be used for workflow ids or correlation ids.
	 * @return
	 */
	public String createUUID();
	
	/**
	 * Creates a workflow factory for the creation of workflow instances.
	 * @param <E> class of the workflows <code>data</code> field
	 * @param classname classname of the workflows class
	 * @return a factory object for the creation of the specified workflow instances
	 * @throws ClassNotFoundException if the specified classname is unknown in the underlying workflow repository
	 */
	public <E> WorkflowFactory<E> createWorkflowFactory(String classname) throws ClassNotFoundException;
	
	/**
	 * Enqueues the specified workflow instance into the engine for execution.  
	 * @param w the workflow instance to run
	 * @throws CopperException if the engine can not run the workflow, e.g. in case of a unkown processor pool id
	 */
	public void run(Workflow<?> w) throws CopperException;
	
	/**
	 * Enqueues the specified list of workflow instances into the engine for execution.  
	 * @param w the list of workflow instances to run
	 * @throws CopperException if the engine can not run the workflow, e.g. in case of a unkown processor pool id
	 */
	public void run(List<Workflow<?>> w) throws CopperException;
	
	/**
	 * returns the engines current state
	 * @return the engine state
	 */
	public EngineState getEngineState();
	
	public String getEngineId();
	
}
