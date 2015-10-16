package org.copperengine.core.persistent.cassandra;

public interface InternalStorageAccessor {
    public void enqueue(String wfId, String ppoolId, int prio);

    public void registerCorrelationId(String correlationId, String wfId);
}
