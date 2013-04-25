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
package de.scoopgmbh.copper.batcher.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.BatchExecutorBase;
import de.scoopgmbh.copper.batcher.BatchRunner;
import de.scoopgmbh.copper.batcher.Batcher;
import de.scoopgmbh.copper.batcher.impl.BatcherQueue.State;
import de.scoopgmbh.copper.management.BatcherMXBean;
import de.scoopgmbh.copper.monitoring.NullRuntimeStatisticsCollector;
import de.scoopgmbh.copper.monitoring.RuntimeStatisticsCollector;

/**
 * COPPERs default implementation of the {@link Batcher} interface
 * 
 * @author austermann
 *
 */
public class BatcherImpl implements Batcher, BatcherMXBean {
	
	private Logger logger = LoggerFactory.getLogger(BatcherImpl.class);
	
	private class WorkerThread extends Thread {
		
		boolean started = false;
		boolean stop = false;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void run() {
			while (!stop) {
				if (!started) {
					synchronized (this) {
						started = true;
						notify();
					}
				}
				List<BatchCommand<?,?>> commands;
				try {
					commands = queue.poll();
					if (commands == null) {
						return;
					}
					final BatchExecutorBase e = commands.get(0).executor();
					final long startTS = System.currentTimeMillis();
					batchRunner.run(commands, e);
					statisticsCollector.submit(e.id(), commands.size(), System.currentTimeMillis()-startTS, TimeUnit.MILLISECONDS);
					
				} catch (InterruptedException e) {
					logger.warn("Interrupted",e);
				}
			}
		}
		
		public synchronized void waitForStartup() throws InterruptedException {
			while (!started)
				wait();
		}
	}
	
	BatcherQueue queue = new BatcherQueue();
	private RuntimeStatisticsCollector statisticsCollector = new NullRuntimeStatisticsCollector();
	private List<WorkerThread> threads = new ArrayList<WorkerThread>();
	private int numThreads;
	@SuppressWarnings("rawtypes")
	private BatchRunner batchRunner;
	
	public BatcherImpl(int numThreads) {
		this.numThreads = numThreads;
	}
	
	@SuppressWarnings("rawtypes")
	public void setBatchRunner(BatchRunner batchRunner) {
		this.batchRunner = batchRunner;
	}
	
	public synchronized int getNumThreads() {
		return numThreads;
	}
	
	public synchronized void setNumThreads(int numThreads) {
		if (numThreads <= 0 || numThreads > 200) throw new IllegalArgumentException();
		this.numThreads = numThreads;
		try {
			adjustNumberOfThreads();
		} 
		catch (InterruptedException e) {
			logger.error("setNumThreads failed",e);
		}
	}

	public void setStatisticsCollector(RuntimeStatisticsCollector statisticsCollector) {
		this.statisticsCollector = statisticsCollector;
	}
	
	private synchronized void adjustNumberOfThreads() throws InterruptedException {
		while (threads.size() < numThreads) {
			logger.info("Starting new batcher thread...");
			WorkerThread thread = new WorkerThread();
			thread.setName("Batcher.Worker#"+(threads.size()+1));
			thread.start();
			thread.waitForStartup();
			threads.add(thread);
			logger.info("Done, starting new batcher thread.");
		}
		while (threads.size() > numThreads) {
			logger.info("Stopping batcher thread...");
			WorkerThread thread = threads.remove(threads.size()-1);
			thread.stop = true;
			thread.interrupt();
			thread.join();
			logger.info("Done, stopping batcher thread.");
		}
	}
	
	private synchronized void start() throws InterruptedException {
		adjustNumberOfThreads();
	}
	
	private synchronized void stop() throws InterruptedException {
		queue.stop();
		for (Thread t : threads) {
			t.join();
		}
	}

	public <E extends BatchExecutor<E,T>, T extends BatchCommand<E,T>> void submitBatchCommand(BatchCommand<E,T> cmd) {
		if (queue.state != State.STARTED)
			throw new IllegalStateException("Batcher is shutting down");
		queue.submitBatchCommand(cmd);		
	}

	public void startup() {
		try {
			start();
		} 
		catch (InterruptedException e) {
			throw new Error("unexpected interruption", e);
		}
	}

	public void shutdown() {
		logger.info("shutting down...");
		try {
			stop();
		} 
		catch (InterruptedException e) {
			throw new Error("unexpected interruption", e);
		}
	}

	@Override
	public String getDescription() {
		return "Default COPPER Batcher";
	}

}
