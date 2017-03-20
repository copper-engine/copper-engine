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
package org.copperengine.core.test.tranzient;

import org.copperengine.core.*;
import org.copperengine.core.lockmgr.LockResult;
import org.copperengine.core.lockmgr.tranzient.TransientLockManager;
import org.copperengine.core.test.tranzient.lockManager.TransientLockEvaluationData;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class TransientLockManagerTestWorkflow extends Workflow<Integer> {

    private transient TransientLockManager lockManager;
    private transient ConcurrentHashMap<Integer, TransientLockEvaluationData> evalData;

    @AutoWire
    public void setLockManager(TransientLockManager lockManager) {
        this.lockManager = lockManager;
    }

    @AutoWire
    public void setEvalData(ConcurrentHashMap<Integer, TransientLockEvaluationData> evalData) {
        this.evalData = evalData;
    }


     private void acquireLock(final String lockId) throws Interrupt {
        String cidOld = null;
        for (;;) {
            String cid = lockManager.acquireLock(lockId, this.getId());

            // Test if we tried to acquire not on the first time any more that wait correlation IDs remain the same
            if((cidOld != null) && (cid != null)) {
                assertTrue(cidOld.equals(cid));
            }

            if (cid == null) {
                // Successfully acquired lock, nobody used it before we called acquireLock.
                return;
            }
            else {
                evalData.get(getData()).countIsAlreadyAcquired();
                wait(WaitMode.ALL, 97, TimeUnit.MILLISECONDS, cid);
                final Response<LockResult> result = getAndRemoveResponse(cid);

                if (result.isTimeout()) {
                    //So the lock is still blocked. Let's retry then..
                } else {
                    assertTrue(result.getResponse() == LockResult.OK);
                    // No timeout of response, thus the lock is successfully acquired.
                    return;
                }
            }
            cidOld = cid;
        }
    }


    public void main() throws Interrupt {
        // We can safely put and read afterwards our evalData here, as this entry will only be added and never deleted in the test
        evalData.putIfAbsent(getData(), new TransientLockEvaluationData());

        // Try to acquire a lock
        final String myLockId = "TransientLockTest-" + getData().intValue();
        acquireLock(myLockId);
        evalData.get(getData()).acquiredLock();

        // Just use the lock for some time.
        try {
            Thread.sleep(131);
        } catch (InterruptedException e) {
            throw new AssertionError("Thread.sleep interrupt occured. Test result unpredicted now. Please try again");
        }

        // Let's release the lock again
        evalData.get(getData()).willReleaseLockNow();
        lockManager.releaseLock(myLockId, this.getId());

    }

}