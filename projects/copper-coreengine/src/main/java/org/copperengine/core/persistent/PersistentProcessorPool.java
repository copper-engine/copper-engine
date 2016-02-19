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
package org.copperengine.core.persistent;

import org.copperengine.core.common.ProcessorPool;

/**
 * Subclass of {@link ProcessorPool} adding some persistence related methods.
 * 
 * @author austermann
 */
public interface PersistentProcessorPool extends ProcessorPool {

    public static final String DEFAULT_POOL_ID = "P#DEFAULT";

    public void doNotify();

    /**
     * Suspends dequeuing of workflow instances from the storage.
     * Workflow instances that already reside in the transient queue are still processed, i.e.
     * calling this methods runs this processor pool "dry".
     * 
     * @see PersistentProcessorPool#resumeDequeue()
     */
    public void suspendDequeue();

    /**
     * Resumes dequeuing of workflow instances from the storage.
     * 
     * @see PersistentProcessorPool#suspendDequeue()
     */
    public void resumeDequeue();

}
