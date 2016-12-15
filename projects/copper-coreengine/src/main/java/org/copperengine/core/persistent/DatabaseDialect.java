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
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.batcher.BatchCommand;

public interface DatabaseDialect {

    public abstract void resumeBrokenBusinessProcesses(Connection con) throws Exception;

    public abstract List<Workflow<?>> dequeue(final String ppoolId, final int max, Connection con) throws Exception;

    public abstract int updateQueueState(final int max, final Connection con) throws SQLException;

    public abstract int deleteStaleResponse(Connection con, int maxRows) throws Exception;

    public abstract void insert(final List<Workflow<?>> wfs, final Connection con) throws DuplicateIdException, Exception;

    public abstract void insert(final Workflow<?> wf, final Connection con) throws DuplicateIdException, Exception;

    public abstract void restart(final String workflowInstanceId, Connection c) throws Exception;

    public abstract void restartAll(Connection c) throws Exception;

    public abstract void notify(List<Response<?>> responses, Connection c) throws Exception;

    @SuppressWarnings({ "rawtypes" })
    public abstract BatchCommand createBatchCommand4Finish(final Workflow<?> w, final Acknowledge callback);

    @SuppressWarnings({ "rawtypes" })
    public abstract BatchCommand createBatchCommand4Notify(final Response<?> response, final Acknowledge callback) throws Exception;

    @SuppressWarnings({ "rawtypes" })
    public abstract BatchCommand createBatchCommand4registerCallback(final RegisterCall rc, final ScottyDBStorageInterface dbStorageInterface, final Acknowledge callback) throws Exception;

    @SuppressWarnings({ "rawtypes" })
    public abstract BatchCommand createBatchCommand4error(Workflow<?> w, Throwable t, DBProcessingState dbProcessingState, final Acknowledge callback);

    /**
     * If true (default), finished workflow instances are removed from the database.
     */
    public void setRemoveWhenFinished(boolean removeWhenFinished);

    /**
     * Checks the DB consistency, e.g. at system startup, by deserialising all workflow instances in the underlying
     * database.
     * 
     * @param con
     *        database connection
     * @return list of ids of bad workflows which could not be deserialized
     * @throws Exception
     */
    public List<String> checkDbConsistency(Connection con) throws Exception;

    public void startup();

    public void shutdown();

    public abstract Workflow<?> read(String workflowInstanceId, Connection con) throws Exception;

    /**
     * Query workflows that were active at the moment (in ENQUEU, RUNNING OR WAITING state)
     * 
     * @param className
     *        - optional, specify which className it want to return
     * @param con
     *        - database connection
     * @return
     * @throws SQLException
     */
    public List<Workflow<?>> queryAllActive(String className, Connection con, int max) throws SQLException;
    
    /**
     * Read the current system time from the underlying database system
     * @return
     * @throws SQLException
     */
    public Date readDatabaseClock(Connection con) throws SQLException;

    public int queryQueueSize(String processorPoolId, int max, Connection con) throws SQLException;

}