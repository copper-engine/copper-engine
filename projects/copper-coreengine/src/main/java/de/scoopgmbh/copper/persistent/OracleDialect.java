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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;

import de.scoopgmbh.copper.CopperRuntimeException;
import de.scoopgmbh.copper.EngineIdProvider;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.internal.WorkflowAccessor;
import de.scoopgmbh.copper.monitoring.NullRuntimeStatisticsCollector;
import de.scoopgmbh.copper.monitoring.RuntimeStatisticsCollector;
import de.scoopgmbh.copper.monitoring.StmtStatistic;

/**
 * Oracle implementation of the {@link DatabaseDialect} interface
 * 
 * @author austermann
 *
 */
public class OracleDialect implements DatabaseDialect {

	private static final Logger logger = LoggerFactory.getLogger(OracleDialect.class);

	// internal members
	private StmtStatistic dequeueAllStmtStatistic;
	private StmtStatistic dequeueQueryBPsStmtStatistic;
	private StmtStatistic dequeueQueryResponsesStmtStatistic;
	private StmtStatistic dequeueMarkStmtStatistic;
	private StmtStatistic enqueueUpdateStateStmtStatistic;
	private StmtStatistic insertStmtStatistic;
	private StmtStatistic deleteStaleResponsesStmtStatistic;
	private StmtStatistic dequeueWait4RespLdrStmtStatistic;
	private final Map<String, ResponseLoader> responseLoaders = new HashMap<String, ResponseLoader>();

	// mandatory Properties
	private WorkflowRepository wfRepository = null;
	private EngineIdProvider engineIdProvider = null;

	// optional Properties
	private boolean multiEngineMode = false;
	private int lockWaitSeconds = 10;
	private RuntimeStatisticsCollector runtimeStatisticsCollector = new NullRuntimeStatisticsCollector();
	private Serializer serializer = new StandardJavaSerializer();
	private boolean removeWhenFinished = true;
	private int defaultStaleResponseRemovalTimeout = 60*60*1000;


	public OracleDialect() {
		initStmtStats();
	}

	public void startup() {
		if (engineIdProvider == null || engineIdProvider.getEngineId() == null) throw new NullPointerException("EngineId is NULL! Change your "+getClass().getSimpleName()+" configuration.");
	}

	private void initStmtStats() {
		dequeueAllStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery.all", runtimeStatisticsCollector);
		dequeueQueryBPsStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery.queryBPs", runtimeStatisticsCollector);
		dequeueQueryResponsesStmtStatistic = new StmtStatistic("DBStorage.dequeue.fullquery.queryResponses", runtimeStatisticsCollector);
		dequeueMarkStmtStatistic = new StmtStatistic("DBStorage.dequeue.mark", runtimeStatisticsCollector);
		enqueueUpdateStateStmtStatistic = new StmtStatistic("DBStorage.enqueue.updateState", runtimeStatisticsCollector);
		insertStmtStatistic = new StmtStatistic("DBStorage.insert", runtimeStatisticsCollector);
		deleteStaleResponsesStmtStatistic = new StmtStatistic("DBStorage.deleteStaleResponses", runtimeStatisticsCollector);
		dequeueWait4RespLdrStmtStatistic = new StmtStatistic("DBStorage.wait4resLoaderStmtStatistic", runtimeStatisticsCollector);

	}

	/**
	 * Sets the default removal timeout for stale responses in the underlying database. A response is stale/timed out when
	 * there is no workflow instance waiting for it within the specified amount of time. 
	 * @param defaultStaleResponseRemovalTimeout
	 */
	public void setDefaultStaleResponseRemovalTimeout(int defaultStaleResponseRemovalTimeout) {
		this.defaultStaleResponseRemovalTimeout = defaultStaleResponseRemovalTimeout;
	}

	public void setRemoveWhenFinished(boolean removeWhenFinished) {
		this.removeWhenFinished = removeWhenFinished;
	}

	public void setEngineIdProvider(EngineIdProvider engineIdProvider) {
		this.engineIdProvider = engineIdProvider;
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	public void setLockWaitSeconds(int lockWaitSeconds) {
		this.lockWaitSeconds = lockWaitSeconds;
	}

	public void setMultiEngineMode(boolean multiEngineMode) {
		this.multiEngineMode = multiEngineMode;
	}

	public void setRuntimeStatisticsCollector(RuntimeStatisticsCollector runtimeStatisticsCollector) {
		this.runtimeStatisticsCollector = runtimeStatisticsCollector;
	}

	public void setWfRepository(WorkflowRepository wfRepository) {
		this.wfRepository = wfRepository;
	}

	public RuntimeStatisticsCollector getRuntimeStatisticsCollector() {
		return runtimeStatisticsCollector;
	}

	public boolean isRemoveWhenFinished() {
		return removeWhenFinished;
	}

	public Serializer getSerializer() {
		return serializer;
	}

	public int getDefaultStaleResponseRemovalTimeout() {
		return defaultStaleResponseRemovalTimeout;
	}	

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#resumeBrokenBusinessProcesses(java.sql.Connection)
	 */
	@Override
	public void resumeBrokenBusinessProcesses(Connection con) throws Exception {
		logger.info("Reactivating queue entries...");
		final PreparedStatement stmt = con.prepareStatement("UPDATE cop_queue SET engine_id = null WHERE engine_id=?");
		try {
			stmt.setString(1, engineIdProvider.getEngineId());
			stmt.execute();
		}
		finally {
			JdbcUtils.closeStatement(stmt);
		}
		logger.info("done!");
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#dequeue(java.lang.String, int, java.sql.Connection)
	 */
	@Override
	public List<Workflow<?>> dequeue(final String ppoolId, final int max, Connection con) throws Exception {
		logger.trace("dequeue({},{})",ppoolId,max);

		final long startTS = System.currentTimeMillis();
		final List<Workflow<?>> rv = new ArrayList<Workflow<?>>(max);

		lock(con,"dequeue#"+ppoolId);

		ResponseLoader responseLoader = getResponseLoader(ppoolId);
		responseLoader.setCon(con);
		responseLoader.setSerializer(serializer);
		responseLoader.setEngineId(engineIdProvider.getEngineId());
		responseLoader.beginTxn();

		final List<OracleSetToError.Command> invalidWorkflowInstances = new ArrayList<OracleSetToError.Command>();
		final PreparedStatement dequeueStmt = con.prepareStatement("select id,priority,data,rowid,long_data,creation_ts,object_state,long_object_state from COP_WORKFLOW_INSTANCE where rowid in (select * from (select WFI_ROWID from cop_queue where ppool_id=? and engine_id is null order by ppool_id, priority, last_mod_ts) where rownum <= ?)");
		final Map<String,Workflow<?>> map = new HashMap<String, Workflow<?>>(max*3);
		try {
			dequeueStmt.setString(1, ppoolId);
			dequeueStmt.setInt(2, max);
			dequeueAllStmtStatistic.start();
			dequeueQueryBPsStmtStatistic.start();
			final ResultSet rs = dequeueStmt.executeQuery();
			dequeueQueryBPsStmtStatistic.stop(1);
			while (rs.next()) {
				final String id = rs.getString(1);
				final int prio = rs.getInt(2);
				final String rowid = rs.getString(4);
				final Timestamp creationTS = rs.getTimestamp(6);
				try {
					String objectState = rs.getString(7);
					if (objectState == null) 
						objectState = rs.getString(8);
					String data = rs.getString(3);
					if (data == null) 
						data = rs.getString(5);
					SerializedWorkflow sw = new SerializedWorkflow();
					sw.setData(data);
					sw.setObjectState(objectState);
					PersistentWorkflow<?> wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
					wf.setId(id);
					wf.setProcessorPoolId(ppoolId);
					wf.setPriority(prio);
					wf.rowid = rowid;
					wf.oldPrio = prio;
					wf.oldProcessorPoolId = ppoolId;
					WorkflowAccessor.setCreationTS(wf, new Date(creationTS.getTime()));
					map.put(wf.getId(), wf);
					responseLoader.enqueue(wf);
				}
				catch(Exception e) {
					logger.error("decoding of '"+id+"' failed: "+e.toString(),e);
					invalidWorkflowInstances.add(new OracleSetToError.Command(new DummyPersistentWorkflow(id, ppoolId, rowid, prio),e,DBProcessingState.INVALID));
				}
			}
		}
		finally {
			JdbcUtils.closeStatement(dequeueStmt);
		}

		dequeueWait4RespLdrStmtStatistic.start();
		responseLoader.endTxn();
		dequeueWait4RespLdrStmtStatistic.stop(map.size());

		rv.addAll(map.values());

		dequeueAllStmtStatistic.stop(map.size());

		handleInvalidWorkflowInstances(con, invalidWorkflowInstances);
		
		if (logger.isDebugEnabled()) logger.debug("dequeue for pool "+ppoolId+" returns "+rv.size()+" element(s) in "+(System.currentTimeMillis()-startTS)+" msec.");
		return rv;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void handleInvalidWorkflowInstances(Connection con, final List invalidWorkflowInstances) throws Exception {
		logger.debug("invalidWorkflowInstances.size()={}",invalidWorkflowInstances.size());
		if (invalidWorkflowInstances.isEmpty()) {
			return;
		}
		((BatchCommand)invalidWorkflowInstances.get(0)).executor().doExec(invalidWorkflowInstances, con);
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#updateQueueState(int, java.sql.Connection)
	 */
	@Override
	public int updateQueueState(final int max, final Connection con) throws SQLException {
		CallableStatement stmt = null;
		try {
			final long startTS = System.currentTimeMillis();
			lock(con, "updateQueueState");
			enqueueUpdateStateStmtStatistic.start();
			stmt = con.prepareCall("begin COP_COREENGINE.enqueue(?,?); end;");
			stmt.setInt(1, max);
			stmt.registerOutParameter(2, Types.INTEGER);
			stmt.execute();
			int rowcount = stmt.getInt(2);
			enqueueUpdateStateStmtStatistic.stop(rowcount == 0 ? 1 : rowcount);
			logger.debug("Queue update in {} msec", System.currentTimeMillis()-startTS);
			return rowcount;
		}
		finally {
			JdbcUtils.closeStatement(stmt);
		}
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#deleteStaleResponse(java.sql.Connection, int)
	 */
	@Override
	public int deleteStaleResponse(Connection con, int maxRows) throws Exception {
		if (logger.isTraceEnabled()) logger.trace("deleteStaleResponse()");

		lock(con,"deleteStaleResponse");

		final PreparedStatement stmt = con.prepareStatement("delete from cop_response r where response_timeout < ? and not exists (select * from cop_wait w where w.correlation_id = r.correlation_id) and rownum <= "+maxRows);
		try {
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			deleteStaleResponsesStmtStatistic.start();
			final int rowCount= stmt.executeUpdate();
			deleteStaleResponsesStmtStatistic.stop(rowCount);
			logger.trace("deleted {} stale response(s).",rowCount);
			return rowCount;
		}
		finally {
			JdbcUtils.closeStatement(stmt);
		}
	}

	/**
	 * returns an int value between 0 and 1073741823 (exclusive)
	 */
	static int computeLockId(String s) {
		// This method handles the following fact: Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE
		int hashCode = s.hashCode();
		if (hashCode == Integer.MIN_VALUE) {
			hashCode = 13;
		}
		return Math.abs(hashCode) % 1073741823;
	}

	private void lock(Connection c, String context) throws SQLException {
		if (!multiEngineMode)
			return;
		final int lockId = computeLockId(context);
		int result=0;
		for (int i=0; i<3; i++) {
			if (logger.isDebugEnabled()) logger.debug("Trying to acquire db lock for '"+context+"' ==> lockId="+lockId);
			CallableStatement stmt = c.prepareCall("{? = call dbms_lock.request(?,DBMS_LOCK.X_MODE,?,TRUE)}");
			stmt.registerOutParameter(1, Types.INTEGER);
			stmt.setInt(2, lockId);  // lock id
			stmt.setInt(3, lockWaitSeconds); // wait time in seconds 
			stmt.execute();
			result = stmt.getInt(1); 
			if (logger.isDebugEnabled()) logger.debug("acquire lock returned with value '"+result+"'");
			if (result == 0 /*OK*/ || result == 4 /* Already own lock specified by id or lockhandle */)
				return;
			if (result == 3 /* Parameter error */ || result == 5 /* Illegal lock handle */)
				throw new SQLException(result == 3 ? "Parameter error" : "Illegal lock handle");
			assert result == 1 || result == 2;
		}
		if (result == 1) throw new SQLException("unable to acquire lock: timeout");
		if (result == 2) throw new SQLException("unable to acquire lock: deadlock");
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#insert(java.util.List, java.sql.Connection)
	 */
	@Override
	public void insert(final List<Workflow<?>> wfs, final Connection con) throws Exception {
		final PreparedStatement stmt = con.prepareStatement("INSERT INTO COP_WORKFLOW_INSTANCE (ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,DATA,LONG_DATA,OBJECT_STATE,LONG_OBJECT_STATE,CREATION_TS,CLASSNAME) VALUES (?,?,?,SYSTIMESTAMP,?,?,?,?,?,?,?)");
		try {
			int n = 0;
			for (int i=0; i<wfs.size(); i++) {
				Workflow<?> wf = wfs.get(i);
				final SerializedWorkflow sw = serializer.serializeWorkflow(wf);
				stmt.setString(1, wf.getId());
				stmt.setInt(2, DBProcessingState.ENQUEUED.ordinal());
				stmt.setInt(3, wf.getPriority());
				stmt.setString(4, wf.getProcessorPoolId());
				if (sw.getData() != null) {
					stmt.setString(5, sw.getData().length() > 4000 ? null : sw.getData());
					stmt.setString(6, sw.getData().length() > 4000 ? sw.getData() : null);
				}
				else {
					stmt.setString(5, null);
					stmt.setString(6, null);
				}
				if (sw.getObjectState() != null) {
					stmt.setString(7, sw.getObjectState().length() > 4000 ? null : sw.getObjectState());
					stmt.setString(8, sw.getObjectState().length() > 4000 ? sw.getObjectState() : null);
				}
				else {
					stmt.setString(7, null);
					stmt.setString(8, null);
				}
				stmt.setTimestamp(9, new Timestamp(wf.getCreationTS().getTime()));
				stmt.setString(10, wf.getClass().getName());
				stmt.addBatch();
				n++;
				if (i % 100 == 0 || (i+1) == wfs.size()) {
					insertStmtStatistic.start();
					stmt.executeBatch();
					insertStmtStatistic.stop(n);
					n = 0;
				}
			}
		}
		finally {
			JdbcUtils.closeStatement(stmt);
		}
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#insert(de.scoopgmbh.copper.Workflow, java.sql.Connection)
	 */
	@Override
	public void insert(final Workflow<?> wf, final Connection con) throws Exception {
		final List<Workflow<?>> wfs = new ArrayList<Workflow<?>>(1);
		wfs.add(wf);
		insert(wfs, con);
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#restart(java.lang.String, java.sql.Connection)
	 */
	@Override
	public void restart(final String workflowInstanceId, Connection c) throws Exception {
		logger.trace("restart({})", workflowInstanceId);
		CallableStatement stmt = c.prepareCall("begin COP_COREENGINE.restart(?); end;");
		try {
			stmt.setString(1, workflowInstanceId);
			stmt.execute();
		}
		finally {
			JdbcUtils.closeStatement(stmt);
		}
		logger.info(workflowInstanceId+" successfully queued for restart.");
	}

	private ResponseLoader getResponseLoader(final String ppoolId) {
		ResponseLoader responseLoader = null;
		synchronized (responseLoaders) {
			responseLoader = responseLoaders.get(ppoolId);
			if (responseLoader == null) {
				responseLoader = new ResponseLoader(dequeueQueryResponsesStmtStatistic, dequeueMarkStmtStatistic);
				responseLoader.start();
				responseLoaders.put(ppoolId, responseLoader);
			}
		}
		return responseLoader;
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#restartAll(java.sql.Connection)
	 */
	@Override
	public void restartAll(Connection c) throws Exception {
		logger.trace("restartAll()");
		CallableStatement stmt = c.prepareCall("begin COP_COREENGINE.restart_all; end;");
		try {
			stmt.execute();
		}
		finally {
			JdbcUtils.closeStatement(stmt);
		}
		logger.info("All error/invalid workflow instances successfully queued for restart.");
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#notify(java.util.List, java.sql.Connection)
	 */
	@Override
	public void notify(List<Response<?>> responses, Connection c) throws Exception {
		if (responses.isEmpty()) 
			return;

		final PreparedStatement stmt = c.prepareCall("INSERT INTO COP_RESPONSE (CORRELATION_ID, RESPONSE_TS, RESPONSE, LONG_RESPONSE, RESPONSE_META_DATA, RESPONSE_TIMEOUT) VALUES (?,?,?,?,?,?)");
		try {
			final Timestamp now = new Timestamp(System.currentTimeMillis());
			int counter=0;
			for(Response<?> r : responses) {
				stmt.setString(1, r.getCorrelationId());
				stmt.setTimestamp(2, now);
				String payload = serializer.serializeResponse(r);
				stmt.setString(3, payload.length() > 4000 ? null : payload);
				stmt.setString(4, payload.length() > 4000 ? payload : null);
				stmt.setString(5, r.getMetaData());
				stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis() + (r.getInternalProcessingTimeout() == null ? defaultStaleResponseRemovalTimeout : r.getInternalProcessingTimeout())));
				stmt.addBatch();
				counter++;
				if (counter == 50) {
					stmt.executeBatch();
					stmt.clearBatch();
					counter = 0;
				}
			}
			if (counter != 0) {
				stmt.executeBatch();
			}
		}
		finally {
			JdbcUtils.closeStatement(stmt);
		}
	}	

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#createBatchCommand4Finish(de.scoopgmbh.copper.Workflow)
	 */
	@Override
	@SuppressWarnings({"rawtypes"})
	public BatchCommand createBatchCommand4Finish(final Workflow<?> w) {
		final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
		return new OracleRemove.Command(pwf,removeWhenFinished);
	}	

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#createBatchCommand4Notify(de.scoopgmbh.copper.Response)
	 */
	@Override
	@SuppressWarnings({"rawtypes"})
	public BatchCommand createBatchCommand4Notify(final Response<?> response) throws Exception {
		if (response == null)
			throw new NullPointerException();
		return new OracleNotify.Command(response, serializer, defaultStaleResponseRemovalTimeout);
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#createBatchCommand4registerCallback(de.scoopgmbh.copper.persistent.RegisterCall, de.scoopgmbh.copper.batcher.Batcher)
	 */
	@Override
	@SuppressWarnings({"rawtypes"})
	public BatchCommand createBatchCommand4registerCallback(final RegisterCall rc, final ScottyDBStorageInterface dbStorageInterface) throws Exception {
		if (rc == null) 
			throw new NullPointerException();
		return new OracleRegisterCallback.Command(rc, serializer, dbStorageInterface);
	}	

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.DatabaseDialect#createBatchCommand4error(de.scoopgmbh.copper.Workflow, java.lang.Throwable)
	 */
	@Override
	@SuppressWarnings({"rawtypes"})
	public BatchCommand createBatchCommand4error(Workflow<?> w, Throwable t, DBProcessingState dbProcessingState) {
		final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
		return new OracleSetToError.Command(pwf,t);
	}

	public void error(Workflow<?> w, Throwable t, Connection con) throws Exception {
		runSingleBatchCommand(con, createBatchCommand4error(w, t, DBProcessingState.ERROR));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void runSingleBatchCommand(Connection con, BatchCommand cmd) throws Exception {
		List<BatchCommand> commands = Collections.singletonList(cmd);
		cmd.executor().doExec(commands, con);
	}
	
	public List<String> checkDbConsistency(Connection con) throws Exception {
		if (multiEngineMode) 
			throw new CopperRuntimeException("Cannot check DB consistency when multiEngineMode is turned on!");
		
		final PreparedStatement dequeueStmt = con.prepareStatement("select id,priority,creation_ts,data,long_data,object_state,long_object_state,PPOOL_ID from COP_WORKFLOW_INSTANCE where state not in (?,?)");
		try {
			final List<String> idsOfBadWorkflows = new ArrayList<String>();
			dequeueStmt.setInt(1, DBProcessingState.INVALID.ordinal());
			dequeueStmt.setInt(2, DBProcessingState.FINISHED.ordinal());
			ResultSet rs = dequeueStmt.executeQuery();
			while(rs.next()) {
				final String id = rs.getString(1);
				try {
					final int prio = rs.getInt(2);
					String data = rs.getString(4);
					if (data == null) 
						data = rs.getString(5);
					String objectState = rs.getString(6);
					if (objectState == null) 
						objectState = rs.getString(7);
					final String ppoolId = rs.getString(8);
					final SerializedWorkflow sw = new SerializedWorkflow();
					sw.setData(data);
					sw.setObjectState(objectState);
					final PersistentWorkflow<?> wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
					wf.setId(id);
					wf.setProcessorPoolId(ppoolId);
					wf.setPriority(prio);
					logger.debug("Successful test deserialization of workflow {}",id);
				}
				catch(Exception e) {
					logger.warn("Test deserialization of workflow "+id+" failed: "+e.toString());
					idsOfBadWorkflows.add(id);
				}
			}
			return idsOfBadWorkflows;
		}
		finally {
			JdbcUtils.closeStatement(dequeueStmt);
		}
	}
	
	public void shutdown() {
		synchronized (responseLoaders) {
			for (ResponseLoader responseLoader : responseLoaders.values()) {
				responseLoader.shutdown();
			}
		}
	}
}
