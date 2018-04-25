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
package org.copperengine.regtest.test.persistent.lock;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.persistent.lock.PersistentLockManager;
import org.copperengine.core.persistent.lock.PersistentLockResult;
import org.copperengine.core.util.Backchannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockingWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(LockingWorkflow.class);

    private transient PersistentLockManager persistentLockManager;
    private transient Backchannel backchannel;

    @AutoWire
    public void setPersistentLockManager(PersistentLockManager persistentLockManager) {
        this.persistentLockManager = persistentLockManager;
    }

    @AutoWire
    public void setBackchannel(Backchannel backchannel) {
        this.backchannel = backchannel;
    }

    @Override
    public void main() throws Interrupt {
        logger.info("Starting...");
        Boolean success = false;
        for (int i = 0; i < 3; i++) {
            final String lockId = getData();
            try {
                acquireLock(lockId);

                logger.info("sleep for 50 msec...");
                Thread.sleep(50);
                logger.info("Done sleeping for 50 msec.");

                success = true;

            } catch (Exception e) {
                logger.error("main failed", e);
            }
            releaseLock(lockId);
        }
        backchannel.notify(getId(), success);
        logger.info("finished!");
    }

    private void acquireLock(final String lockId) throws Interrupt {
        for (;;) {
            logger.info("Going to acquire lock '{}'", lockId);
            final String cid = persistentLockManager.acquireLock(lockId, this.getId());
            if (cid == null) {
                logger.info("Successfully acquired lock '{}'", lockId);
                return;
            }
            else {
                logger.info("Lock '{}' is currently not free - calling wait...", lockId);
                wait(WaitMode.ALL, 10000, cid);
                final Response<PersistentLockResult> result = getAndRemoveResponse(cid);
                logger.info("lock result={}", result);
                if (result.isTimeout()) {
                    logger.info("Failed to acquire lock: Timeout - trying again...");
                }
                else if (result.getResponse() != PersistentLockResult.OK) {
                    logger.error("Failed to acquire lock: {} - trying again...", result.getResponse());
                }
                else {
                    logger.info("Successfully acquired lock '{}'", lockId);
                    return;
                }
            }
        }
    }

    private void releaseLock(final String lockId) {
        logger.info("releaseLock({})", lockId);
        persistentLockManager.releaseLock(lockId, this.getId());
    }
}
