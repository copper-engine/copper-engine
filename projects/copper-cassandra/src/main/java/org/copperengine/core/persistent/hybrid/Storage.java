package org.copperengine.core.persistent.hybrid;

import org.copperengine.core.ProcessingState;

import com.google.common.util.concurrent.ListenableFuture;

public interface Storage {

    public void safeWorkflowInstance(WorkflowInstance cw) throws Exception;

    public ListenableFuture<Void> deleteWorkflowInstance(String wfId) throws Exception;

    public WorkflowInstance readWorkflowInstance(String wfId) throws Exception;

    public void initialize(HybridDBStorageAccessor internalStorageAccessor) throws Exception;

    public ListenableFuture<Void> safeEarlyResponse(String correlationId, String serializedResponse) throws Exception;

    public String readEarlyResponse(String correlationId) throws Exception;

    public ListenableFuture<Void> deleteEarlyResponse(String correlationId) throws Exception;

    public ListenableFuture<Void> updateWorkflowInstanceState(String wfId, ProcessingState state) throws Exception;

}
