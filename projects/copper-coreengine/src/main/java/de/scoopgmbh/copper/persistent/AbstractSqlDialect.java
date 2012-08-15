/*
 * Copyright 2002-2012 SCOOP Software GmbH
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;

import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.internal.WorkflowAccessor;
import de.scoopgmbh.copper.monitoring.NullRuntimeStatisticsCollector;
import de.scoopgmbh.copper.monitoring.RuntimeStatisticsCollector;
import de.scoopgmbh.copper.monitoring.StmtStatistic;

/**
 * Abstract base implementation of the {@link DatabaseDialect} for SQL databases
 * 
 * @author austermann
 *
 */
public abstract class AbstractSqlDialect implements DatabaseDialect {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSqlDialect.class);

	private WorkflowRepository wfRepository;
	private RuntimeStatisticsCollector runtimeStatisticsCollector = new NullRuntimeStatisticsCollector();
	private boolean removeWhenFinished = true;
	protected int defaultStaleResponseRemovalTimeout = 60*60*1000;
	protected Serializer serializer = new StandardJavaSerializer();

	protected String queryUpdateQueueState = getResourceAsString("/sql-query-ready-bpids.sql");

	private StmtStatistic dequeueStmtStatistic;
	private StmtStatistic queueDeleteStmtStatistic;
	private StmtStatistic enqueueUpdateStateStmtStatistic;
	private StmtStatistic insertStmtStatistic;
	private StmtStatistic deleteStaleResponsesStmtStatistic;

	public AbstractSqlDialect() {
		initStats();
	}


	public void startup() {
		
	}
	
	/**
	 * Sets the default removal timeout for stale responses in the underlying database. A response is stale/timed out when
	 * there is no workflow instance waiting for it within the specified amount of time. 
	 * @param defaultStaleResponseRemovalTimeout
	 */
	public void setDefaultStaleResponseRemovalTimeout(int defaultStaleResponseRemovalTimeout) {
		this.defaultStaleResponseRemovalTimeout = defaultStaleResponseRemovalTimeout;
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	public void setRuntimeStatisticsCollector(RuntimeStatisticsCollector runtimeStatisticsCollector) {
		this.runtimeStatisticsCollector = runtimeStatisticsCollector;
	}

	private void initStats() {
		dequeueStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery",runtimeStatisticsCollector);
		queueDeleteStmtStatistic = new StmtStatistic("DBStorage.queue.delete",runtimeStatisticsCollector);
		enqueueUpdateStateStmtStatistic = new StmtStatistic("DBStorage.enqueue.updateState",runtimeStatisticsCollector);
		insertStmtStatistic = new StmtStatistic("DBStorage.insert",runtimeStatisticsCollector);
		deleteStaleResponsesStmtStatistic = new StmtStatistic("DBStorage.deleteStaleResponses",runtimeStatisticsCollector);
	}

	protected String getResourceAsString(String name) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(name)));
			try {
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				return sb.toString();
			}
			finally {
				br.close();
			}
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#setWfRepository(de.scoopgmbh.copper.common.WorkflowRepository)
	 */
	public void setWfRepository(WorkflowRepository wfRepository) {
		this.wfRepository = wfRepository;
	}

	@Override
	public void setRemoveWhenFinished(boolean removeWhenFinished) {
		this.removeWhenFinished = removeWhenFinished;
	}

	@Override
	public void resumeBrokenBusinessProcesses(Connection con) throws Exception {
		logger.info("resumeBrokenBusinessProcesses");

		final Statement truncateStmt = con.createStatement();
		try {
			logger.info("Truncating queue...");
			truncateStmt.execute("TRUNCATE TABLE COP_QUEUE");
			logger.info("done!");
		}
		finally {
			JdbcUtils.closeStatement(truncateStmt);
		}

		final PreparedStatement insertStmt = con.prepareStatement("insert into cop_queue (ppool_id, priority, last_mod_ts, WORKFLOW_INSTANCE_ID) (select ppool_id, priority, last_mod_ts, id from COP_WORKFLOW_INSTANCE where state=0)");
		try {
			logger.info("Adding all BPs in state 0 to queue...");
			int rowcount = insertStmt.executeUpdate();
			logger.info("done - processed "+rowcount+" BP(s).");
		}
		finally {
			JdbcUtils.closeStatement(insertStmt);
		}

		final PreparedStatement updateStmt = con.prepareStatement("update cop_wait set state=0 where state=1");
		try {
			logger.info("Changing all WAITs to state 0...");
			int rowcount = updateStmt.executeUpdate();
			logger.info("done - changed "+rowcount+" WAIT(s).");
		}
		finally {
			JdbcUtils.closeStatement(updateStmt);
		}

		logger.info("Adding all BPs in working state with existing response(s)...");
		int rowcount = 0;
		for (;;) {
			int x = updateQueueState(100000, con);
			con.commit(); // TODO this is dirty
			if (x==0) break;
			rowcount+=x;
		}

		logger.info("done - processed "+rowcount+" BPs.");
	}

	@Override
	public List<Workflow<?>> dequeue(String ppoolId, int max, Connection con) throws Exception {
		logger.trace("dequeue({},{})", ppoolId, max);

		PreparedStatement dequeueStmt = null;
		PreparedStatement deleteStmt = null;
		PreparedStatement selectResponsesStmt = null;
		PreparedStatement updateBpStmt = null;
		try {
			final long startTS = System.currentTimeMillis();

			final List<Workflow<?>> rv = new ArrayList<Workflow<?>>(max);
			final List<String> invalidBPs = new ArrayList<String>();

			dequeueStmt = createDequeueStmt(con, ppoolId, max);
			deleteStmt = con.prepareStatement("delete from cop_queue where WORKFLOW_INSTANCE_ID=?");

			dequeueStmtStatistic.start();
			final ResultSet rs = dequeueStmt.executeQuery();
			final Map<String,Workflow<?>> map = new HashMap<String, Workflow<?>>(max*3);
			while (rs.next()) {
				final String id = rs.getString(1);
				final int prio = rs.getInt(2);

				deleteStmt.setString(1, id);
				deleteStmt.addBatch();

				try {
					SerializedWorkflow sw = new SerializedWorkflow();
					sw.setData(rs.getString(3));
					sw.setObjectState(rs.getString(4));
					PersistentWorkflow<?> wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
					wf.setId(id);
					wf.setProcessorPoolId(ppoolId);
					wf.setPriority(prio);
					WorkflowAccessor.setCreationTS(wf, new Date(rs.getTimestamp(5).getTime()));
					map.put(wf.getId(), wf);
				}
				catch(Exception e) {
					logger.error("decoding of '"+id+"' failed: "+e.toString(),e);
					invalidBPs.add(id);
				}
			}
			dequeueStmt.close();
			dequeueStmtStatistic.stop(map.size());

			if (map.isEmpty()) {
				return Collections.emptyList();
			}

			selectResponsesStmt = con.prepareStatement("select w.WORKFLOW_INSTANCE_ID, w.correlation_id, r.response from (select WORKFLOW_INSTANCE_ID, correlation_id from cop_wait where WORKFLOW_INSTANCE_ID in (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)) w LEFT OUTER JOIN cop_response r ON w.correlation_id = r.correlation_id");
			List<List<String>> ids = splitt(map.keySet(),25); 
			for (List<String> id : ids) {
				selectResponsesStmt.clearParameters();
				for (int i=0; i<25; i++) {
					selectResponsesStmt.setString(i+1, id.size() >= i+1 ? id.get(i) : null);
				}
				ResultSet rsResponses = selectResponsesStmt.executeQuery();
				while (rsResponses.next()) {
					String bpId = rsResponses.getString(1);
					String cid = rsResponses.getString(2);
					String response = rsResponses.getString(3);
					PersistentWorkflow<?> wf = (PersistentWorkflow<?>) map.get(bpId);
					Response<?> r;
					if (response != null) {
						r = (Response<?>) serializer.deserializeResponse(response);
						wf.addResponseCorrelationId(cid);
					}
					else {
						// timeout
						r = new Response<Object>(cid);
					}
					wf.putResponse(r);
					wf.addWaitCorrelationId(cid);
				}
				rsResponses.close();
			}

			queueDeleteStmtStatistic.start();
			deleteStmt.executeBatch();
			queueDeleteStmtStatistic.stop(map.size());

			rv.addAll(map.values());

			if (!invalidBPs.isEmpty()) {
				updateBpStmt = con.prepareStatement("update COP_WORKFLOW_INSTANCE set state=? where id=?");
				for (String id : invalidBPs) {
					updateBpStmt.setInt(1, DBProcessingState.INVALID.ordinal());
					updateBpStmt.setString(2,id);
					updateBpStmt.addBatch();
				}
				updateBpStmt.executeBatch();
				// ggf. auch die Waits und Responses löschen
			}
			logger.trace("dequeue for pool {} returns {} element(s)", ppoolId, rv.size());
			logger.debug("{} in {} msec", rv.size(),(System.currentTimeMillis()-startTS));
			return rv;
		}
		finally {
			JdbcUtils.closeStatement(updateBpStmt);
			JdbcUtils.closeStatement(dequeueStmt);
			JdbcUtils.closeStatement(deleteStmt);
			JdbcUtils.closeStatement(selectResponsesStmt);
		}
	}

	@Override
	public int updateQueueState(int max, Connection con) throws SQLException {
		PreparedStatement queryStmt = null;
		PreparedStatement updStmt = null;
		PreparedStatement insStmt = null;
		try {
			int rowcount = 0;
			final long startTS = System.currentTimeMillis();
			final Timestamp NOW = new Timestamp(System.currentTimeMillis());

			enqueueUpdateStateStmtStatistic.start();

			queryStmt = createUpdateStateStmt(con, max);
			ResultSet rs = queryStmt.executeQuery();
			updStmt = con.prepareStatement("update cop_wait set state=1 where WORKFLOW_INSTANCE_ID=?");
			insStmt = con.prepareStatement("INSERT INTO COP_QUEUE (PPOOL_ID, PRIORITY, LAST_MOD_TS, WORKFLOW_INSTANCE_ID) VALUES (?,?,?,?)");
			while (rs.next()) {
				rowcount++;

				final String bpId = rs.getString(1);
				final String ppoolId = rs.getString(2);
				final int prio = rs.getInt(3);

				updStmt.setString(1, bpId);
				updStmt.addBatch();

				insStmt.setString(1, ppoolId);
				insStmt.setInt(2, prio);
				insStmt.setTimestamp(3, NOW);
				insStmt.setString(4, bpId);
				insStmt.addBatch();
			}
			if (rowcount > 0) {
				insStmt.executeBatch();
				updStmt.executeBatch();
			}
			enqueueUpdateStateStmtStatistic.stop(rowcount == 0 ? 1 : rowcount);
			logger.debug("Queue update in {} msec", (System.currentTimeMillis()-startTS));
			return rowcount;
		}
		finally {
			JdbcUtils.closeStatement(insStmt);
			JdbcUtils.closeStatement(updStmt);
			JdbcUtils.closeStatement(queryStmt);
		}
	}

	@Override
	public int deleteStaleResponse(Connection con, int maxRows) throws Exception {
		PreparedStatement stmt = createDeleteStaleResponsesStmt(con, maxRows);
		try {
			deleteStaleResponsesStmtStatistic.start();
			int rowCount = stmt.executeUpdate();
			deleteStaleResponsesStmtStatistic.stop(rowCount);
			logger.trace("deleted {} stale response(s).", rowCount);
			return rowCount;
		}
		finally {
			JdbcUtils.closeStatement(stmt);
		}
	}

	@Override
	public void restart(String workflowInstanceId, Connection c) throws Exception {
		PreparedStatement stmtQueue = null;
		PreparedStatement stmtInstance = null;
		try {
			final Timestamp NOW = new Timestamp(System.currentTimeMillis());
			stmtQueue = c.prepareStatement("INSERT INTO COP_QUEUE (PPOOL_ID, PRIORITY, LAST_MOD_TS, WORKFLOW_INSTANCE_ID) (SELECT PPOOL_ID, PRIORITY, ?, ID FROM COP_WORKFLOW_INSTANCE WHERE ID=? AND STATE=5)");
			stmtInstance = c.prepareStatement("UPDATE COP_WORKFLOW_INSTANCE SET STATE=?, LAST_MOD_TS=? WHERE ID=? AND (STATE=? OR STATE=?)");
			stmtQueue.setTimestamp(1, NOW);
			stmtQueue.setString(2, workflowInstanceId);
			stmtInstance.setInt(1, DBProcessingState.ENQUEUED.ordinal());
			stmtInstance.setTimestamp(2, NOW);
			stmtInstance.setString(3, workflowInstanceId);
			stmtInstance.setInt(4, DBProcessingState.ERROR.ordinal());
			stmtInstance.setInt(5, DBProcessingState.INVALID.ordinal());
			stmtQueue.execute();
			stmtInstance.execute();
		}
		finally {
			JdbcUtils.closeStatement(stmtInstance);
			JdbcUtils.closeStatement(stmtQueue);
		}
	}

	@Override
	public void restartAll(Connection c) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void notify(List<Response<?>> responses, Connection c) throws Exception {
		final int MAX = 50;
		final List<Response<?>> subset = new ArrayList<Response<?>>(MAX);
		for (int i=0; i<responses.size(); i++) {
			subset.add(responses.get(i));
			if (subset.size() == MAX) {
				insertResponses(subset, c);
				subset.clear();
			}
		}
		insertResponses(subset, c);
		subset.clear();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void insertResponses(List<Response<?>> responses, Connection con) throws Exception {
		if (responses.isEmpty()) 
			return;
		List<BatchCommand> cmds = new ArrayList<BatchCommand>(responses.size());
		for (Response<?> r : responses) {
			cmds.add(createBatchCommand4Notify(r));
		}
		cmds.get(0).executor().doExec(cmds, con);
	}

	@Override
	@SuppressWarnings({"rawtypes"})
	public BatchCommand createBatchCommand4Finish(Workflow<?> w) {
		return new SqlRemove.Command((PersistentWorkflow<?>) w,removeWhenFinished);
	}

	@Override
	@SuppressWarnings({"rawtypes"})
	public BatchCommand createBatchCommand4Notify(Response<?> response) throws Exception {
		if (response == null) throw new NullPointerException();
		return new SqlNotify.Command(response, serializer);
	}

	@Override
	@SuppressWarnings({"rawtypes"})
	public BatchCommand createBatchCommand4registerCallback(RegisterCall rc, ScottyDBStorageInterface dbStorageInterface) throws Exception {
		if (rc == null) throw new NullPointerException();
		return new SqlRegisterCallback.Command(rc, serializer, dbStorageInterface);
	}

	@Override
	public void insert(List<Workflow<?>> wfs, Connection con) throws Exception {
		PreparedStatement stmtWF = null;
		PreparedStatement stmtQueue = null;
		try {
			final Timestamp NOW = new Timestamp(System.currentTimeMillis());
			stmtWF = con.prepareStatement("INSERT INTO COP_WORKFLOW_INSTANCE (ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,DATA,OBJECT_STATE,CREATION_TS,CLASSNAME) VALUES (?,?,?,?,?,?,?,?,?)");
			stmtQueue = con.prepareStatement("insert into cop_queue (ppool_id, priority, last_mod_ts, WORKFLOW_INSTANCE_ID) values (?,?,?,?)");
			int n = 0;
			for (int i=0; i<wfs.size(); i++) {
				Workflow<?> wf = wfs.get(i);
				final SerializedWorkflow sw = serializer.serializeWorkflow(wf);
				stmtWF.setString(1, wf.getId());
				stmtWF.setInt(2, DBProcessingState.ENQUEUED.ordinal());
				stmtWF.setInt(3, wf.getPriority());
				stmtWF.setTimestamp(4,NOW);
				stmtWF.setString(5, wf.getProcessorPoolId());
				stmtWF.setString(6, sw.getData());
				stmtWF.setString(7, sw.getObjectState());
				stmtWF.setTimestamp(8, new Timestamp(wf.getCreationTS().getTime()));
				stmtWF.setString(9, wf.getClass().getName());
				stmtWF.addBatch();

				stmtQueue.setString(1, wf.getProcessorPoolId());
				stmtQueue.setInt(2, wf.getPriority());
				stmtQueue.setTimestamp(3,NOW);
				stmtQueue.setString(4, wf.getId());
				stmtQueue.addBatch();

				n++;
				if (i % 100 == 0 || (i+1) == wfs.size()) {
					insertStmtStatistic.start();
					stmtWF.executeBatch();
					stmtQueue.executeBatch();
					insertStmtStatistic.stop(n);
					n = 0;
				}
			}
		}
		finally {
			JdbcUtils.closeStatement(stmtQueue);
			JdbcUtils.closeStatement(stmtWF);
		}
	}

	@Override
	public void insert(Workflow<?> wf, Connection con) throws Exception {
		final List<Workflow<?>> wfs = new ArrayList<Workflow<?>>(1);
		wfs.add(wf);
		insert(wfs, con);
	}

	protected List<List<String>> splitt(Collection<String> keySet, int n) {
		if (keySet.isEmpty()) 
			return Collections.emptyList();

		List<List<String>> r = new ArrayList<List<String>>(keySet.size()/n+1);
		List<String> l = new ArrayList<String>(n);
		for (String s : keySet) {
			l.add(s);
			if (l.size() == n) {
				r.add(l);
				l = new ArrayList<String>(n);
			}
		}
		if (l.size() > 0) {
			r.add(l);
		}
		return r;
	}	


	@Override
	@SuppressWarnings({"rawtypes"})
	public abstract BatchCommand createBatchCommand4error(Workflow<?> w, Throwable t);

	protected abstract PreparedStatement createUpdateStateStmt(final Connection c, final int max) throws SQLException;

	protected abstract PreparedStatement createDequeueStmt(final Connection c, final String ppoolId, final int max) throws SQLException;

	protected abstract PreparedStatement createDeleteStaleResponsesStmt(final Connection c, final int MAX_ROWS) throws SQLException;

}
