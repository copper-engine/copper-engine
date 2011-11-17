/*
 * Copyright 2002-2011 SCOOP Software GmbH
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

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.ProcessingState;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.Processor;
import de.scoopgmbh.copper.internal.ProcessingStateAccessor;

/**
 * Internally used class.
 * 
 * @author austermann
 *
 */
class TransientProcessor extends Processor {

	private TransientScottyEngine engine;
	
	public TransientProcessor(String name, Queue<Workflow<?>> queue, int prio, ProcessingEngine engine) {
		super(name, queue, prio, engine);
		this.engine = (TransientScottyEngine) engine;
	}

	@Override
	protected void process(Workflow<?> wf) {
		synchronized (wf) {
			try {
				ProcessingStateAccessor.setProcessingState(wf, ProcessingState.RUNNING);
				wf.__beforeProcess();
				wf.main();
				//logger.debug("stack="+wf.get__stack());
				if (wf.get__stack().isEmpty()) {
					engine.removeWorkflow(wf.getId());
				}
			}
			catch(InterruptException e) {
				//logger.debug("stack="+wf.get__stack());
				assert wf.get__stack().size() > 0;
			}
			catch(Exception e) {
				engine.removeWorkflow(wf.getId());
				logger.error("Execution of wf "+wf.getId()+" failed",e);
			}
		}					
	}
}
