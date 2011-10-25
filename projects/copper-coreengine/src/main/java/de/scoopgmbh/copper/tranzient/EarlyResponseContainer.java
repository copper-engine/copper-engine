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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.Response;

public class EarlyResponseContainer {

	private static final Logger logger = Logger.getLogger(EarlyResponseContainer.class);
	
	private static final class EarlyResponse {
		final long ts;
		final Response<?> response;
		public EarlyResponse(final Response<?> response, final int minHoldBackTime) {
			this.response = response;
			this.ts = System.currentTimeMillis() + minHoldBackTime;
		}
	}
	
	private int lowerBorder4stale = 25000;
	private int upperBorder4stale = 26000;
	private LinkedHashSet<String> staleCorrelationIds = new LinkedHashSet<String>(upperBorder4stale);
	
	private int lowerBorderResponseMapSize = 25000;
	private int upperBorderResponseMapSize = 26000;
	private LinkedHashMap<String,EarlyResponse> responseMap = new LinkedHashMap<String, EarlyResponse>(5000);
	
	private int minHoldBackTime = 30000;
	private Thread thread;
	private boolean shutdown = false;
	private int checkInterval = 250;
	
	public EarlyResponseContainer() {
	}
	
	public void put(final Response<?> response) {
		if (response == null) 
			throw new NullPointerException();
		
		synchronized (staleCorrelationIds) {
			if (staleCorrelationIds.contains(response.getCorrelationId())) {
				if (logger.isDebugEnabled()) logger.debug("Response "+response+" is stale");
				return;
			}
		}
		
		synchronized (responseMap) {
			responseMap.put(response.getCorrelationId(), new EarlyResponse(response,minHoldBackTime));
			
			if (responseMap.size() > upperBorderResponseMapSize) {
				Iterator<String> iterator = responseMap.keySet().iterator();
				while (responseMap.size() > lowerBorderResponseMapSize) {
					iterator.next();
					iterator.remove();
				}
			}
		}
		
	}
	
	public Response<?> get(final String correlationId) {
		if (correlationId == null)
			throw new NullPointerException();
		if (correlationId.length() == 0)
			throw new IllegalArgumentException();

		synchronized (responseMap) {
			EarlyResponse er = responseMap.remove(correlationId);
			if (er != null) {
				return er.response;
			}
			return null;
		}
	}
	
	public void putStaleCorrelationId(final String correlationId) {
		if (correlationId == null)
			throw new NullPointerException();
		if (correlationId.length() == 0)
			throw new IllegalArgumentException();
		
		synchronized (staleCorrelationIds) {
			staleCorrelationIds.add(correlationId);
			if (staleCorrelationIds.size() > upperBorder4stale) {
				Iterator<String> iterator = staleCorrelationIds.iterator();
				while (staleCorrelationIds.size() > lowerBorder4stale) {
					iterator.next();
					iterator.remove();
				}
			}
		}
	}
	
	public synchronized void startup() {
		if (thread != null) 
			throw new IllegalStateException();
		thread = new Thread("EarlyResponseManager") {
			@Override
			public void run() {
				logger.info("started");
				while(!shutdown) {
					try {
						synchronized (responseMap) {
							Iterator<EarlyResponse> iterator = responseMap.values().iterator();
							while (iterator.hasNext()) {
								EarlyResponse er = iterator.next();
								if (er.ts < System.currentTimeMillis()) {
									iterator.remove();
								}
								else {
									break;
								}
							}
						}
						sleep(checkInterval);
					} 
					catch (InterruptedException e) {
						// ignore
					}
				}
				logger.info("stopped");
			}
		};
		thread.setDaemon(true);
		thread.start();
	}
	
	public synchronized void shutdown() {
		shutdown = true;
		thread.interrupt();
		thread = null;
	}
	
	public void setUpperBorder4stale(final int upperBorder4stale) {
		if (upperBorder4stale <= lowerBorder4stale)
			throw new IllegalArgumentException();
		this.upperBorder4stale = upperBorder4stale;
	}
	
	public void setLowerBorder4stale(final int lowerBorder4stale) {
		if (lowerBorder4stale <= 0)
			throw new IllegalArgumentException();
		this.lowerBorder4stale = lowerBorder4stale;
	}
	
	public void setUpperBorderResponseMapSize(int upperBorderResponseMapSize) {
		if (upperBorderResponseMapSize <= lowerBorderResponseMapSize)
			throw new IllegalArgumentException();
		this.upperBorderResponseMapSize = upperBorderResponseMapSize;
	}
	
	public void setLowerBorderResponseMapSize(int lowerBorderResponseMapSize) {
		if (lowerBorderResponseMapSize <= 0)
			throw new IllegalArgumentException();
		this.lowerBorderResponseMapSize = lowerBorderResponseMapSize;
	}
	
	public int getLowerBorder4stale() {
		return lowerBorder4stale;
	}
	
	public int getUpperBorder4stale() {
		return upperBorder4stale;
	}
	
	public int getLowerBorderResponseMapSize() {
		return lowerBorderResponseMapSize;
	}
	
	public int getUpperBorderResponseMapSize() {
		return upperBorderResponseMapSize;
	}
	
	public void setMinHoldBackTime(int minHoldBackTime) {
		if (minHoldBackTime <= 0)
			throw new IllegalArgumentException();
		this.minHoldBackTime = minHoldBackTime;
	}
	
	public int getMinHoldBackTime() {
		return minHoldBackTime;
	}
	
	public void setCheckInterval(int checkInterval) {
		if (checkInterval <= 0) 
			throw new IllegalArgumentException();
		this.checkInterval = checkInterval;
	}
	
	public int getCheckInterval() {
		return checkInterval;
	}

	public void putStaleCorrelationId(List<String> correlationIds) {
		if (correlationIds == null)
			throw new NullPointerException();
		if (correlationIds.size() == 0)
			return;
		
		synchronized (staleCorrelationIds) {
			staleCorrelationIds.addAll(correlationIds);
			if (staleCorrelationIds.size() > upperBorder4stale) {
				Iterator<String> iterator = staleCorrelationIds.iterator();
				while (staleCorrelationIds.size() > lowerBorder4stale) {
					iterator.next();
					iterator.remove();
				}
			}
		}
	}
}
