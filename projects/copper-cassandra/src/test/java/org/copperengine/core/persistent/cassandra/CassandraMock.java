package org.copperengine.core.persistent.cassandra;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.copperengine.core.ProcessingState;

public class CassandraMock implements Cassandra {

    private Map<String, CassandraWorkflow> map = new ConcurrentHashMap<String, CassandraWorkflow>();

    @Override
    public void safeWorkflowInstance(CassandraWorkflow cw) throws Exception {
        map.put(cw.id, cw);
    }

    @Override
    public void deleteWorkflowInstance(String wfId) throws Exception {
        map.remove(wfId);
    }

    @Override
    public CassandraWorkflow readCassandraWorkflow(String wfId) throws Exception {
        return map.get(wfId);
    }

    @Override
    public void initialize(InternalStorageAccessor internalStorageAccessor) throws Exception {

    }

    @Override
    public void updateWorkflowInstanceState(String wfId, ProcessingState state) throws Exception {
        map.get(wfId).state = state;
    }

    @Override
    public void safeEarlyResponse(String correlationId, String serializedResponse) throws Exception {

    }

    @Override
    public String readEarlyResponse(String correlationId) throws Exception {
        return null;
    }

    @Override
    public void deleteEarlyResponse(String correlationId) throws Exception {

    }

}
