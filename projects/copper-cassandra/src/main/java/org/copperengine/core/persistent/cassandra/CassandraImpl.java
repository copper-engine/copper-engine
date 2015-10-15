package org.copperengine.core.persistent.cassandra;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.copperengine.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraImpl implements Cassandra {

    private static final Logger logger = LoggerFactory.getLogger(CassandraImpl.class);

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
    public Iterator<CassandraWorkflow> readAllWorkflowInstances() throws Exception {
        return map.values().iterator();
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

}
