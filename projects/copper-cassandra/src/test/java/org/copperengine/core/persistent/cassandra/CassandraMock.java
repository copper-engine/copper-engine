package org.copperengine.core.persistent.cassandra;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.copperengine.core.ProcessingState;
import org.copperengine.core.persistent.hybrid.Storage;
import org.copperengine.core.persistent.hybrid.WorkflowInstance;
import org.copperengine.core.persistent.hybrid.HybridDBStorageAccessor;

public class CassandraMock implements Storage {

    private Map<String, WorkflowInstance> map = new ConcurrentHashMap<String, WorkflowInstance>();

    @Override
    public void safeWorkflowInstance(WorkflowInstance cw) throws Exception {
        map.put(cw.id, cw);
    }

    @Override
    public void deleteWorkflowInstance(String wfId) throws Exception {
        map.remove(wfId);
    }

    @Override
    public WorkflowInstance readCassandraWorkflow(String wfId) throws Exception {
        return map.get(wfId);
    }

    @Override
    public void initialize(HybridDBStorageAccessor internalStorageAccessor) throws Exception {

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
