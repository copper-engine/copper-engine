package org.copperengine.core.persistent.hybrid;

/**
 * Used just for initialization of {@link HybridDBStorage} during startup
 * 
 * @author austermann
 *
 */
public interface HybridDBStorageAccessor {

    public void enqueue(String wfId, String ppoolId, int prio);

    public void registerCorrelationId(String correlationId, String wfId);

}
