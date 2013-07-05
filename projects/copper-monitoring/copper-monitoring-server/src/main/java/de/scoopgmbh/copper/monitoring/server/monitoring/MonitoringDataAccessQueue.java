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
package de.scoopgmbh.copper.monitoring.server.monitoring;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.monitoring.core.data.MonitoringDataAccesor;
import de.scoopgmbh.copper.monitoring.core.data.MonitoringDataAdder;
import de.scoopgmbh.copper.monitoring.core.data.MonitoringDataStorage;

/**
 *	provide thread save access to monitoring data by serialize the access with {@link java.util.concurrent.ArrayBlockingQueue}.
 *  Accessing monitoring data is not blocking so monitoring can't block copper core functionality.
 */
public class MonitoringDataAccessQueue {

	public static final String IGNORE_WARN_TEXT = "could not process monitoring data. total ignored:";
	final ArrayBlockingQueue<Runnable> queue;
	AtomicLong ignored = new AtomicLong();
	private static final Logger logger = LoggerFactory.getLogger(MonitoringDataAccessQueue.class);
	
	/**
	 *	Contains the data for monitoring.
	 *  Should only be accessed via the {@link MonitoringDataAccessQueue}
	 */
	private final MonitoringDataAccesor monitoringDataAccesor;
	private final  MonitoringDataAdder monitoringDataAdder;
	private final MonitoringDataStorage dataStorage;

	public MonitoringDataAccessQueue(MonitoringDataAccesor monitoringDataAccesor, MonitoringDataAdder monitoringDataAdder, MonitoringDataStorage dataStorage){
		this(1000, monitoringDataAccesor,monitoringDataAdder, dataStorage);
	}
	
	public MonitoringDataAccessQueue(int queueCapacity, MonitoringDataAccesor monitoringDataAccesor, MonitoringDataAdder monitoringDataAdder, MonitoringDataStorage dataStorage){
		this.monitoringDataAccesor = monitoringDataAccesor;
		this.monitoringDataAdder = monitoringDataAdder;
		this.dataStorage = dataStorage;
		queue = new ArrayBlockingQueue<Runnable>(queueCapacity);
		new Thread("monitoringEventQueue") {
			{
				setDaemon(true);
			}
			@Override
			public void run() {
				while (true) {
					work();
				}
			};
		}.start();
		
	}

	public boolean offer(MonitoringDataAwareRunnable runnable) {
		runnable.setMonitoringDataAccesor(monitoringDataAccesor);
		runnable.setMonitoringDataAdder(monitoringDataAdder);
		runnable.setMonitoringDataStorage(dataStorage);
		boolean result=queue.offer(runnable);
		if (!result) {
			logger.warn(IGNORE_WARN_TEXT+ignored.incrementAndGet());
		}
		return result;
	}
	
	/**@see java.util.concurrent.BlockingQueue#put
	 * @param runnable
	 */
	public void put(MonitoringDataAwareRunnable runnable) {
		runnable.setMonitoringDataAccesor(monitoringDataAccesor);
		runnable.setMonitoringDataAdder(monitoringDataAdder);
		runnable.setMonitoringDataStorage(dataStorage);
		try {
			queue.put(runnable);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public <T> T callAndWait(MonitoringDataAwareCallable<T> callable) {
		callable.setMonitoringDataAccesor(monitoringDataAccesor);
		callable.setMonitoringDataAdder(monitoringDataAdder);
		callable.setMonitoringDataStorage(dataStorage);
		try {
			FutureTask<T> futureTask = new FutureTask<T>(callable);
			queue.put(futureTask);
			try {
				return futureTask.get();
			} catch (ExecutionException e) {
				if (e.getCause() instanceof RuntimeException)
					throw (RuntimeException)e.getCause();
				throw new RuntimeException(e.getCause());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}
	
	ArrayList<Runnable> elements = new ArrayList<Runnable>(100);
	public void work() {
		try {
			elements.clear();
			elements.add(queue.take());
			queue.drainTo(elements,100);
			for (Runnable runnable : elements) {
				runnable.run();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	
}
