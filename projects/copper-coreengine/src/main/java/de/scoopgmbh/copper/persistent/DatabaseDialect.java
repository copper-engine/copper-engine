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
import java.sql.SQLException;
import java.util.List;

import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.audit.MessagePostProcessor;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummary;

public interface DatabaseDialect {

	public abstract void resumeBrokenBusinessProcesses(Connection con) throws Exception;

	public abstract List<Workflow<?>> dequeue(final String ppoolId,	final int max, Connection con) throws Exception;

	public abstract int updateQueueState(final int max, final Connection con) throws SQLException;

	public abstract int deleteStaleResponse(Connection con, int maxRows) throws Exception;

	public abstract void insert(final List<Workflow<?>> wfs, final Connection con) throws Exception;

	public abstract void insert(final Workflow<?> wf, final Connection con) throws Exception;

	public abstract void restart(final String workflowInstanceId, Connection c) throws Exception;

	public abstract void restartAll(Connection c) throws Exception;

	public abstract void notify(List<Response<?>> responses, Connection c) throws Exception;

	@SuppressWarnings({"rawtypes"})
	public abstract BatchCommand createBatchCommand4Finish(final Workflow<?> w);

	@SuppressWarnings({"rawtypes"})
	public abstract BatchCommand createBatchCommand4Notify(final Response<?> response) throws Exception;

	@SuppressWarnings({"rawtypes"})
	public abstract BatchCommand createBatchCommand4registerCallback(final RegisterCall rc, final ScottyDBStorageInterface dbStorageInterface) throws Exception;

	@SuppressWarnings({"rawtypes"})
	public abstract BatchCommand createBatchCommand4error(Workflow<?> w, Throwable t, DBProcessingState dbProcessingState);
	
	/**
	 * If true (default), finished workflow instances are removed from the database.
	 */
	public void setRemoveWhenFinished(boolean removeWhenFinished);	
	
	/**
	 * Checks the DB consistency, e.g. at system startup, by deserialising all workflow instances in the underlying database.
	 * @param con
	 * @return
	 * @throws Exception
	 */
	public List<String> checkDbConsistency(Connection con) throws Exception;
	
	public void startup();
	
	public void shutdown();

	public abstract WorkflowStateSummary selectTotalWorkflowStateSummary(Connection con);

	public abstract List<AuditTrailInfo> selectAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level,
			long resultRowLimit, Connection con);

	public abstract String selectAuditTrailMessage(long id, Connection con, MessagePostProcessor messagePostProcessor);

	public abstract List<WorkflowSummary> selectWorkflowStateSummary(String poolid, String classname, Connection con);

	public abstract List<WorkflowInstanceInfo> selectWorkflowInstanceList(String poolid, String classname,
			WorkflowInstanceState state, Integer priority, long resultRowLimit, Connection con);


}