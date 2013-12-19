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
package org.copperengine.core.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.Response;
import org.copperengine.core.db.utility.RetryingTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DBMockAdapter {

	private static final Logger logger = LoggerFactory.getLogger(DBMockAdapter.class);

	private ScheduledExecutorService pool;
	private int delay=500;
	private PersistentProcessingEngine engine;
	private AtomicInteger invokationCounter = new AtomicInteger(0);
	private DataSource dataSource;

	public void setEngine(PersistentProcessingEngine engine) {
		this.engine = engine;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDelayMSec(int delay) {
		this.delay = delay;
	}

	// do some work; delayed response to engine object
	public void foo(final String param, final String cid) {
		invokationCounter.incrementAndGet();
		if (delay <= 0) {
			doNotify(param, cid);
		}
		else {
			pool.schedule(new Runnable() {
				@Override
				public void run() {
					doNotify(param, cid);
				}
			}, delay, TimeUnit.MILLISECONDS);
		}
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

	void doNotify(final String param, final String cid) {
		try {
			new RetryingTransaction<Void>(dataSource) {
				@Override
				protected Void execute() throws Exception {
					engine.notify(new Response<String>(cid, param, null), this.getConnection());
					return null;
				}
			}.run();
		} 
		catch (Exception e) {
			logger.error("notify failed",e);
		}
	}
}
