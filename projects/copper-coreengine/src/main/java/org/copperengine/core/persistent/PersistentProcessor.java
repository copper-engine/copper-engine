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

import java.util.Date;
import java.util.Queue;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.Interrupt;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.ProcessingState;
import org.copperengine.core.Workflow;
import org.copperengine.core.common.Processor;
import org.copperengine.core.internal.WorkflowAccessor;
import org.copperengine.core.persistent.txn.Transaction;
import org.copperengine.core.persistent.txn.TransactionController;

public class PersistentProcessor extends Processor {

    private final PersistentScottyEngine engine;
    private final TransactionController transactionController;

    public PersistentProcessor(String name, Queue<Workflow<?>> queue, int prio, ProcessingEngine engine, TransactionController transactionController) {
        super(name, queue, prio, engine);
        if (engine == null)
            throw new NullPointerException();
        if (transactionController == null)
            throw new NullPointerException();
        this.engine = (PersistentScottyEngine) engine;
        this.transactionController = transactionController;
    }

    @Override
    protected void process(final Workflow<?> wf) {
        final PersistentWorkflow<?> pw = (PersistentWorkflow<?>) wf;
        try {
            transactionController.run(new Transaction<Void>() {
                @Override
                public Void run() throws Exception {
                    synchronized (pw) {
                        try {
                            WorkflowAccessor.setProcessingState(pw, ProcessingState.RUNNING);
                            WorkflowAccessor.setLastActivityTS(wf, new Date());
                            engine.injectDependencies(pw);
                            pw.__beforeProcess();
                            pw.main();
                            WorkflowAccessor.setProcessingState(pw, ProcessingState.FINISHED);
                            engine.getDbStorage().finish(pw, new Acknowledge.BestEffortAcknowledge());
                            assert pw.get__stack().isEmpty() : "Stack must be empty";
                        } catch (Interrupt e) {
                            assert pw.get__stack().size() > 0;
                        } finally {
                            WorkflowAccessor.setLastActivityTS(wf, new Date());
                            engine.unregister(pw);
                        }
                        if (pw.getRegisterCall() != null) {
                            engine.getDbStorage().registerCallback(pw.getRegisterCall(), new Acknowledge.BestEffortAcknowledge());
                        }
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("execution of workflow instance failed", e);
            handleError(pw, e);
        }
    }

    protected void handleError(PersistentWorkflow<?> wf, Exception exception) {
        logger.error("Storing error information for workflow instance...");
        try {
            engine.getDbStorage().error(wf, exception, new Acknowledge.BestEffortAcknowledge());
        } catch (Exception e) {
            logger.error("FATAL ERROR: Unable to store error information", e);
        }
    }

}
