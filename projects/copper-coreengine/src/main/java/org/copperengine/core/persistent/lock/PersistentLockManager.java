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
    void acquireLock(String lockId, String correlationId, String workflowInstanceId);

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
    void releaseLock(String lockId, String workflowInstanceId);

}
