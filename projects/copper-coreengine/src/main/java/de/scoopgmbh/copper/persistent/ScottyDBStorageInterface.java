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
package de.scoopgmbh.copper.persistent;

import java.sql.Connection;
import java.util.List;

import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.audit.MessagePostProcessor;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummary;

/**
 * Interface for the storage of a {@link PersistentScottyEngine}.
 * Offers methods for storing and retrieving {@link Workflow}s and {@link Response}s. 
 *  
 * @author austermann
 *
 */
public interface ScottyDBStorageInterface {

	/**
	 * Inserts a new workflow to the underlying database.
	 */
	public void insert(final Workflow<?> wf) throws Exception;

	/**
	 * Inserts a list of new workflows to the underlying database.
	 */
	public void insert(final List<Workflow<?>> wfs) throws Exception;

	/**
	 * Inserts a new workflow to the underlying database using the provided connection.
	 * It is up to the caller commit or rollback and close the connection.
	 */
	public void insert(final Workflow<?> wf, Connection con) throws Exception;

	/**
	 * Inserts a list of new workflows to the underlying database using the provided connection.
	 * It is up to the caller commit or rollback and close the connection.
	 */
	public void insert(final List<Workflow<?>> wfs, Connection con) throws Exception;

	/**
	 * Marks a workflow instance as finished or removes it from the underlying database. 
	 */
	public void finish(final Workflow<?> w);

	/**
	 * Dequeues up to <code>max</code> Workflow instances for the specified processor pool from the database.
	 * It dequeues only such workflow instances that need further processing, e.g. when a response arrived or 
	 * a timeout occured. 
	 */
	public List<Workflow<?>> dequeue(final String ppoolId, final int max)
			throws Exception;

	/**
	 * Asynchronous service to add a {@link Response} to the database.
	 */
	public void notify(final Response<?> response, final Object callback)
			throws Exception;

	/**
	 * Asynchronous service to add a list of {@link Response}s to the database.
	 */
	public void notify(final List<Response<?>> response) throws Exception;

	/**
	 * Synchronous service to add a list of {@link Response}s to the database using a provided database connection.
	 */
	public void notify(List<Response<?>> responses, Connection c) throws Exception;
	
	

	/**
	 * Writes a workflow instance that is waiting for one or more asynchronous response back to
	 * database.  
	 */
	public void registerCallback(final RegisterCall rc) throws Exception;

	/**
	 * Startup the service
	 */
	public void startup();

	/**
	 * Shutdown the service
	 */
	public void shutdown();
	
	/**
	 * Marks a workflow instance as failed in the database. It may me triggered again later when the
	 * error cause has been solved using the <code>restart</code> method. 
	 */
	public void error(final Workflow<?> w, Throwable t);
	
	/**
	 * Triggers the restart of a failed workflow instance.
	 */
	public void restart(final String workflowInstanceId) throws Exception;
	
	/**
	 * If true (default), finished workflow instances are removed from the database.
	 */
	public void setRemoveWhenFinished(boolean removeWhenFinished);

	/**
	 * Triggers the restart of all failed workflow instances.
	 * @throws Exception 
	 */
	public void restartAll() throws Exception;
	
	
	public WorkflowStateSummary selectTotalWorkflowStateSummary();

	public List<AuditTrailInfo> selectAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level, long resultRowLimit);

	public String selectAuditTrailMessage(long id, MessagePostProcessor messagePostProcessor);

	public List<WorkflowSummary> selectWorkflowSummary(String poolid, String classname, long resultRowLimit);

	public List<WorkflowInstanceInfo> selectWorkflowInstanceList(String poolid, String classname,
			WorkflowInstanceState state, Integer priority, long resultRowLimit);


}