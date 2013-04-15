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
package de.scoopgmbh.copper.tranzient;

import java.util.Queue;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.ProcessingState;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.PriorityProcessorPool;
import de.scoopgmbh.copper.common.Processor;
import de.scoopgmbh.copper.internal.WorkflowAccessor;

/**
 * Default implementation of a {@link TransientProcessorPool}, backed by a priority queue and a configurable
 * amount of processor threads.
 *  
 * @author austermann
 *
 */
public class TransientPriorityProcessorPool extends PriorityProcessorPool implements TransientProcessorPool {

	/**
	 * Creates a new {@link TransientPriorityProcessorPool} with as many worker threads as processors available on the corresponding environment.
	 * <code>id</code> needs to be initialized later using the setter.
	 */
	public TransientPriorityProcessorPool() {
	}
	
	/**
	 * Creates a new {@link TransientPriorityProcessorPool} with as many worker threads as processors available on the corresponding environment.
	 */
	public TransientPriorityProcessorPool(String id) {
		super(id);
	}
	
	public TransientPriorityProcessorPool(String id, int numberOfThreads) {
		super(id, numberOfThreads);
	}

	@Override
	public void enqueue(Workflow<?> wf) {
		if (wf == null)
			throw new NullPointerException();
		WorkflowAccessor.setProcessingState(wf, ProcessingState.ENQUEUED);
		monitoringDataCollector.submitWorkflowHistory(ProcessingState.ENQUEUED.toString(), wf);
		synchronized (queue) {
			queue.add(wf);
			if (!queue.isSuspended()) {
				queue.notify();
			}
		}
	}

	@Override
	protected Processor newProcessor(String name, Queue<Workflow<?>> queue, int threadPrioriry, ProcessingEngine engine) {
		return new TransientProcessor(name,queue,threadPrioriry,engine,monitoringDataCollector);
	}

}
