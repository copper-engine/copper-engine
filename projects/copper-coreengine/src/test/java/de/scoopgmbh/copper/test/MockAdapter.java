/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.Callback;
import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Response;

public class MockAdapter {

	private static final Logger logger = LoggerFactory.getLogger(MockAdapter.class);

	private ScheduledExecutorService pool;
	private int delay=100;
	private ProcessingEngine engine;
	private AtomicInteger invokationCounter = new AtomicInteger(0);

	public void setEngine(ProcessingEngine engine) {
		this.engine = engine;
	}

	public void setDelayMSec(int delay) {
		this.delay = delay;
	}

	// do some work; delayed response to callback object
	public void foo(final String param, final Callback<String> cb) {
		invokationCounter.incrementAndGet();
		if (delay <= 0) {
			cb.notify(param);
		}
		else {
			pool.schedule(new Runnable() {
				@Override
				public void run() {
					cb.notify(param);
				}
			}, delay, TimeUnit.MILLISECONDS);
		}
	}

	// do some work; delayed response to engine object
	public void foo(final String param, final String cid) {
		foo(param, cid, delay);
	}

	// do some work; delayed response to engine object
	public void foo(final String param, final String cid, int overrideDelay) {
		invokationCounter.incrementAndGet();
		if (overrideDelay <= 0) {
			engine.notify(new Response<String>(cid, param, null));
		}
		else {
			pool.schedule(new Runnable() {
				@Override
				public void run() {
					engine.notify(new Response<String>(cid, param, null));
				}
			}, overrideDelay, TimeUnit.MILLISECONDS);
		}
	}	
	// do some work; delayed resonse to engine object
	public void incrementAsync(final int c, final String cid) {
		invokationCounter.incrementAndGet();
		if (delay <= 0) {
			engine.notify(new Response<Integer>(cid, c+1, null));
		}
		else {
			pool.schedule(new Runnable() {
				@Override
				public void run() {
					engine.notify(new Response<Integer>(cid, c+1, null));
				}
			}, delay, TimeUnit.MILLISECONDS);
		}
	}

	// do some work; t once resonse to engine object	
	public void incrementSync(final int c, final String cid) {
		invokationCounter.incrementAndGet();
		engine.notify(new Response<Integer>(cid, c+1, null));
	}

	public synchronized void shutdown() {
		logger.info("Shutting down...");
		this.pool.shutdown();
	}

	public int getInvokationCounter() {
		return invokationCounter.get();
	}

	public synchronized void startup() {
		logger.info("Starting up...");
		pool = Executors.newScheduledThreadPool(2);
	}

	// generate and return the correlation id
	public String foo(String param) {
		final String cid = engine.createUUID();
		this.foo(param, cid);
		return cid;
	}
}
