package org.copperengine.core.persistent;

import java.sql.Connection;

interface ResponseLoader {

    public void enqueue(PersistentWorkflow<?> wf);

    public void shutdown();

    public void start();

    public void beginTxn();

    public void endTxn();

    public void setCon(Connection con);

    public void setSerializer(Serializer serializer);

    public void setEngineId(String engineId);
}
