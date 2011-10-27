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
package de.scoopgmbh.copper.persistent;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.EngineIdProvider;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.batcher.Batcher;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.db.utility.RetryingTransaction;
import de.scoopgmbh.copper.monitoring.NullRuntimeStatisticsCollector;
import de.scoopgmbh.copper.monitoring.RuntimeStatisticsCollector;
import de.scoopgmbh.copper.monitoring.StmtStatistic;

/**
 * Oracle implementation of the {@link ScottyDBStorageInterface}.
 * It supports multiple engines (cluster) connected to one database. 
 * 
 * @author austermann
 *
 */
public class OracleScottyDBStorage implements ScottyDBStorageInterface {

	private static final Logger logger = Logger.getLogger(OracleScottyDBStorage.class);

	private StmtStatistic dequeueAllStmtStatistic;
	private StmtStatistic dequeueQueryBPsStmtStatistic;
	private StmtStatistic dequeueQueryResponsesStmtStatistic;
	private StmtStatistic dequeueMarkStmtStatistic;
	private StmtStatistic enqueueUpdateStateStmtStatistic;
	private StmtStatistic insertStmtStatistic;
	private StmtStatistic deleteStaleResponsesStmtStatistic;
	private StmtStatistic dequeueWait4RespLdrStmtStatistic;

	private WorkflowRepository wfRepository;
	private Thread enqueueThread;
	private ScheduledExecutorService scheduledExecutorService;
	private long deleteStaleResponsesIntervalMsec = 60L*60L*1000L;
	private boolean shutdown = false;
	//private ResponseLoader responseLoader;
	private DataSource dataSource;
	private Batcher batcher;
	private boolean multiEngineMode = true;
	private int lockWaitSeconds = 10;
	private RuntimeStatisticsCollector runtimeStatisticsCollector = new NullRuntimeStatisticsCollector();
	private Serializer serializer = new StandardJavaSerializer();
	private EngineIdProvider engineIdProvider = null;
	private Map<String, ResponseLoader> responseLoaders = new HashMap<String, ResponseLoader>();

	public OracleScottyDBStorage() {

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
	
	public void setBatcher(Batcher batcher) {
		this.batcher = batcher;
	}
	
	public void setRuntimeStatisticsCollector(RuntimeStatisticsCollector runtimeStatisticsCollector) {
		this.runtimeStatisticsCollector = runtimeStatisticsCollector;
	}
	
	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#setWfRepository(de.scoopgmbh.copper.common.WorkflowRepository)
	 */
	public void setWfRepository(WorkflowRepository wfRepository) {
		this.wfRepository = wfRepository;
	}

	public void setDeleteStaleResponsesIntervalMsec(long deleteStaleResponsesIntervalMsec) {
		this.deleteStaleResponsesIntervalMsec = deleteStaleResponsesIntervalMsec;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
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

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#resumeBrokenBusinessProcesses()
	 */
	private void resumeBrokenBusinessProcesses() throws Exception {
		logger.info("resumeBrokenBusinessProcesses");
		new RetryingTransaction(dataSource) {
			@Override
			protected void execute() throws Exception {
				getConnection().setAutoCommit(true);

				logger.info("Reactivating queue entries...");
				PreparedStatement stmt = getConnection().prepareStatement("UPDATE cop_queue SET engine_id = null WHERE engine_id=?");
				stmt.setString(1, engineIdProvider.getEngineId());
				stmt.execute();
				stmt.close();
				logger.info("done!");
			}
		}.run();
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#insert(de.scoopgmbh.copper.Workflow)
	 */
	public void insert(final Workflow<?> wf) throws Exception {
		if (logger.isTraceEnabled()) logger.trace("insert("+wf+")");
		new RetryingTransaction(dataSource) {
			@Override
			protected void execute() throws Exception {
				doInsert(wf, getConnection());
			}
		}.run();
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#insert(java.util.List)
	 */
	public void insert(final List<Workflow<?>> wfs) throws Exception {
		if (logger.isTraceEnabled()) logger.trace("insert("+wfs.size()+")");
		new RetryingTransaction(dataSource) {
			@Override
			protected void execute() throws Exception {
				doInsert(wfs, getConnection());
			}
		}.run();
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#finish(de.scoopgmbh.copper.Workflow)
	 */
	public void finish(final Workflow<?> w) {
		if (logger.isTraceEnabled()) logger.trace("finish("+w.getId()+")");
		final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
		batcher.submitBatchCommand(new GenericRemove.Command(pwf,dataSource));
	}

	
	static void close(Connection c) {
		if (c == null) return;
		try {
			if (!c.isClosed())
				c.close();
		}
		catch(Exception e) {
			logger.warn("unable to close connection",e);
		}
	}
	
	
	@Deprecated
	public List<Workflow<?>> __dequeue(final String ppoolId, final int max) throws Exception {
		if (logger.isTraceEnabled()) logger.trace("dequeue("+ppoolId+", "+max+")");
		
//		Thread.sleep(1000);
//		if (1==1) return Collections.emptyList();

		long startTS = System.currentTimeMillis();

		final List<Workflow<?>> rv = new ArrayList<Workflow<?>>(max);
		new RetryingTransaction(dataSource) {
			@Override
			protected void execute() throws Exception {
				getConnection().setAutoCommit(true);
				final List<String> invalidBPs = new ArrayList<String>();
				final PreparedStatement dequeueStmt = getConnection().prepareStatement("select /*+ PARALLEL */ id,priority,data,rowid,long_data from COP_WORKFLOW_INSTANCE where rowid in (select * from (select WFI_ROWID from cop_queue where ppool_id=? order by ppool_id, priority, WFI_ROWID) where rownum <= ?)"); // TODO last_mod_ts
				final PreparedStatement deleteStmt = getConnection().prepareStatement("delete from cop_queue where ppool_id=? and priority=? and WFI_ROWID=?");
				dequeueStmt.setString(1, ppoolId);
				dequeueStmt.setInt(2, max);
				dequeueAllStmtStatistic.start();
				dequeueQueryBPsStmtStatistic.start();
				final ResultSet rs = dequeueStmt.executeQuery();
				dequeueQueryBPsStmtStatistic.stop(1);
				final Map<String,Workflow<?>> map = new HashMap<String, Workflow<?>>(max*3);
				while (rs.next()) {
					final String id = rs.getString(1);
					final int prio = rs.getInt(2);
					final String rowid = rs.getString(4);
						
					deleteStmt.setString(1, ppoolId);
					deleteStmt.setInt(2, prio);
					deleteStmt.setString(3, rowid);
					// TODO last_mod_ts fehlt noch...
					deleteStmt.addBatch();
					
					try {
						String data = rs.getString(3);
						if (data == null) 
							data = rs.getString(5);
						PersistentWorkflow<?> wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(data, wfRepository);
						wf.setId(id);
						wf.setProcessorPoolId(ppoolId);
						wf.setPriority(prio);
						wf.rowid = rowid;
						map.put(wf.getId(), wf);
					}
					catch(Exception e) {
						logger.error("decoding of '"+id+"' failed: "+e.toString(),e);
						invalidBPs.add(id);
					}
				}
				rs.close();
				dequeueStmt.close();

				if (map.isEmpty()) {
					return;
				}

				PreparedStatement stmt = getConnection().prepareStatement("select w.WORKFLOW_INSTANCE_ID, w.correlation_id, r.response, r.long_response from (select WORKFLOW_INSTANCE_ID, correlation_id from cop_wait where WORKFLOW_INSTANCE_ID in (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)) w, cop_response r where w.correlation_id = r.correlation_id");
				List<List<String>> ids = splitt(map.keySet(),50); 
				for (List<String> id : ids) {
					int n=0;
					stmt.clearParameters();
					for (int i=0; i<50; i++) {
						stmt.setString(i+1, id.size() >= i+1 ? id.get(i) : null);
					}
					dequeueQueryResponsesStmtStatistic.start();					
					ResultSet rsResponses = stmt.executeQuery();
					while (rsResponses.next()) {
						String bpId = rsResponses.getString(1);
						String cid = rsResponses.getString(2);
						String response = rsResponses.getString(3);
						if (response == null) rsResponses.getString(4);
						PersistentWorkflow<?> wf = (PersistentWorkflow<?>) map.get(bpId);
						Response<?> r;
						if (response != null) {
							r = (Response<?>) serializer.deserializeResponse(response);
						}
						else {
							// timeout
							r = new Response<Object>(cid);
						}
						wf.putResponse(r);
						if (wf.cidList == null) wf.cidList = new ArrayList<String>();
						wf.cidList.add(cid);
						++n;
					}
					rsResponses.close();
					dequeueQueryResponsesStmtStatistic.stop(n);					
				}
				dequeueMarkStmtStatistic.start();
				deleteStmt.executeBatch();
				dequeueMarkStmtStatistic.stop(map.size());

				rv.addAll(map.values());
				
				dequeueAllStmtStatistic.stop(map.size());

				if (!invalidBPs.isEmpty()) {
					PreparedStatement updateBpStmt = getConnection().prepareStatement("update COP_WORKFLOW_INSTANCE set state=? where id=?");
					for (String id : invalidBPs) {
						updateBpStmt.setInt(1, DBProcessingState.INVALID.ordinal());
						updateBpStmt.setString(2,id);
						updateBpStmt.addBatch();
					}
					updateBpStmt.executeBatch();
					// ggf. auch die Waits und Responses löschen
				}
			}
		}.run();
		if (logger.isTraceEnabled()) logger.trace("dequeue for pool "+ppoolId+" returns "+rv.size()+" element(s)");

		if (logger.isDebugEnabled()) logger.debug("dequeued "+rv.size()+" BPs in "+(System.currentTimeMillis()-startTS));
		return rv;
	}

	
	
	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#dequeue(java.lang.String, int)
	 */
	public List<Workflow<?>> dequeue(final String ppoolId, final int max) throws Exception {
		if (logger.isTraceEnabled()) logger.trace("dequeue("+ppoolId+", "+max+")");
		
//		Thread.sleep(1000);
//		if (1==1) return Collections.emptyList();

		final long startTS = System.currentTimeMillis();
		final List<Workflow<?>> rv = new ArrayList<Workflow<?>>(max);
		new RetryingTransaction(dataSource) {
			@Override
			protected void execute() throws Exception {
				getConnection().setAutoCommit(false);
				
				lock(getConnection(),"dequeue#"+ppoolId);

				ResponseLoader responseLoader = getResponseLoader(ppoolId);
				responseLoader.setCon(getConnection());
				responseLoader.setSerializer(serializer);
				responseLoader.setEngineId(engineIdProvider.getEngineId());
				responseLoader.beginTxn();
				
				final List<String> invalidBPs = new ArrayList<String>();
				final PreparedStatement dequeueStmt = getConnection().prepareStatement("select id,priority,data,rowid,long_data from COP_WORKFLOW_INSTANCE where rowid in (select * from (select WFI_ROWID from cop_queue where ppool_id=? and engine_id is null order by ppool_id, priority, last_mod_ts) where rownum <= ?)"); 
				dequeueStmt.setString(1, ppoolId);
				dequeueStmt.setInt(2, max);
				dequeueAllStmtStatistic.start();
				dequeueQueryBPsStmtStatistic.start();
				final ResultSet rs = dequeueStmt.executeQuery();
				dequeueQueryBPsStmtStatistic.stop(1);
				final Map<String,Workflow<?>> map = new HashMap<String, Workflow<?>>(max*3);
				while (rs.next()) {
					final String id = rs.getString(1);
					final int prio = rs.getInt(2);
					final String rowid = rs.getString(4);
					try {
						String data = rs.getString(3);
						if (data == null) 
							data = rs.getString(5);
						PersistentWorkflow<?> wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(data, wfRepository);
						wf.setId(id);
						wf.setProcessorPoolId(ppoolId);
						wf.setPriority(prio);
						wf.rowid = rowid;
						wf.oldPrio = prio;
						wf.oldProcessorPoolId = ppoolId;
						map.put(wf.getId(), wf);
						responseLoader.enqueue(wf);
					}
					catch(Exception e) {
						logger.error("decoding of '"+id+"' failed: "+e.toString(),e);
						invalidBPs.add(id);
					}
				}
				rs.close();
				dequeueStmt.close();

				if (map.isEmpty()) {
					return;
				}

				dequeueWait4RespLdrStmtStatistic.start();
				responseLoader.endTxn();
				dequeueWait4RespLdrStmtStatistic.stop(map.size());
				
				rv.addAll(map.values());
				
				dequeueAllStmtStatistic.stop(map.size());

				if (!invalidBPs.isEmpty()) {
					PreparedStatement updateBpStmt = getConnection().prepareStatement("update COP_WORKFLOW_INSTANCE set state=? where id=?");
					for (String id : invalidBPs) {
						updateBpStmt.setInt(1, DBProcessingState.INVALID.ordinal());
						updateBpStmt.setString(2,id);
						updateBpStmt.addBatch();
					}
					updateBpStmt.executeBatch();
					// ggf. auch die Waits und Responses löschen?
				}
			}
		}.run();
		if (logger.isDebugEnabled()) logger.debug("dequeue for pool "+ppoolId+" returns "+rv.size()+" element(s) in "+(System.currentTimeMillis()-startTS)+" msec.");
		return rv;
	}

	private int updateQueueState(final int max) {
		final int[] rowcount = { 0 };
		final long startTS = System.currentTimeMillis();
		try {
			new RetryingTransaction(dataSource) {
				@Override
				protected void execute() throws Exception {
					getConnection().setAutoCommit(false);
					lock(getConnection(), "updateQueueState");
					enqueueUpdateStateStmtStatistic.start();
					CallableStatement stmt = getConnection().prepareCall("begin COP_COREENGINE.enqueue(?,?); end;");
					stmt.setInt(1, max);
					stmt.registerOutParameter(2, Types.INTEGER);
					stmt.execute();
					rowcount[0] = stmt.getInt(2);
					enqueueUpdateStateStmtStatistic.stop(rowcount[0] == 0 ? 1 : rowcount[0]);
				}
			}.run();
		} 
		catch (Exception e) {
			logger.error("",e);
		}
		logger.info("Queue update in "+(System.currentTimeMillis()-startTS)+" msec");
		return rowcount[0];
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

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#notify(de.scoopgmbh.copper.Response, java.lang.Object)
	 */
	public void notify(final Response<?> response, final Object callback) throws Exception {
		if (logger.isTraceEnabled()) logger.trace("notify("+response+")");
		if (response == null)
			throw new NullPointerException();
		batcher.submitBatchCommand(new GenericNotify.Command(response, dataSource, serializer));
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#notify(java.util.List)
	 */
	public void notify(final List<Response<?>> response) throws Exception {
		for (Response<?> r : response)
			notify(r,null);
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#registerCallback(de.scoopgmbh.copper.persistent.RegisterCall)
	 */
	public void registerCallback(final RegisterCall rc) throws Exception {
		if (logger.isTraceEnabled()) logger.trace("registerCallback("+rc+")");
		if (rc == null) 
			throw new NullPointerException();
		batcher.submitBatchCommand(new GenericRegisterCallback.Command(rc, dataSource, serializer));
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#startup()
	 */
	public synchronized void startup() {
		try {
			if (engineIdProvider == null || engineIdProvider.getEngineId() == null) throw new NullPointerException("EngineId is NULL! Change your OracleScottyDBStorage configuration.");
			
			initStmtStats();
			
			deleteStaleResponse();
			resumeBrokenBusinessProcesses();
			
			enqueueThread = new Thread("ENQUEUE") {
				@Override
				public void run() {
					updateQueueState();
				}
			};
			enqueueThread.start();

			scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
			
			scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					try {
						deleteStaleResponse();
					} 
					catch (Exception e) {
						logger.error("deleteStaleResponse failed",e);
					}
				}
			}, deleteStaleResponsesIntervalMsec, deleteStaleResponsesIntervalMsec, TimeUnit.MILLISECONDS);
		}
		catch(Exception e) {
			throw new Error("Unable to startup",e);
		}
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#shutdown()
	 */
	public synchronized void shutdown() {
		if (shutdown)
			return;
		
		shutdown = true;
		enqueueThread.interrupt();
		scheduledExecutorService.shutdown();
		synchronized (responseLoaders) {
			for (ResponseLoader responseLoader : responseLoaders.values()) {
				responseLoader.shutdown();
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.persistent.ScottyDBStorageInterface#deleteStaleResponse(java.sql.Timestamp)
	 */
	private void deleteStaleResponse() throws Exception {
		if (logger.isTraceEnabled()) logger.trace("deleteStaleResponse()");

		final int[] n = { 0 };
		final int MAX_ROWS = 20000;
		do {
			new RetryingTransaction(dataSource) {
				@Override
				protected void execute() throws Exception {
					getConnection().setAutoCommit(false);
					lock(getConnection(),"deleteStaleResponse");
					
					PreparedStatement stmt = getConnection().prepareStatement("delete from cop_response r where response_ts < ? and not exists (select * from cop_wait w where w.correlation_id = r.correlation_id) and rownum <= "+MAX_ROWS);
					stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()-60L*60L*1000L));
					deleteStaleResponsesStmtStatistic.start();
					n[0] = stmt.executeUpdate();
					deleteStaleResponsesStmtStatistic.stop(n[0]);
					if (logger.isTraceEnabled()) logger.trace("deleted "+n+" stale response(s).");
				}
			}.run();
		}
		while(n[0] == MAX_ROWS);
	}

	private void updateQueueState() {
		final int max = 5000;
		logger.info("started");
		while(!shutdown) {
			int x=0;
			try {
				x = updateQueueState(max);
			}
			catch(Exception e) {
				logger.error("updateQueueState failed",e);
			}
			if (x == 0) {
				try {
					Thread.sleep(2000);
				} 
				catch (InterruptedException e) {
					//ignore
				}
			}
			else if (x < max) {
				try {
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) {
					//ignore
				}
			}
		}
		logger.info("finished");
	}
	
	private void lock(Connection c, String context) throws SQLException {
		if (!multiEngineMode)
			return;
		final int lockId = Math.abs(context.hashCode()) % 1073741823;
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

	@Override
	public void insert(Workflow<?> wf, Connection con) throws Exception {
		if (con == null) {
			insert(wf);
		}
		else {
			doInsert(wf, con);
		}
	}

	@Override
	public void insert(List<Workflow<?>> wfs, Connection con) throws Exception {
		if (con == null) {
			insert(wfs);
		}
		else {
			doInsert(wfs, con);
		}
	}
	
	private void doInsert(final List<Workflow<?>> wfs, final Connection con) throws Exception {
		con.setAutoCommit(true);
		final PreparedStatement stmt = con.prepareStatement("INSERT INTO COP_WORKFLOW_INSTANCE (ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,DATA,LONG_DATA) VALUES (?,?,?,SYSTIMESTAMP,?,?,?)");
		try {
			int n = 0;
			for (int i=0; i<wfs.size(); i++) {
				Workflow<?> wf = wfs.get(i);
				final String data = serializer.serializeWorkflow(wf);
				stmt.setString(1, wf.getId());
				stmt.setInt(2, DBProcessingState.ENQUEUED.ordinal());
				stmt.setInt(3, wf.getPriority());
				stmt.setString(4, wf.getProcessorPoolId());
				stmt.setString(5, data.length() > 4000 ? null : data);
				stmt.setString(6, data.length() > 4000 ? data : null);
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
			stmt.close();
		}
	}
	
	private void doInsert(final Workflow<?> wf, final Connection con) throws Exception {
		con.setAutoCommit(true);
		final String data = serializer.serializeWorkflow(wf);
		final PreparedStatement stmt = con.prepareStatement("INSERT INTO COP_WORKFLOW_INSTANCE (ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,DATA,LONG_DATA) VALUES (?,?,?,SYSTIMESTAMP,?,?,?)");
		try {
			stmt.setString(1, wf.getId());
			stmt.setInt(2, DBProcessingState.ENQUEUED.ordinal());
			stmt.setInt(3, wf.getPriority());
			stmt.setString(4, wf.getProcessorPoolId());
			stmt.setString(5, data.length() > 4000 ? null : data);
			stmt.setString(6, data.length() > 4000 ? data : null);
			stmt.execute();
		}
		finally {
			stmt.close();
		}

	}

	@Override
	public void error(Workflow<?> w, Throwable t) {
		if (logger.isTraceEnabled()) logger.trace("error("+w.getId()+","+t.toString()+")");
		final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
		batcher.submitBatchCommand(new GenericSetToError.Command(pwf,dataSource,t));
	}
	
	public void restart(final String workflowInstanceId) throws Exception {
		logger.trace("restart("+workflowInstanceId+")");
		new RetryingTransaction(dataSource) {
			@Override
			protected void execute() throws Exception {
				CallableStatement stmt = getConnection().prepareCall("begin COP_COREENGINE.restart(?); end;");
				stmt.setString(1, workflowInstanceId);
				stmt.execute();
			}
		}.run();
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
	

}
