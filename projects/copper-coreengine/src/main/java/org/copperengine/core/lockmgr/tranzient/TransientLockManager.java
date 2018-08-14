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
package org.copperengine.core.lockmgr.tranzient;


import org.copperengine.core.Acknowledge;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.Response;
import org.copperengine.core.lockmgr.LockManager;
import org.copperengine.core.lockmgr.LockResult;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation for the {@link LockManager} interface for transient engines.
 * Note: This will not work on a cluster with transient engines! Synchronization on a lock id only works for one engine.
 * Note: You might use this for persistent engines as well, but keep in mind, the synchronization only works for workflows
 *       on one engine and won't survive system crashes! Might still be useful sometimes..
 *
 */
public class TransientLockManager implements LockManager {

    // For each lockId, we store a collection of LockPairs to know which workflow tried to acquire a lock and have
    // a correlation ID for notification..
    // The workflow instance ID is stored as well in order to allow a single workflow call acquireLock multiple times
    // without breaking the locking mechanism (It will always receive the same correlation ID to wait upon).
    // Behavior is consistent to PersistentLockManager.
    private class LockPair {
        public String workflowInstanceID;
        public String correlationID;

        public LockPair(String workflowInstanceID, String correlationID) {
            this.workflowInstanceID = workflowInstanceID;
            this.correlationID = correlationID;
        }
    }

    // For garbage collection capabilities, we add a boolean inUse here. With this small helper, it is possible
    // to remove a workflow from the global lock map if no other workflow is currently using it anymore.
    protected class Locker {
        boolean inUse=true; // Help variable to allow removal out of HashMap for garbage collection of unused lock-keys!
        LinkedList<LockPair> waiter;

        public Locker(final String workflowInstanceId) {
            waiter = new LinkedList<LockPair>();
            waiter.addFirst(new LockPair(workflowInstanceId, engine.createUUID()));
        }

        // Called from acquire lock
        synchronized String getWaitId(final String workflowInstanceId) {
            if (inUse) {
                // Locker currently in use, so add a new waiter here. (If workflow not already on waiting list)
                for (LockPair currentWaiter : waiter) {
                    if (currentWaiter.workflowInstanceID.equals(workflowInstanceId)) {
                        return currentWaiter.correlationID;
                    }
                }
                final String newWaitId = engine.createUUID();
                waiter.addLast(new LockPair(workflowInstanceId, newWaitId));
                return newWaitId;
            } else {
                // If not inUse, then the Lock must be empty (Only set to inUse=false when empty)
                assert(waiter.isEmpty());

                // As waiter list is empty here (and not inUse) then the Locker object is completely unused!
                // This can happen if one thread releases
                // the last lock and at the same time, another thread acquires this lock here.
                // If this happens, we just return null (to the calling acquire method) and let acquire handle the
                // situation. Acquire will then just retry acquiring to get a "valid" new Locker object which is inUse..
                return null;
            }
        }

        synchronized String getNextWaiter() {
            assert(!waiter.isEmpty()); // Checked in releaseLock method already.
            return waiter.getFirst().correlationID;
        }
    }




    protected ConcurrentHashMap<String, Locker> lockMap;
    protected ProcessingEngine engine;

    public TransientLockManager(ProcessingEngine engine) {
        this.engine = engine;
        lockMap = new ConcurrentHashMap<>();
    }

    @Override
    public String acquireLock(final String lockId, final String workflowInstanceId) {
        // It is important for this function to almost not block and work fast.
        // A requirement of the TransientLockManager is that it runs fast for a million different lock ids where only
        // a few occur multiple times. Thus, querying the lock should almost not block. But if two ore more workflows
        // use the same lockId, then performance doesn't matter that much anymore. They have to wait for each other
        // nonetheless. So it's critical to have a fast lockId-Lock-Lookup which is why we use a ConcurrentHashMap here.


        while (true) {
            Locker lockObj = lockMap.get(lockId);
            if (lockObj==null) {
                lockObj=lockMap.putIfAbsent(lockId, new Locker(workflowInstanceId));
                // putIfAbsent returns the previous value in the map, so null if nothing was in there before!
                if (lockObj==null) {
                    // Insertion worked as first one for this lockId, so return without giving a correlation id to wait
                    // for but just null to let workflow directly continue execution. Workflow is added to the waiter-list
                    // nonetheless until workflow releases the lock.
                    return null;
                } else {
                    // Ok, someone put a Locker in the map in between our get and putIfAbsent.
                    // Let's try in the next block to get a correlation ID to wait for on this new Locker-object
                }
            }

            String waitID = lockObj.getWaitId(workflowInstanceId);
            if (waitID != null) {
                // If waitID is null, then the current lock is set to "unused" and the while loop shall just restart
                // It will then either create a new lock or append waiting to an existing one. (Or come back here again..)
                return waitID;
            }
        }
    }

    public void releaseLock(final String key, final String workflowInstanceId) {
        // lockMap.get(key) always exists. Otherwise we would have called releaseLock more often than acquireLock!
        Locker locker = lockMap.get(key);
        assert(locker != null);
        synchronized (locker) {
            LockPair firstRemoval = locker.waiter.removeFirst();
            assert(firstRemoval.workflowInstanceID.equals(workflowInstanceId)); // Only the current running workflow may release the lock
            if (locker.waiter.isEmpty()) {
                // set Locker to deprecated and not be used anymore!
                // No one needs to be notificated upon anymore
                locker.inUse = false;
                lockMap.remove(key);
            } else {
                // notify engine that next one can be awaken
                final String nextCorrelation = locker.getNextWaiter();
                engine.notify(new Response<LockResult>(nextCorrelation, LockResult.OK, null), new Acknowledge.BestEffortAcknowledge());
            }
        }
    }

    public long curNumberOfLocks() {
        return lockMap.size();
        // TODO When Java 8 is set as minimum version for COPPER, change to lockMap.mappingCount().
    }
}
