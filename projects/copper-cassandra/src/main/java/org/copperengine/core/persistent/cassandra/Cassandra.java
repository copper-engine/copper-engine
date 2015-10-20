package org.copperengine.core.persistent.cassandra;

import org.copperengine.core.ProcessingState;

public interface Cassandra {

    public void safeWorkflowInstance(CassandraWorkflow cw) throws Exception;

    public void deleteWorkflowInstance(String wfId) throws Exception;

    public CassandraWorkflow readCassandraWorkflow(String wfId) throws Exception;

    public void initialize(InternalStorageAccessor internalStorageAccessor) throws Exception;

    public void safeEarlyResponse(String correlationId, String serializedResponse) throws Exception;

    public String readEarlyResponse(String correlationId) throws Exception;

    public void deleteEarlyResponse(String correlationId) throws Exception;

    public void updateWorkflowInstanceState(String wfId, ProcessingState state) throws Exception;

}
