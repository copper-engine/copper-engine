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
package de.scoopgmbh.copper.tranzient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Response;

@Deprecated
public class SimpleTimeoutManager implements TimeoutManager {

	private static final Logger logger = Logger.getLogger(SimpleTimeoutManager.class);
	
	private ProcessingEngine engine;
	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r,"TimeoutManager");
		}
	});

	@Override
	public void registerTimeout(final long timeoutTS, final List<String> _correlationIds) {
		long delay = timeoutTS - System.currentTimeMillis();
		if (delay <= 0) delay = 1;
		final List<String> correlationIds = new ArrayList<String>(_correlationIds);
		service.schedule(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {
					for (String cid : correlationIds) {
						engine.notify(new Response(cid));
					}
				}
				catch(Exception e) {
					logger.error("",e);
				}
			}
		}, delay, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void setEngine(ProcessingEngine engine) {
		this.engine = engine;
	}

	@Override
	public void unregisterTimeout(long timeoutTS, String correlationIds) {

	}

	@Override
	public void unregisterTimeout(long timeoutTS, List<String> correlationIds) {

	}

	@Override
	public void registerTimeout(long timeoutTS, String correlationId) {
		ArrayList<String> x = new ArrayList<String>(1);
		x.add(correlationId);
		registerTimeout(timeoutTS, x);
	}

	@Override
	public void shutdown() {

	}

	@Override
	public void startup() {

	}

}
