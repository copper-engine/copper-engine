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
package org.copperengine.core;

import org.copperengine.management.model.WorkflowInstanceFilter;

import java.sql.Connection;
import java.util.List;

/**
 * Adds some persistence specific methods to the ProcessingEngine interface.
 * 
 * @author austermann
 */
public interface PersistentProcessingEngine extends ProcessingEngine {

    /**
     * Enqueues the workflow instance into the engine for execution.
     * 
     * @param wfInstanceDescr
     *        workflow instance descriptions to run
     * @param con
     *        connection used for the inserting the workflow to the database
     * @throws CopperException
     *         if the engine can not run the workflow, e.g. in case of a unknown processor pool id
     * @throws DuplicateIdException
     *         if a workflow instance with the same id already exists
     * @return workflow instance ID
     */
    public String run(WorkflowInstanceDescr<?> wfInstanceDescr, Connection con) throws DuplicateIdException, CopperException;

    /**
     * Enqueues the specified list of workflow instances into the engine for execution.
     * 
     * @param wfInstanceDescr
     *        the list of workflow instance descriptions to run
     * @param con
     *        connection used for the inserting the workflow to the database
     * @throws CopperException
     *         if the engine can not run the workflow, e.g. in case of a unknown processor pool id
     * @throws DuplicateIdException
     *         if a workflow instance with the same id already exists
     */
    public void runBatch(List<WorkflowInstanceDescr<?>> wfInstanceDescr, Connection con) throws DuplicateIdException, CopperException;

    /**
     * Trigger restart a workflow instance that is in the error state.
     * 
     * @param workflowInstanceId
     *        id of workflow which shall be restarted
     * @throws Exception
     *        any exception happening on this request, like sql connection lost.
     *        Note: If id doesn't exist or the workflow with this id is not in error/invalid-state, this method just
     *        returns with doing nothing.
     */
    public void restart(final String workflowInstanceId) throws Exception;

    /**
     * Trigger restart of all workflow instances that are in error state.
     * 
     * @throws Exception
     *         {@link #restart(String)}.
     */
    public void restartAll() throws Exception;

    public void restartFiltered(WorkflowInstanceFilter filter) throws Exception;

    /**
     * Adds a response to the engine, using the provided jdbc connection. The engine will subsequently try to find the
     * corresponding workflow instance that is waiting for the response. Depending on the workflow instances waitmode
     * and
     * the number of open responses, the workflow may or may not be resumed.
     * 
     * @param response
     *        the reponse
     * @param c
     *        jdbc connection to use
     * @throws CopperRuntimeException
     *         any exception occuring in this operation (Like SQL connection lost) will be wrapped into this exception.
     */
    public void notify(Response<?> response, Connection c) throws CopperRuntimeException;

    /**
     * Adds a list of responses to the engine, using the provided jdbc connection. The engine will subsequently try to
     * find the
     * corresponding workflow instance that is waiting for the response. Depending on the workflow instances waitmode
     * and
     * the number of open responses, the workflow may or may not be resumed.
     * 
     * @param responses
     *        the list of reponses
     * @param c
     *        jdbc connection to use
     * @throws CopperRuntimeException
     *         any exception occuring in this operation (Like SQL connection lost) will be wrapped into this exception.
     */
    public void notify(List<Response<?>> responses, Connection c) throws CopperRuntimeException;

}
