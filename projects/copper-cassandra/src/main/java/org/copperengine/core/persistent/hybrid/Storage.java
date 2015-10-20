package org.copperengine.core.persistent.hybrid;

import org.copperengine.core.ProcessingState;

public interface Storage {

    public void safeWorkflowInstance(WorkflowInstance cw) throws Exception;

    public void deleteWorkflowInstance(String wfId) throws Exception;

    public WorkflowInstance readCassandraWorkflow(String wfId) throws Exception;

    public void initialize(HybridDBStorageAccessor internalStorageAccessor) throws Exception;

    public void safeEarlyResponse(String correlationId, String serializedResponse) throws Exception;

    public String readEarlyResponse(String correlationId) throws Exception;

    public void deleteEarlyResponse(String correlationId) throws Exception;

    public void updateWorkflowInstanceState(String wfId, ProcessingState state) throws Exception;

}
