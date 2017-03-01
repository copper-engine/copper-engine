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
package org.copperengine.core;

public enum ProcessingState {
    RAW,        // Workflow was just initialized, nothing happened with it so far
    ENQUEUED,   // Workflow is in queue and waits for execution (Used by transient engines) / waits for engine to take ownership and grep it from database (persistent)
    DEQUEUED,   // Workflow is pulled from database (dequeued) and put to the Processing pool queue. Dequeue is marked on the processing state within the database.
    RUNNING,    // Workflow is currently running (This state is set in RAM only. A persistent engine will not update the database whether a workflow is running (it keeps on dequeud)
    WAITING,    // Workflow is in wait state. The awake-conditions from wait are not yet (fully) fulfilled.
    FINISHED,   // Workflow finished execution normally.
    ERROR,      // Workflow stopped execution due to an exception. Might be resubmitted later on.
    INVALID     // Something illegal happened to the workflow. Cannot work with it anymore. In persistent mode, this might be caused by a deserialization error or something similar.
}
