/**
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
package org.copperengine.core.test.tranzient.lockManager;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class TransientLockEvaluationData {
    private AtomicInteger countAlreadyAcquired;
    private AtomicInteger countConcurrent;

    public TransientLockEvaluationData() {
        countAlreadyAcquired = new AtomicInteger(0);
        countConcurrent = new AtomicInteger(0);
    }

    // Here we count if a workflow wanted to acquire a lock but it already was acquired and workflow must wait
    public void countIsAlreadyAcquired() {
        countAlreadyAcquired.incrementAndGet();
    }

    public int getCountOfAlreadyAcquired() {
        return countAlreadyAcquired.get();
    }

    // Here we pseudo-assert that only one workflow at a time holds the lock. 
    public void acquiredLock() {
        assertEquals(1, countConcurrent.incrementAndGet());
    }

    public void willReleaseLockNow() {
        assertEquals(0, countConcurrent.decrementAndGet());
    }
}
