package de.scoopgmbh.copper.persistent;

import java.util.Queue;

import org.springframework.transaction.PlatformTransactionManager;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.Processor;

public class SpringTxnPersistentPriorityProcessorPool extends PersistentPriorityProcessorPool {
	
	private PlatformTransactionManager transactionManager;

	public SpringTxnPersistentPriorityProcessorPool(String id, int numberOfThreads, PlatformTransactionManager transactionManager) {
		super(id, numberOfThreads);
		this.transactionManager = transactionManager;
	}
	
	@Override
	protected Processor newProcessor(String name, Queue<Workflow<?>> queue, int threadPrioriry, ProcessingEngine engine) { 
		return new SpringTxnPersistentProcessor(name, queue, threadPrioriry, engine, transactionManager);
	}

}
