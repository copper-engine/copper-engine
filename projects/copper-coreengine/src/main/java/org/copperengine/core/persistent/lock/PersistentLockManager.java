package org.copperengine.core.persistent.lock;

/**
 * A service to obtain/manager persistent locks, e.g. to functionally synchronize workflow instances.
 * 
 * @author austermann
 * 
 */
public interface PersistentLockManager {

    /**
     * Acquires a lock with the specified id. As soon as this lock is asigned to the caller, the lock manager creates a
     * {@link Response} for the specified correlationId, containing the {@link PersistentLockResult}.
     * 
     * @param lockId
     *        symbolic lock id
     * @param correlationId
     *        correlationId for retrieving the COPPER {@link Response}
     * @param workflowInstanceId
     *        requestor/owner of this lock
     * @throws Exception
     *         in case of technical problems
     */
    void acquireLock(String lockId, String correlationId, String workflowInstanceId) throws Exception;

    /**
     * Releases the specified lock. If the workflow with the specified workflowId is not yet the owner of the lock (i.e.
     * it is still waiting to retrieve the lock), the acquireLock request is removed from the queue.
     * 
     * @param lockId
     *        symbolic lock id
     * @param workflowInstanceId
     *        requestor/owner of this lock
     * @throws Exception
     *         in case of technical problems
     */
    void releaseLock(String lockId, String workflowInstanceId) throws Exception;

}
