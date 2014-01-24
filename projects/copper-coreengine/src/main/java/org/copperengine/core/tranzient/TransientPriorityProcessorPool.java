/*
 * Copyright 2002-2014 SCOOP Software GmbH
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

import org.copperengine.core.ProcessingState;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowAccessor;
import org.copperengine.core.common.PriorityProcessorPool;


/**
 * Default implementation of a {@link TransientProcessorPool}, backed by a priority queue and a configurable
 * amount of processor threads.
 *
 * @author austermann
 */
public class TransientPriorityProcessorPool extends PriorityProcessorPool implements TransientProcessorPool {
    private WorkflowAccessor accessor = new WorkflowAccessor();

    /**
     * Creates a new {@link TransientPriorityProcessorPool} with as many worker threads as processors available on the
     * corresponding environment. <code>id</code> needs to be initialized later using the setter.
     */
    public TransientPriorityProcessorPool() {
        setProcessorFactory(new TransientProcessorFactory());
    }

    /**
     * Creates a new {@link TransientPriorityProcessorPool} with as many worker threads as processors available on the
     * corresponding environment.
     */
    public TransientPriorityProcessorPool(String id) {
        super(id);
        setProcessorFactory(new TransientProcessorFactory());
    }

    public TransientPriorityProcessorPool(String id, int numberOfThreads) {
        super(id, numberOfThreads);
        setProcessorFactory(new TransientProcessorFactory());
    }

    @Override
    public void enqueue(Workflow<?> wf) {
        if (wf == null)
            throw new NullPointerException();
        accessor.setProcessingState(wf, ProcessingState.ENQUEUED);
        synchronized (queue) {
            queue.add(wf);
            if (!queue.isSuspended()) {
                queue.notify();
            }
        }
    }

}
