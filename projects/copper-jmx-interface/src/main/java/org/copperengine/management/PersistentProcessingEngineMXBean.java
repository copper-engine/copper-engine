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
package org.copperengine.management;

public interface PersistentProcessingEngineMXBean extends ProcessingEngineMXBean {

    /**
     * Trigger restart a workflow instance that is in the error state.
     * If workflow id is unknown or the workflow is not in broken state, nothing happens.
     *
     * @param workflowInstanceId workflow instance id as string
     * @throws Exception When some database operations fail, i.e. connection lost, transaction problems, ..
     */
    public void restart(String workflowInstanceId) throws Exception;

    /**
     * Trigger restart all workflow instances that are in error state.
     *
     * @throws Exception When some database operations fail, i.e. connection lost, transaction problems, ..
     */
    public void restartAll() throws Exception;


    /**
     * Entirely deletes the provided worklow instance if it is in the error state. If it currently holds a lock, the lock is
     * released.
     * @param workflowInstanceId
     * @throws Exception
     */
    public void deleteBroken(String workflowInstanceId) throws Exception;

    public DBStorageMXBean getDBStorage();

}
