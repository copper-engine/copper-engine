package org.copperengine.core.persistent.cassandra;

import org.copperengine.core.ProcessingState;
import org.copperengine.core.Response;

public interface Cassandra {

    public void safeWorkflowInstance(CassandraWorkflow cw) throws Exception;

    public void deleteWorkflowInstance(String wfId) throws Exception;

    public CassandraWorkflow readCassandraWorkflow(String wfId) throws Exception;

    public void initialize(InternalStorageAccessor internalStorageAccessor) throws Exception;

    public void safeEarlyResponse(Response<?> r) throws Exception;

    public Response<?> readEarlyResponse(String cid) throws Exception;

    public void deleteEarlyResponse(String cid) throws Exception;

    public void updateWorkflowInstanceState(String wfId, ProcessingState state) throws Exception;

}
