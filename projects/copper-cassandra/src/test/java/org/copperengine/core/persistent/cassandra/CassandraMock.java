package org.copperengine.core.persistent.cassandra;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.copperengine.core.ProcessingState;
import org.copperengine.core.persistent.hybrid.HybridDBStorageAccessor;
import org.copperengine.core.persistent.hybrid.Storage;
import org.copperengine.core.persistent.hybrid.WorkflowInstance;

import com.google.common.util.concurrent.ListenableFuture;

public class CassandraMock implements Storage {

    private Map<String, WorkflowInstance> map = new ConcurrentHashMap<String, WorkflowInstance>();

    @Override
    public void safeWorkflowInstance(WorkflowInstance cw) throws Exception {
        map.put(cw.id, cw);
    }

    @Override
    public ListenableFuture<Void> deleteWorkflowInstance(String wfId) throws Exception {
        map.remove(wfId);
        return null;
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
    public ListenableFuture<Void> safeEarlyResponse(String correlationId, String serializedResponse) throws Exception {
        return null;
    }

    @Override
    public String readEarlyResponse(String correlationId) throws Exception {
        return null;
    }

    @Override
    public ListenableFuture<Void> deleteEarlyResponse(String correlationId) throws Exception {
        return null;
    }

}
