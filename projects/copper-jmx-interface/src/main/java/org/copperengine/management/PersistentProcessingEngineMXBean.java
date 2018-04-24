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

import org.copperengine.management.model.WorkflowInstanceFilter;

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
     * @param filter the WorkflowInstanceFilter
     * @throws Exception When some database operations fail, i.e. connection lost, transaction problems, ..
     */
    public void restartFiltered(WorkflowInstanceFilter filter) throws Exception;

    public void restartAll() throws Exception;


    /**
     * Entirely deletes the provided worklow instance if it is in the error state. If it currently holds a lock, the lock is
     * released.
     * @param workflowInstanceId workflow instance id as string
     * @throws Exception when the delete operation fails
     */
    public void deleteBroken(String workflowInstanceId) throws Exception;

    public void deleteFiltered(WorkflowInstanceFilter filter) throws Exception;

    public DBStorageMXBean getDBStorage();

    /**
     *  Gets EngineClusterId to make it possible grouping engines into engine cluster.
     *  Engines in one engine cluster should use same Database, in other case grouping will show incorrect data in copper monitoring
     * @return the engineClusterId
     */
    public String getEngineClusterId();

}
