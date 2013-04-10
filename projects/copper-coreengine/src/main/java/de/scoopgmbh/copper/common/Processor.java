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
package de.scoopgmbh.copper.common;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.instrument.Transformed;
import de.scoopgmbh.copper.monitoring.MonitoringDataCollector;
import de.scoopgmbh.copper.monitoring.NoMonitoringDataCollector;
import de.scoopgmbh.copper.util.MDCConstants;

/**
 * A COPPER Processor is a thread executing {@link Workflow} instances.
 * 
 * @author austermann
 *
 */
public abstract class Processor extends Thread {
	
	protected static final Logger logger = LoggerFactory.getLogger(Processor.class);
	protected final Queue<Workflow<?>> queue;
	protected volatile boolean shutdown = false;
	protected final ProcessingEngine engine;
	
	protected MonitoringDataCollector monitoringDataCollector = new NoMonitoringDataCollector();
	public void setMonitoringDataCollector(MonitoringDataCollector monitoringDataCollector) {
		this.monitoringDataCollector = monitoringDataCollector;
	}
	
	public Processor(String name, Queue<Workflow<?>> queue, int prio, final ProcessingEngine engine) {
		super(name);
		this.queue = queue;
		this.setPriority(prio);
		this.engine = engine;
	}
	
	public synchronized void shutdown() {
		if (shutdown)
			return;
		logger.info("Stopping processor '"+getName()+"'...");
		shutdown = true;
		interrupt();
	}
	
	@Override
	public void run() {
		logger.info("started");
		while (!shutdown) {
			try {
				Workflow<?> wf = null;
				synchronized (queue) {
					wf = queue.poll();
					if (wf == null) {
						//logger.info("queue is empty - waiting");
						queue.wait();
						wf = queue.poll();
					}
				}
				if (!shutdown && wf != null) {
					if (wf.getClass().getAnnotation(Transformed.class) == null) {
						throw new RuntimeException(wf.getClass().getName()+" has not been transformed");
					}
					MDC.put(MDCConstants.REQUEST, wf.getId());
					try {
						process(wf);
					}
					finally {
						MDC.remove(MDCConstants.REQUEST);
					}
				}
			}
			catch(InterruptedException e) {
				// ignore
			}
			catch(Throwable t) {
				logger.error("",t);
				t.printStackTrace();
			}
		}
		logger.info("stopped");
	}
	
	protected abstract void process(Workflow<?> wf);
}
