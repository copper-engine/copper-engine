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
	 * may or may not be resumed. This method is unsafe. The control might be returned to the caller before the notification has been safeley delivered.
	 * Use {@link ProcessingEngine#notify(Response, Acknowledge)} instead.
	 * 
	 * @param response the reponse
	 * @throws CopperRuntimeException
	 */
	@Deprecated
	public void notify(Response<?> response) throws CopperRuntimeException;
	
	/**
	 * Adds a response to the engine. The engine will subsequently try to find the corresponding workflow instance that is
	 * waiting for the response. Depending on the workflow instances waitmode and the number of open responses, the workflow 
	 * may or may not be resumed.
	 * 
	 * @param response the reponse
	 * @param ack the object to notify upon processing of the message.
	 * @throws CopperRuntimeException
	 */
	public void notify(Response<?> response, Acknowledge ack) throws CopperRuntimeException;
	
	/**
	 * Creates a Universally Unique Identifier (UUID). The UUID may be used for workflow ids or correlation ids.
	 * @return
	 */
	public String createUUID();
	
	/**
	 * Enqueues the specified workflow instance into the engine for execution.  
	 * @param wfname name or alias of the workflows class
	 * @param data the data to pass to the workflow
	 * @throws CopperException if the engine can not run the workflow, e.g. in case of a unkown processor pool id. THIS METHOD IS NOT SAFE: The control flow may return before the message has been processed securely.
	 */
	public void run(String wfname, Object data) throws CopperException;
	
	/**
	 * Enqueues the specified workflow instance description into the engine for execution.  
	 * @throws CopperException if the engine can not run the workflow, e.g. in case of a unkown processor pool id. THIS METHOD IS NOT SAFE: The control flow may return before the message has been processed securely.
	 */
	public void run(WorkflowInstanceDescr<?> wfInstanceDescr) throws CopperException;
	
	/**
	 * Enqueues the specified batch of workflow instance description into the engine for execution.  
	 * @throws CopperException if the engine can not run the workflows, e.g. in case of a unkown processor pool id. THIS METHOD IS NOT SAFE: The control flow may return before the message has been processed securely.
	 */
	public void runBatch(List<WorkflowInstanceDescr<?>> wfInstanceDescr) throws CopperException;
	
	
	/**
	 * returns the engines current state
	 * @return the engine state
	 */
	public EngineState getEngineState();
	
	/**
	 * returns the engines id
	 * @return the engine id
	 */
	public String getEngineId();
	
	/**
	 * Adds the specified WaitHook for the workflow instance with the specified id.
	 * The WaitHook is called once at the next wait invocation of the specified workflow instance.
	 *  
	 * @param wfInstanceId
	 * @param waitHook
	 */
	public void addWaitHook(String wfInstanceId, WaitHook waitHook);
	
}
