package org.copperengine.core.persistent.cassandra;

import java.sql.Connection;
import java.util.List;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.DuplicateIdException;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.persistent.RegisterCall;
import org.copperengine.core.persistent.ScottyDBStorageInterface;

public class CassandraStorage implements ScottyDBStorageInterface {

    @Override
    public void insert(Workflow<?> wf, Acknowledge ack) throws DuplicateIdException, Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void insert(List<Workflow<?>> wfs, Acknowledge ack) throws DuplicateIdException, Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void insert(Workflow<?> wf, Connection con) throws DuplicateIdException, Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void insert(List<Workflow<?>> wfs, Connection con) throws DuplicateIdException, Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void finish(Workflow<?> w, Acknowledge callback) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Workflow<?>> dequeue(String ppoolId, int max) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void notify(Response<?> response, Acknowledge ack) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void notify(List<Response<?>> response, Acknowledge ack) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void notify(List<Response<?>> responses, Connection c) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerCallback(RegisterCall rc, Acknowledge callback) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void startup() {
        // TODO Auto-generated method stub

    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(Workflow<?> w, Throwable t, Acknowledge callback) {
        // TODO Auto-generated method stub

    }

    @Override
    public void restart(String workflowInstanceId) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRemoveWhenFinished(boolean removeWhenFinished) {
        // TODO Auto-generated method stub

    }

    @Override
    public void restartAll() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public Workflow<?> read(String workflowInstanceId) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
