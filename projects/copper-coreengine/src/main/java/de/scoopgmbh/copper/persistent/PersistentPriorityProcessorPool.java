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
package de.scoopgmbh.copper.persistent;

import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.ProcessingState;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.PriorityProcessorPool;
import de.scoopgmbh.copper.common.Processor;
import de.scoopgmbh.copper.common.WfPriorityQueue;
import de.scoopgmbh.copper.internal.ProcessingStateAccessor;
import de.scoopgmbh.copper.management.PersistentPriorityProcessorPoolMXBean;

/**
 * Implementation of the {@link PriorityProcessorPool} interface for use in the {@link PersistentScottyEngine}.
 * 
 * @author austermann
 *
 */
public class PersistentPriorityProcessorPool extends PriorityProcessorPool implements PersistentProcessorPool, PersistentPriorityProcessorPoolMXBean {

	private static final Logger logger = Logger.getLogger(PersistentPriorityProcessorPool.class);
	
	private Thread thread;
	private boolean shutdown = false;
	private final Object mutex = new Object();
	private int lowerThreshold = 3000;
	private int upperThreshold = 6000;
	private int upperThresholdReachedWaitMSec = 50;
	private int emptyQueueWaitMSec = 500;
	private int dequeueBulkSize = 2000;
	
	public PersistentPriorityProcessorPool() {
		super();
	}

	public PersistentPriorityProcessorPool(String id, int numberOfThreads) {
		super(id, numberOfThreads);
	}

	@Override
	protected Processor newProcessor(String name, Queue<Workflow<?>> queue, int threadPrioriry, ProcessingEngine engine) {
		return new PersistentProcessor(name, queue, threadPrioriry, engine);
	}
	
	@Override
	protected Queue<Workflow<?>> createQueue() {
		return new WfPriorityQueue() {
			private static final long serialVersionUID = 1L;
			private boolean notifiedLowerThreshold = false;
			@Override
			public Workflow<?> poll() {
				Workflow<?> wf = super.poll();
				if (!notifiedLowerThreshold && size() < lowerThreshold) {
					doNotify();
					notifiedLowerThreshold = true;
				}
				if (notifiedLowerThreshold && size() > lowerThreshold) {
					notifiedLowerThreshold = false;
				}
				return wf;
			}
			
		};
	}
	
	@Override
	public synchronized void startup() {
		super.startup();
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				PersistentPriorityProcessorPool.this.run();
			}
		}, getId()+"#DBReader");
		thread.start();
	}
	
	@Override
	public synchronized void shutdown() {
		super.shutdown();
		shutdown = true;
		thread.interrupt();
	}
	
	private void run() {
		logger.info("started");
		final PersistentScottyEngine engine = (PersistentScottyEngine) getEngine();
		final ScottyDBStorageInterface dbStorage = engine.getDbStorage();
		while(!shutdown) {
			try {
				while (!shutdown) {
					int queueSize = 0;
					synchronized (queue) {
						queueSize = queue.size();
					}
					if (queueSize < upperThreshold) {
						break;
					}
					if (logger.isTraceEnabled()) logger.trace("Queue size "+queueSize+" >= upper threshold "+upperThreshold+". Waiting...");
					doWait(upperThresholdReachedWaitMSec);
				}
				logger.trace("Dequeueing elements from DB...");
				List<Workflow<?>> rv = dbStorage.dequeue(getId(), dequeueBulkSize);
				if (shutdown) break;
				if (rv.isEmpty()) {
					if (logger.isTraceEnabled()) logger.trace("Dequeue returned nothing. Waiting...");
					doWait(emptyQueueWaitMSec);
				}
				else {
					if (logger.isTraceEnabled()) logger.trace("Dequeue returned "+rv.size()+" elements.");
					for (Workflow<?> wf : rv) {
						ProcessingStateAccessor.setProcessingState(wf, ProcessingState.DEQUEUED);
						engine.register(wf);
					}
					synchronized (queue) {
						queue.addAll(rv);
						queue.notifyAll();
					}
				}
			} 
			catch(InterruptedException e) {
				logger.info("interrupted");
			}
			catch(Exception e) {
				logger.error("dequeue failed",e);
			}
		}
		logger.info("stopped");
	}

	@Override
	public void doNotify() {
		logger.trace("doNotify");
		synchronized (mutex) {
			mutex.notify();
		}
	}
	
	private void doWait(long t) throws InterruptedException {
		if (logger.isTraceEnabled()) logger.trace("doWait("+t+")");
		synchronized (mutex) {
			mutex.wait(t);
		}
	}
	
	public synchronized void setLowerThreshold(int lowerThreshold) {
		if (lowerThreshold < 0 || lowerThreshold > upperThreshold) throw new IllegalArgumentException();
		this.lowerThreshold = lowerThreshold;
	}
	
	public int getLowerThreshold() {
		return lowerThreshold;
	}
	
	public synchronized void setUpperThreshold(int upperThreshold) {
		if (upperThreshold < 1 || upperThreshold < lowerThreshold) throw new IllegalArgumentException();
		this.upperThreshold = upperThreshold;
	}
	
	public int getUpperThreshold() {
		return upperThreshold;
	}

	public int getUpperThresholdReachedWaitMSec() {
		return upperThresholdReachedWaitMSec;
	}

	public void setUpperThresholdReachedWaitMSec(int upperThresholdReachedWaitMSec) {
		if (upperThresholdReachedWaitMSec <= 0) throw new IllegalArgumentException();
		this.upperThresholdReachedWaitMSec = upperThresholdReachedWaitMSec;
	}

	public int getEmptyQueueWaitMSec() {
		return emptyQueueWaitMSec;
	}

	public void setEmptyQueueWaitMSec(int emptyQueueWaitMSec) {
		if (emptyQueueWaitMSec <= 0) throw new IllegalArgumentException();
		this.emptyQueueWaitMSec = emptyQueueWaitMSec;
	}

	public int getDequeueBulkSize() {
		return dequeueBulkSize;
	}

	public void setDequeueBulkSize(int dequeueBulkSize) {
		if (dequeueBulkSize <= 0) throw new IllegalArgumentException();
		this.dequeueBulkSize = dequeueBulkSize;
	}
	


}
