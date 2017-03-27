/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.core.persistent.lock;

import org.copperengine.core.Response;

/**
 * A service to obtain/manager persistent locks, e.g. to functionally synchronize workflow instances.
 * 
 * @author austermann
 * 
 */
public interface PersistentLockManager {

    /**
     * Acquires a lock with the specified id. If the lock is free, then the lock is assigned to the caller and the
     * methods return <code>null</code>.
     * <p>
     * Otherwise, if the lock is currently held by another entity, then the method returns a correlationId that the
     * caller has to use to wait for. As soon as this lock is assigned to the caller, the lock manager creates a
     * {@link Response} for this specified correlationId, containing the {@link PersistentLockResult}.
     * <p>
     * Example:
     * 
     * <pre>
     * private void acquireLock(final String lockId) throws Interrupt {
     *     for (;;) {
     *         logger.info(&quot;Going to acquire lock '{}'&quot;, lockId);
     *         final String cid = persistentLockManager.acquireLock(lockId, this.getId());
     *         if (cid == null) {
     *             logger.info(&quot;Successfully acquired lock '{}'&quot;, lockId);
     *             return;
     *         }
     *         else {
     *             logger.info(&quot;Lock '{}' is currently not free - calling wait...&quot;, lockId);
     *             wait(WaitMode.ALL, 10000, cid);
     *             final Response&lt;PersistentLockResult&gt; result = getAndRemoveResponse(cid);
     *             logger.info(&quot;lock result={}&quot;, result);
     *             if (result.isTimeout()) {
     *                 logger.info(&quot;Failed to acquire lock: Timeout - trying again...&quot;);
     *             }
     *             else if (result.getResponse() != PersistentLockResult.OK) {
     *                 logger.error(&quot;Failed to acquire lock: {} - trying again...&quot;, result.getResponse());
     *             }
     *             else {
     *                 logger.info(&quot;Successfully acquired lock '{}'&quot;, lockId);
     *                 return;
     *             }
     *         }
     *     }
     * }
     * </pre>
     * 
     * @param lockId
     *        symbolic lock id
     * @param workflowInstanceId
     *        requestor/owner of this lock
     * @throws RuntimeException
     *         in case of technical problems
     * @return <code>null</code> if the lock was free and was assigned to the caller (workflowInstanceId) otherwise a
     *         correlationId to wait for.
     */
    String acquireLock(String lockId, String workflowInstanceId);

    /**
     * Releases the specified lock. If the workflow with the specified workflowId is not yet the owner of the lock (i.e.
     * it is still waiting to retrieve the lock), the acquireLock request is removed from the queue.
     * 
     * @param lockId
     *        symbolic lock id
     * @param workflowInstanceId
     *        requestor/owner of this lock
     * @throws RuntimeException
     *         in case of technical problems
     */
    void releaseLock(String lockId, String workflowInstanceId);

}
