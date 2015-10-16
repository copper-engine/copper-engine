package org.copperengine.core.persistent.cassandra;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.copperengine.core.Response;

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

    }

}
