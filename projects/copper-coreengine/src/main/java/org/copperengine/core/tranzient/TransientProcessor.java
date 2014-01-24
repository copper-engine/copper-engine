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

import java.util.Queue;

import org.copperengine.core.Interrupt;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.ProcessingState;
import org.copperengine.core.Workflow;
import org.copperengine.core.common.Processor;
import org.copperengine.core.WorkflowAccessor;

/**
 * Internally used class.
 *
 * @author austermann
 */
class TransientProcessor extends Processor {

    private TransientScottyEngine engine;
    private final WorkflowAccessor accessor = new WorkflowAccessor();

    public TransientProcessor(String name, Queue<Workflow<?>> queue, int prio, ProcessingEngine engine) {
        super(name, queue, prio, engine);
        this.engine = (TransientScottyEngine) engine;
    }

    @Override
    protected void process(Workflow<?> wf) {
        logger.trace("before - stack.size()={}", wf.get__stack().size());
        logger.trace("before - stack={}", wf.get__stack());
        synchronized (wf) {
            try {
                accessor.setProcessingState(wf, ProcessingState.RUNNING);
                wf.__beforeProcess();
                wf.main();
                logger.trace("after 'main' - stack={}", wf.get__stack());
                engine.removeWorkflow(wf.getId());
                assert wf.get__stack().isEmpty() : "Stack must be empty \n" + wf.get__stack();
            } catch (Interrupt e) {
                logger.trace("interrupt - stack={}", wf.get__stack());
                assert wf.get__stack().size() > 0;
            } catch (Exception e) {
                engine.removeWorkflow(wf.getId());
                logger.error("Execution of wf " + wf.getId() + " failed", e);
                assert wf.get__stack().isEmpty() : "Stack must be empty \n" + wf.get__stack();
            }
        }
    }
}
