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
import java.util.List;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.management.model.AuditTrailInfo;
import org.copperengine.management.model.AuditTrailInstanceFilter;
import org.copperengine.management.model.WorkflowInstanceFilter;

/**
 * Interface for the storage of a {@link PersistentScottyEngine}.
 * Offers methods for storing and retrieving {@link Workflow}s and {@link Response}s.
 * 
 * @author austermann
 */
public interface ScottyDBStorageInterface {

    /**
     * Inserts a new workflow to the underlying database. The implementation may execute the inserts outside the callers
     * context. The completion will be signalled through the Acknowledge object.
     * @param wf
     *        workflow to be inserted
     * @param ack
     *         acknowledgment to notify about success or exception.
     * @throws DuplicateIdException
     *         If a workflow with the same id as the id of wf is already stored in the database.
     * @throws Exception
     *         For all kinds of other exceptions like losing database connection
     */
    public void insert(final Workflow<?> wf, Acknowledge ack) throws DuplicateIdException, Exception;

    /**
     * Inserts a list of new workflows to the underlying database. The implementation may execute the inserts outside
     * the callers context. The completion will be signalled through the Acknowledge object.
     * @param wfs
     *        workflows to be inserted
     * @param ack
     *         acknowledgment to notify about success or exception.
     * @throws DuplicateIdException
     *         If a workflow with the same id as the id of wf is already stored in the database.
     * @throws Exception
     *         For all kinds of other exceptions like losing database connection
     */
    public void insert(final List<Workflow<?>> wfs, Acknowledge ack) throws DuplicateIdException, Exception;

    /**
     * Inserts a new workflow to the underlying database using the provided connection.
     * It is up to the caller commit or rollback and close the connection.
     * @param wf
     *        workflow to be inserted
     * @param con
     *         Connection object on which insertion shall happen. With connection as parameter, the caller get
     *         full commit/rollback control and can thus make use of stronger insertion guarantees than possible
     *         with using Acknowledgments only.
     * @throws DuplicateIdException
     *         If a workflow with the same id as the id of wf is already stored in the database.
     * @throws Exception
     *         For all kinds of other exceptions like losing database connection
     */
    public void insert(final Workflow<?> wf, Connection con) throws DuplicateIdException, Exception;

    /**
     * Inserts a list of new workflows to the underlying database using the provided connection.
     * It is up to the caller commit or rollback and close the connection.
     * @param wfs
     *        workflows to be inserted
     * @param con
     *         Connection object on which insertion shall happen. With connection as parameter, the caller get
     *         full commit/rollback control and can thus make use of stronger insertion guarantees than possible
     *         with using Acknowledgments only.
     * @throws DuplicateIdException
     *         If a workflow with the same id as the id of wf is already stored in the database.
     * @throws Exception
     *         For all kinds of other exceptions like losing database connection
     */
    public void insert(final List<Workflow<?>> wfs, Connection con) throws DuplicateIdException, Exception;

    /**
     * Marks a workflow instance as finished or removes it from the underlying database. (Depending on how
     * {@link AbstractSqlDialect#setRemoveWhenFinished} is set. Default is to delete).
     * @param w
     *        workflow instance which finished
     * @param callback
     *        which is called after operation succeeded or an Exception occurred.
     */
    public void finish(final Workflow<?> w, final Acknowledge callback);

    /**
     * Dequeues up to <code>max</code> Workflow instances for the specified processor pool from the database.
     * It dequeues only such workflow instances that need further processing, e.g. when a response arrived or
     * a timeout occured.
     * @param ppoolId
     *        the processor pool id of the processor pool to which the workflows shall be dequeued
     * @param max
     *        maximum number of workflows which shall be dequeud by this call
     * @return
     *        List of workflows dequeued from database storage
     * @throws Exception
     *        Any exception which could happen in this procedure like losing database connection.
     */
    public List<Workflow<?>> dequeue(final String ppoolId, final int max)
            throws Exception;

    /**
     * Asynchronous service to add a {@link Response} to the database.
     * Regarding to our best practices, this method should be called from "outside COPPER" from some user implemented adapter.
     * @param response
     *        the response which holds the correlation ID (required to know which workflow is to be waken up if all wait-
     *        conditions are met) and the data which shall be provided to the workflow
     * @param ack
     *        An {@link Acknowledge} which is notified after a successful operation run or about the exception which occured otherwise.
     * @throws Exception
     *        Any exception which could happen in this procedure like losing database connection.
     *
     */
    public void notify(final Response<?> response, final Acknowledge ack)
            throws Exception;

    /**
     * Asynchronous service to add a list of {@link Response}s to the database.
     * @param response
     *        the list of responses for which each holds the correlation ID (required to know which workflow is to be waken up if all wait-
     *        conditions are met) and the data which shall be provided to the workflow
     * @param ack
     *        An {@link Acknowledge} which is notified after a successful operation run or about the exception which occured otherwise.
     * @throws Exception
     *        Any exception which could happen in this procedure like losing database connection.
     */
    public void notify(final List<Response<?>> response, final Acknowledge ack) throws Exception;

    /**
     * Synchronous service to add a list of {@link Response}s to the database using a provided database connection.
     * @param responses
     *        the responses for which each holds the correlation ID (required to know which workflow is to be waken up if all wait-
     *        conditions are met) and the data which shall be provided to the workflow
     * @param c
     *        Connection object on which notify shall happen. With connection as parameter, the caller get
     *        full commit/rollback control and can thus make use of stronger notification-guarantees than possible
     *        with using Acknowledgments only.
     * @throws Exception
     *        Any exception which could happen in this procedure like losing database connection.
     */
    public void notify(List<Response<?>> responses, Connection c) throws Exception;

    /**
     * Writes a workflow instance that is waiting for one or more asynchronous response back to
     * database.
     * @param rc
     *        class for holding information about the workflow which shall be updated in its processing state. Holds the
     *        workflow itself but also more data like the correlation IDs to wait upon and more. All data required for
     *        the database update is stored in here.
     * @param callback
     *        Callback which is called on success or error after this operation finished.
     * @throws Exception
     *         Any unexpected Exception like losing database connection.
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
     * @param w
     *        The workflow which shall be marked as erroneous
     * @param t
     *        The Throwable which lead the workflow get into this error state.
     * @param callback
     *        callback which notifies about success or error within this operation after it finished.
     */
    public void error(final Workflow<?> w, Throwable t, final Acknowledge callback);

    /**
     * Triggers the restart of a failed workflow instance, i.e. resubmitting the given workflow in DBProcessingState.INVALID
     * or DBProcessingState.ERROR to the queue to (re-)start from last stored execution point.
     * @param workflowInstanceId
     *        workflow id of broken workflow
     * @throws Exception
     *         Any Exception like losing database connection.
     */
    public void restart(final String workflowInstanceId) throws Exception;

    /**
     * @param removeWhenFinished If true (default), finished workflow instances are removed from the database.
     */
    public void setRemoveWhenFinished(boolean removeWhenFinished);

    /**
     * Triggers the restart of all failed workflow instances. See also {@link #restart(String)}
     * @param filter the WorkflowInstanceFilter
     * @throws Exception
     *         Any Exception like losing database connection.
     */
    public void restartFiltered(WorkflowInstanceFilter filter) throws Exception;

    public void restartAll() throws Exception;


    /**
     * Deletes a broken workflow from the system (i.e. from all tables)
     * @param workflowInstanceId workflow id of broken workflow
     * @throws Exception when the delete operation fails
     */
    public void deleteBroken(final String workflowInstanceId) throws Exception;
    public void deleteWaiting(final String workflowInstanceId) throws Exception;

    public void deleteFiltered(WorkflowInstanceFilter filter) throws Exception;

    public Workflow<?> read(final String workflowInstanceId) throws Exception;

    /**
     * Query all active workflowinstances from the backing storage
     * 
     * @param className
     *        optional - className of the active workflow instances
     * @param max
     *        limits the number of Workflow instances to read
     * @return list of deserialized workflow instance
     * @throws Exception
     *         Any Exception like losing database connection.
     */
    public List<Workflow<?>> queryAllActive(final String className, final int max) throws Exception;
    
    public int queryQueueSize(String processorPoolId) throws Exception;

    public List<Workflow<?>> queryWorkflowInstances(WorkflowInstanceFilter filter) throws Exception;

    public int countWorkflowInstances(WorkflowInstanceFilter filter) throws Exception;

    public List<AuditTrailInfo> queryAuditTrailInstances(final AuditTrailInstanceFilter filter) throws Exception;

    public String queryAuditTrailMessage(final long id) throws Exception;

    public int countAuditTrailInstances(final AuditTrailInstanceFilter filter) throws Exception;
}