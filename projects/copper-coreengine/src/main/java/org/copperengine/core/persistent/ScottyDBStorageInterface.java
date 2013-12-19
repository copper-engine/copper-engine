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
package org.copperengine.core.persistent;

import java.sql.Connection;
import java.util.List;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;


/**
 * Interface for the storage of a {@link PersistentScottyEngine}.
 * Offers methods for storing and retrieving {@link Workflow}s and {@link Response}s. 
 *  
 * @author austermann
 *
 */
public interface ScottyDBStorageInterface {

	/**
	 * Inserts a new workflow to the underlying database. The implementation may execute the inserts outside the callers context. The completion will be signalled through the Acknowledge object.
	 */
	public void insert(final Workflow<?> wf, Acknowledge ack) throws DuplicateIdException, Exception;

	/**
	 * Inserts a list of new workflows to the underlying database. The implementation may execute the inserts outside the callers context. The completion will be signalled through the Acknowledge object. 
	 */
	public void insert(final List<Workflow<?>> wfs, Acknowledge ack) throws DuplicateIdException, Exception;

	/**
	 * Inserts a new workflow to the underlying database using the provided connection.
	 * It is up to the caller commit or rollback and close the connection.
	 */
	public void insert(final Workflow<?> wf, Connection con) throws DuplicateIdException, Exception;

	/**
	 * Inserts a list of new workflows to the underlying database using the provided connection.
	 * It is up to the caller commit or rollback and close the connection.
	 */
	public void insert(final List<Workflow<?>> wfs, Connection con) throws DuplicateIdException, Exception;

	/**
	 * Marks a workflow instance as finished or removes it from the underlying database. 
	 */
	public void finish(final Workflow<?> w, final Acknowledge callback);

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
	public void notify(final Response<?> response, final Acknowledge ack)
			throws Exception;

	/**
	 * Asynchronous service to add a list of {@link Response}s to the database.
	 */
	public void notify(final List<Response<?>> response, final Acknowledge ack) throws Exception;

	/**
	 * Synchronous service to add a list of {@link Response}s to the database using a provided database connection.
	 */
	public void notify(List<Response<?>> responses, Connection c) throws Exception;
	
	

	/**
	 * Writes a workflow instance that is waiting for one or more asynchronous response back to
	 * database.  
	 */
	public void registerCallback(final RegisterCall rc, final Acknowledge callback) throws Exception;

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
	public void error(final Workflow<?> w, Throwable t, final Acknowledge callback);
	
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
	
	/**
	 * Read a workflow from the backing storage
	 * @param workflowInstanceId the id of the workflow
	 * @return a new deserialized workflow instance
	 * @throws Exception
	 */
	public Workflow<?> read(final String workflowInstanceId) throws Exception;


}