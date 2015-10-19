package org.copperengine.core.persistent.cassandra;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.NullArgumentException;
import org.copperengine.core.ProcessingState;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.SerializedWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraImpl implements Cassandra {

    private static final Logger logger = LoggerFactory.getLogger(CassandraImpl.class);

    private static final String TABLE_PLACEHOLDER = "<table>";
    private static final String DEFAULT_TABLE_NAME = "COP_WORKFLOW_INSTANCE";
    private static final String CQL_UPD_WORKFLOW_INSTANCE_NOT_WAITING = "UPDATE <table> SET PPOOL_ID=?, PRIO=?, CREATION_TS=?, DATA=?, OBJECT_STATE=?, STATE=? WHERE ID=?";
    private static final String CQL_UPD_WORKFLOW_INSTANCE_WAITING = "UPDATE <table> SET PPOOL_ID=?, PRIO=?, CREATION_TS=?, DATA=?, OBJECT_STATE=?, WAIT_MODE=?, TIMEOUT=?, RESPONSE_MAP_JSON=?, STATE=? WHERE ID=?";
    private static final String CQL_UPD_WORKFLOW_INSTANCE_STATE = "UPDATE <table> SET STATE=? WHERE ID=?";
    private static final String CQL_DEL_WORKFLOW_INSTANCE_WAITING = "DELETE FROM <table> WHERE ID=?";
    private static final String CQL_SEL_WORKFLOW_INSTANCE_WAITING = "SELECT * FROM <table> WHERE ID=?";
    private static final String CQL_SEL_ALL_WORKFLOW_INSTANCES = "SELECT ID, PPOOL_ID, PRIO, WAIT_MODE, RESPONSE_MAP_JSON, STATE FROM <table>";

    private final Session session;
    private final Map<String, PreparedStatement> preparedStatements = new HashMap<>();
    private final JsonMapper jsonMapper = new JsonMapperImpl();

    public CassandraImpl(final CassandraSessionManager sessionManager) {
        if (sessionManager == null)
            throw new NullArgumentException("sessionManager");
        this.session = sessionManager.getSession();
        prepare(CQL_UPD_WORKFLOW_INSTANCE_NOT_WAITING);
        prepare(CQL_UPD_WORKFLOW_INSTANCE_WAITING);
        prepare(CQL_DEL_WORKFLOW_INSTANCE_WAITING);
        prepare(CQL_SEL_WORKFLOW_INSTANCE_WAITING);
        prepare(CQL_SEL_ALL_WORKFLOW_INSTANCES);
        prepare(CQL_UPD_WORKFLOW_INSTANCE_STATE);
    }

    private void prepare(String cql) {
        String replaced = cql.replace(TABLE_PLACEHOLDER, DEFAULT_TABLE_NAME);
        logger.info("Preparing cql stmt {}", replaced);
        preparedStatements.put(cql, session.prepare(replaced));
    }

    @Override
    public void safeWorkflowInstance(final CassandraWorkflow cw) throws Exception {
        logger.debug("safeWorkflow({})", cw);
        if (cw.cid2ResponseMap == null || cw.cid2ResponseMap.isEmpty()) {
            final PreparedStatement pstmt = preparedStatements.get(CQL_UPD_WORKFLOW_INSTANCE_NOT_WAITING);
            session.execute(pstmt.bind(cw.ppoolId, cw.prio, cw.creationTS, cw.serializedWorkflow.getData(), cw.serializedWorkflow.getObjectState(), cw.state.name(), cw.id));
        }
        else {
            final PreparedStatement pstmt = preparedStatements.get(CQL_UPD_WORKFLOW_INSTANCE_WAITING);
            final String responseMapJson = jsonMapper.toJSON(cw.cid2ResponseMap);
            session.execute(pstmt.bind(cw.ppoolId, cw.prio, cw.creationTS, cw.serializedWorkflow.getData(), cw.serializedWorkflow.getObjectState(), cw.waitMode.name(), cw.timeout, responseMapJson, cw.state.name(), cw.id));
        }
    }

    @Override
    public void deleteWorkflowInstance(String wfId) throws Exception {
        logger.debug("deleteWorkflowInstance({})", wfId);
        final PreparedStatement pstmt = preparedStatements.get(CQL_DEL_WORKFLOW_INSTANCE_WAITING);
        session.execute(pstmt.bind(wfId));
    }

    @Override
    public CassandraWorkflow readCassandraWorkflow(String wfId) throws Exception {
        logger.debug("readCassandraWorkflow({})", wfId);
        final PreparedStatement pstmt = preparedStatements.get(CQL_SEL_WORKFLOW_INSTANCE_WAITING);
        ResultSet rs = session.execute(pstmt.bind(wfId));
        Row row = rs.one();
        if (row == null) {
            logger.warn("No workflow instance with id {} found", wfId);
            return null;
        }
        final CassandraWorkflow cw = new CassandraWorkflow();
        cw.id = wfId;
        cw.ppoolId = row.getString("PPOOL_ID");
        cw.prio = row.getInt("PRIO");
        cw.creationTS = row.getDate("CREATION_TS");
        cw.timeout = row.getDate("TIMEOUT");
        cw.waitMode = toWaitMode(row.getString("WAIT_MODE"));
        cw.serializedWorkflow = new SerializedWorkflow();
        cw.serializedWorkflow.setData(row.getString("DATA"));
        cw.serializedWorkflow.setObjectState(row.getString("OBJECT_STATE"));
        cw.cid2ResponseMap = toResponseMap(row.getString("RESPONSE_MAP_JSON"));
        cw.state = ProcessingState.valueOf(row.getString("STATE"));
        return cw;
    }

    @Override
    public void safeEarlyResponse(Response<?> r) throws Exception {
        throw new UnsupportedOperationException();

    }

    @Override
    public Response<?> readEarlyResponse(String cid) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteEarlyResponse(String cid) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialize(InternalStorageAccessor internalStorageAccessor) throws Exception {
        final long startTS = System.currentTimeMillis();
        final ResultSet rs = session.execute(preparedStatements.get(CQL_SEL_ALL_WORKFLOW_INSTANCES).bind());
        long counter = 0;
        Row row;
        while ((row = rs.one()) != null) {
            final String wfId = row.getString("ID");
            final String ppoolId = row.getString("PPOOL_ID");
            final int prio = row.getInt("PRIO");
            final WaitMode waitMode = toWaitMode(row.getString("WAIT_MODE"));
            final Map<String, String> responseMap = toResponseMap(row.getString("RESPONSE_MAP_JSON"));
            final ProcessingState state = ProcessingState.valueOf(row.getString("STATE"));
            if (state == ProcessingState.ERROR)
                continue;

            if (waitMode == null) {
                internalStorageAccessor.enqueue(wfId, ppoolId, prio);
            }
            else {
                int numberOfAvailableResponses = 0;
                for (Entry<String, String> e : responseMap.entrySet()) {
                    internalStorageAccessor.registerCorrelationId(e.getKey(), wfId);
                    if (e.getValue() != null)
                        numberOfAvailableResponses++;
                }
                if (numberOfAvailableResponses == responseMap.size() || (numberOfAvailableResponses == 1 && waitMode == WaitMode.FIRST)) {
                    internalStorageAccessor.enqueue(wfId, ppoolId, prio);
                }
            }
            counter++;
        }
        logger.info("Read {} rows in {} msec", counter, System.currentTimeMillis() - startTS);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> toResponseMap(String v) {
        return v == null ? null : jsonMapper.fromJSON(v, HashMap.class);
    }

    private WaitMode toWaitMode(String v) {
        return v == null ? null : WaitMode.valueOf(v);
    }

    @Override
    public void updateWorkflowInstanceState(String wfId, ProcessingState state) throws Exception {
        logger.debug("updateWorkflowInstanceState({}, {})", wfId, state);
        session.execute(preparedStatements.get(CQL_UPD_WORKFLOW_INSTANCE_STATE).bind(wfId, state.name()));
    }
}
