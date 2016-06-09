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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.Response;
import org.copperengine.core.monitoring.StmtStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DownstreamResponseLoader implements ResponseLoader {

    private static final Logger logger = LoggerFactory.getLogger(DownstreamResponseLoader.class);

    private final StmtStatistic statResponse;
    private final StmtStatistic statQueue;

    private Connection con;
    private Serializer serializer;
    private String engineId;

    public DownstreamResponseLoader(StmtStatistic statResponse, StmtStatistic statQueue) {
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

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    private void markQueueEntries(final List<PersistentWorkflow<?>> list) throws SQLException {
        final PreparedStatement updateStmt = con.prepareStatement("update COP_QUEUE set engine_id = ? where ppool_id=? and priority=? and WFI_ROWID=?");
        try {
            for (PersistentWorkflow<?> wf : list) {
                updateStmt.setString(1, engineId);
                updateStmt.setString(2, wf.getProcessorPoolId());
                updateStmt.setInt(3, wf.getPriority());
                updateStmt.setString(4, wf.rowid);
                updateStmt.addBatch();
            }
            statQueue.start();
            updateStmt.executeBatch();
            statQueue.stop(list.size());
        } finally {
            updateStmt.close();
        }
    }

    private void loadResponses(final List<PersistentWorkflow<?>> list) throws Exception {
        Map<String, PersistentWorkflow<?>> map = new HashMap<String, PersistentWorkflow<?>>(50 * 3);
        for (PersistentWorkflow<?> wf : list) {
            map.put(wf.getId(), wf);
        }
        PreparedStatement stmt = con.prepareStatement("select w.WORKFLOW_INSTANCE_ID, w.correlation_id, r.response, r.long_response, w.is_timed_out from (select WORKFLOW_INSTANCE_ID, correlation_id, case when timeout_ts < systimestamp then 1 else 0 end is_timed_out from COP_WAIT where WORKFLOW_INSTANCE_ID in (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)) w, COP_RESPONSE r where w.correlation_id = r.correlation_id(+) order by r.correlation_id");
        try {
            for (int i = 0; i < 50; i++) {
                stmt.setString(i + 1, list.size() >= i + 1 ? list.get(i).getId() : null);
            }
            statResponse.start();
            int n = 0;
            final ResultSet rsResponses = stmt.executeQuery();
            while (rsResponses.next()) {
                String bpId = rsResponses.getString(1);
                String cid = rsResponses.getString(2);
                String response = rsResponses.getString(3);
                if (response == null)
                    response = rsResponses.getString(4);
                boolean isTimedOut = rsResponses.getInt(5) == 1;
                PersistentWorkflow<?> wf = (PersistentWorkflow<?>) map.get(bpId);
                Response<?> r = null;
                if (response != null) {
                    r = (Response<?>) serializer.deserializeResponse(response);
                    wf.addResponseId(r.getResponseId());
                } else if (isTimedOut) {
                    // timeout
                    r = new Response<Object>(cid);
                }
                if (r != null) {
                    wf.putResponse(r);
                }
                wf.addWaitCorrelationId(cid);
                ++n;
            }
            rsResponses.close();
            statResponse.stop(n);
        } finally {
            stmt.close();
        }
    }

    private List<PersistentWorkflow<?>> list = new ArrayList<>();

    public void enqueue(PersistentWorkflow<?> wf) {
        list.add(wf);
    }

    public void shutdown() {

    }

    public void start() {

    }

    public void beginTxn() {
        list.clear();
    }

    public void endTxn() {
        try {
            logger.trace("marking queue entries...");
            markQueueEntries(list);

            logger.trace("loading responses...");
            final int MAX = 50;
            List<PersistentWorkflow<?>> sublist = new ArrayList<>(MAX);
            for (PersistentWorkflow<?> wf : list) {
                sublist.add(wf);
                if (sublist.size() == MAX) {
                    loadResponses(sublist);
                    sublist.clear();
                }
            }
            if (!sublist.isEmpty()) {
                loadResponses(sublist);
                sublist.clear();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperRuntimeException(e);
        }

    }
}
