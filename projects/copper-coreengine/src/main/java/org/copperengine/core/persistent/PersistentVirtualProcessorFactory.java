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

import java.util.Queue;

import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.Workflow;
import org.copperengine.core.common.Processor;
import org.copperengine.core.common.ProcessorFactory;
import org.copperengine.core.persistent.txn.TransactionController;

public class PersistentVirtualProcessorFactory implements ProcessorFactory {

    private final TransactionController transactionController;

    public PersistentVirtualProcessorFactory(TransactionController transactionController) {
        this.transactionController = transactionController;
    }

    public Processor newProcessor(String id, Queue<Workflow<?>> queue, int threadPrioriry, ProcessingEngine engine) {
        return new PersistentProcessor(id, queue, threadPrioriry, engine, transactionController, true);
    }
}
