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
package de.scoopgmbh.copper.persistent;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.IProcessorFactory;
import de.scoopgmbh.copper.common.Processor;
import de.scoopgmbh.copper.persistent.txn.TransactionController;

import java.util.Queue;

public class PersistentProcessorFactory implements IProcessorFactory {


    private TransactionController transactionController;

    public PersistentProcessorFactory(TransactionController transactionController){
        this.transactionController = transactionController;
    }

    public PersistentProcessorFactory(){
    }


    public void setTransactionController(TransactionController transactionController) {
        this.transactionController = transactionController;
    }

    public Processor newProcessor(String id, Queue<Workflow<?>> queue, int threadPrioriry, ProcessingEngine engine){
        return new PersistentProcessor(id, queue, threadPrioriry, engine, transactionController);
    }
}
