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

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.ProcessingState;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.Processor;
import de.scoopgmbh.copper.internal.WorkflowAccessor;

class SpringTxnPersistentProcessor extends Processor {

	private final PersistentScottyEngine engine;
	private final PlatformTransactionManager transactionManager;

	public SpringTxnPersistentProcessor(String name, Queue<Workflow<?>> queue, int prio, ProcessingEngine engine, PlatformTransactionManager transactionManager) {
		super(name, queue, prio, engine);
		this.engine = (PersistentScottyEngine)engine;
		this.transactionManager = transactionManager;
	}

	@Override
	protected void process(Workflow<?> wf) {
		Exception exception = null;
		DefaultTransactionDefinition defWfExec = new DefaultTransactionDefinition();
		defWfExec.setName("WorkflowInstanceExecution#"+wf.getId());
		final TransactionStatus txnStatus = transactionManager.getTransaction(defWfExec);
		try {
			boolean error = false;
			PersistentWorkflow<?> pw = (PersistentWorkflow<?>)wf;
			synchronized (pw) {
				try {
					WorkflowAccessor.setProcessingState(wf, ProcessingState.RUNNING);
					engine.getDependencyInjector().inject(pw);
					wf.__beforeProcess();
					pw.main();
					WorkflowAccessor.setProcessingState(wf, ProcessingState.FINISHED);
					engine.getDbStorage().finish(pw);
					assert wf.get__stack().isEmpty() : "Stack must be empty";
				}
				catch(InterruptException e) {
					assert pw.get__stack().size() > 0;
				}
				catch(Exception e) {
					logger.error("Execution failed",e);
					exception = e;
					error = true;
				}
				finally {
					engine.unregister(pw);
				}

				if (pw.registerCall != null && !error) {
					engine.getDbStorage().registerCallback(pw.registerCall);
				}
			}
		}
		catch(Exception e) {
			logger.error("",e);
			exception = e;
		}

		if (exception != null) {
			transactionManager.rollback(txnStatus);
		}
		else {
			transactionManager.commit(txnStatus);
		}

		if (exception != null) {
			logger.error("Storing error information for workflow instance...");
			DefaultTransactionDefinition def = new DefaultTransactionDefinition(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
			def.setName("COPPER#SetToError#"+wf.getId());
			TransactionStatus setErrorTxn = transactionManager.getTransaction(new DefaultTransactionDefinition());
			try {
				engine.getDbStorage().error(wf, exception);
				transactionManager.commit(setErrorTxn);
			}
			catch(Exception e) {
				logger.error("Unable to store error information",e);
				transactionManager.rollback(setErrorTxn);
			}
		}
	}

}
