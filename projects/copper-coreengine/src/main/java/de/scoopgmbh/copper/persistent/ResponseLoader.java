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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.monitoring.StmtStatistic;

class ResponseLoader extends ConcurrentBatchedWorker {

	private static final Logger logger = Logger.getLogger(ResponseLoader.class);

	private Connection con;
	private Serializer serializer;
	private final StmtStatistic statResponse;
	private final StmtStatistic statQueue;
	
	public ResponseLoader(StmtStatistic statResponse, StmtStatistic statQueue) {
		super();
		this.statResponse = statResponse;
		this.statQueue = statQueue;
	}
	
	public void setCon(Connection con) {
		this.con = con;
	}
	
	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	@Override
	void process(final List<PersistentWorkflow<?>> list) {
		if (flushSize != 50) throw new RuntimeException();
		if (list.size() > 50) throw new RuntimeException();
		if (list.isEmpty())
			return;

		try {
			loadResponses(list);
			deleteQueueEntries(list);
		} 
		catch (Exception e) {
			logger.error("process failed",e);
		}

	}
	
	private void deleteQueueEntries(final List<PersistentWorkflow<?>> list) throws SQLException {
		final PreparedStatement deleteStmt = con.prepareStatement("delete from cop_queue where ppool_id=? and priority=? and WFI_ROWID=?");
		try {
			for (PersistentWorkflow<?> wf : list) {
				deleteStmt.setString(1, wf.getProcessorPoolId());
				deleteStmt.setInt(2,wf.getPriority());
				deleteStmt.setString(3, wf.rowid);
				deleteStmt.addBatch();
			}
			statQueue.start();
			deleteStmt.executeBatch();
			statQueue.stop(list.size());
		}
		finally {
			deleteStmt.close();
		}
	}

	private void loadResponses(final List<PersistentWorkflow<?>> list) 	throws Exception {
		Map<String,PersistentWorkflow<?>> map = new HashMap<String, PersistentWorkflow<?>>(flushSize*3);
		for (PersistentWorkflow<?> wf : list) {
			map.put(wf.getId(), wf);
		}
		PreparedStatement stmt = con.prepareStatement("select w.WORKFLOW_INSTANCE_ID, w.correlation_id, r.response, r.long_response from (select WORKFLOW_INSTANCE_ID, correlation_id from cop_wait where WORKFLOW_INSTANCE_ID in (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)) w, cop_response r where w.correlation_id = r.correlation_id(+)");
		try {
			for (int i=0; i<flushSize; i++) {
				stmt.setString(i+1, list.size() >= i+1 ? list.get(i).getId() : null);
			}
			statResponse.start();
			int n=0;
			final ResultSet rsResponses = stmt.executeQuery();
			while (rsResponses.next()) {
				String bpId = rsResponses.getString(1);
				String cid = rsResponses.getString(2);
				String response = rsResponses.getString(3);
				if (response == null) response = rsResponses.getString(4);
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
			statResponse.stop(n);
		}
		finally {
			stmt.close();
		}
	}

}
