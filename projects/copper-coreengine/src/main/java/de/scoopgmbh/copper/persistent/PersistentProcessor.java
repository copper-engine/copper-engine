/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.persistent;

import java.util.Queue;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.ProcessingState;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.Processor;
import de.scoopgmbh.copper.internal.WorkflowAccessor;
import de.scoopgmbh.copper.persistent.txn.Transaction;
import de.scoopgmbh.copper.persistent.txn.TransactionController;

class PersistentProcessor extends Processor {

	private final PersistentScottyEngine engine;
	private final TransactionController transactionController;

	public PersistentProcessor(String name, Queue<Workflow<?>> queue, int prio, ProcessingEngine engine, TransactionController transactionController) {
		super(name, queue, prio, engine);
		if (engine == null) throw new NullPointerException();
		if (transactionController == null) throw new NullPointerException();
		this.engine = (PersistentScottyEngine)engine;
		this.transactionController = transactionController;
	}

	@Override
	protected void process(final Workflow<?> wf) {
		final PersistentWorkflow<?> pw = (PersistentWorkflow<?>)wf;
		try {
			transactionController.run(new Transaction<Void>() {
				@Override
				public Void run() throws Exception {
					synchronized (pw) {
						try {
							WorkflowAccessor.setProcessingState(pw, ProcessingState.RUNNING);
							engine.getDependencyInjector().inject(pw);
							pw.__beforeProcess();
							pw.main();
							WorkflowAccessor.setProcessingState(pw, ProcessingState.FINISHED);
							engine.getDbStorage().finish(pw);
							assert pw.get__stack().isEmpty() : "Stack must be empty";
						}
						catch(InterruptException e) {
							assert pw.get__stack().size() > 0;
						}
						finally {
							engine.unregister(pw);
						}
						if (pw.registerCall != null) {
							engine.getDbStorage().registerCallback(pw.registerCall);
						}
					}
					return null;
				}
			});
		}
		catch(Exception e) {
			logger.error("execution of workflow instance failed",e);
			handleError(pw, e);
		}
	}

	protected void handleError(PersistentWorkflow<?> wf, Exception exception) {
		logger.error("Storing error information for workflow instance...");
		try {
			engine.getDbStorage().error(wf, exception);
		}
		catch(Exception e) {
			logger.error("FATAL ERROR: Unable to store error information",e);
		}
	}

}
