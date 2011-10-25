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
package de.scoopgmbh.copper.batcher.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.BatchExecutorBase;
import de.scoopgmbh.copper.batcher.Batcher;
import de.scoopgmbh.copper.batcher.impl.BatcherQueue.State;
import de.scoopgmbh.copper.monitoring.NullRuntimeStatisticsCollector;
import de.scoopgmbh.copper.monitoring.RuntimeStatisticsCollector;

/**
 * Implementation of the Batcher interface
 * 
 * @author austermann
 *
 */
public class BatcherImpl implements Batcher {
	
	Logger logger = Logger.getLogger(BatcherImpl.class);
	
	class WorkerThread extends Thread {
		
		boolean started = false;
		
		public void run() {
			while (true) {
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
					final BatchExecutorBase<?> e = commands.get(0).executor();
					final long startTS = System.currentTimeMillis();
					e.execute(commands);
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
	WorkerThread[] threads;
	private RuntimeStatisticsCollector statisticsCollector = new NullRuntimeStatisticsCollector();
	
	public BatcherImpl(int numThreads) {
		threads = new WorkerThread [numThreads];
	}

	public void setStatisticsCollector(RuntimeStatisticsCollector statisticsCollector) {
		this.statisticsCollector = statisticsCollector;
	}
	
	private void start() throws InterruptedException {
		for (int i = 0; i < threads.length; ++i) {
			threads[i] = new WorkerThread();
			threads[i].start();
			threads[i].waitForStartup();
		}
	}
	
	private void stop() throws InterruptedException {
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
		} catch (InterruptedException e) {
			throw new Error("unexpected interruption", e);
		}
	}

	public void shutdown() {
		logger.info("shutting down...");
		try {
			stop();
		} catch (InterruptedException e) {
			throw new Error("unexpected interruption", e);
		}
	}

}
