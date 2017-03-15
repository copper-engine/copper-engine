/*
 * Copyright 2002-2017 SCOOP Software GmbH
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
package org.copperengine.core.tranzient;


import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.copperengine.core.LockManager;

/**
 * Implementation for the {@link org.copperengine.core.LockManager} interface for transient engines.
 * Note: This will not work on a cluster with transient engines! Synchronization on a lock id only works for one engine.
 *
 * @author theoDiefenthal
 *
 */
public class TransientLockManager implements LockManager, Comparable {

    protected static final String LOCK_PREFIX = "asdjfiwe_hkasdu3745isgfdjagu49";

    @Override
    public int compareTo(Object o) {
        // TODO Implement
        return 0;
    }


    private class LockPair {
        public String workflowInstanceID;
        public String correlationID;

        public LockPair(String workflowInstanceID, String correlationID) {
            this.workflowInstanceID = workflowInstanceID;
            this.correlationID = correlationID;
        }
    }

    protected ConcurrentHashMap<String, Set<LockPair>> lockMap;
    protected TransientScottyEngine engine;

    public TransientLockManager(TransientScottyEngine engine) {
        this.engine = engine;
        lockMap = new ConcurrentHashMap<>();
    }


    @Override
    public String acquireLock(String lockId, String workflowInstanceId) {

        //synchronized ((LOCK_PREFIX + lockId).intern()) {
        //    // 1. Insert lock if not
        //    insertLock(_lockId, _workflowInstanceId, _correlationId, _insertTS, con);
        //    return findNewLockOwnerAfterAquire(_lockId, _workflowInstanceId, con);
        //}

        // Thread safe call to the lock map (Concurrent Hash map).
        Set<LockPair> set = lockMap.putIfAbsent(lockId, new TreeSet<LockPair>());

        // On the non thread safe Set, we can perform some operations in the synchronized block. Not performance critical
        // here as this synchronized is for each lock only, not for all locks in common like it is the lock map!
        synchronized(set) {
            // Behavior as in PersistentLockManager. If set contains current workflow instance already, just return correlation ID of it.
            // If not, create a new correlation ID, add to the set. Don't forget ordering by time though! Set might not be appropriate data structure! Better Queue? Linked List? Or similar..
            // TODO Implement.
        }

        return null;
    }

    @Override
    public void releaseLock(String lockId, String workflowInstanceId) {
        // TODO implement.
    }
}
