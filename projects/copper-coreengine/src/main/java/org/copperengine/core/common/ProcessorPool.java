/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package org.copperengine.core.common;

import org.copperengine.core.ProcessingEngine;

/**
 * A processor pool is a set of {@link Processor} threads executing workflow instances.
 *
 * @author austermann
 */
public interface ProcessorPool {

    /**
     * Called internally by COPPER during initialization
     */
    public void setEngine(ProcessingEngine engine);

    /**
     * returns the processor pools identifier
     */
    public String getId();

    /**
     * Starts up the processor pool
     */
    public void startup();

    /**
     * Shuts down the processor pool
     */
    public void shutdown();

    /**
     * Suspend processing of workflows.
     */
    public void suspend();

    /**
     * Resume processing of workflows
     */
    public void resume();
}
