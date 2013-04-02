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
package de.scoopgmbh.copper.monitoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects runtime statistics and logs average processing times to the logging system in a 
 * configurable time interval. 
 * 
 * @author austermann
 *
 */
public class LoggingStatisticCollector implements RuntimeStatisticsCollector {
	
	private static final class StatSet {
		final String mpId;
		long elementCount = 0L;
		long elapsedTimeMicros = 0L;
		long count = 0L;
		public StatSet(String mpId) {
			this.mpId = mpId;
		}
		void reset() {
			elementCount = 0L;
			elapsedTimeMicros = 0L;
			count = 0L;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(LoggingStatisticCollector.class);
	private static final Logger statLogger = LoggerFactory.getLogger("stat");

	private int loggingIntervalSec = 15;
	private boolean resetAfterLogging = false;
	
	private Thread thread;
	private boolean shutdown = false;
	private volatile Map<String,StatSet> map = new HashMap<String, StatSet>();
	private final Object mutex = new Object();

	public void setLoggingIntervalSec(int loggingIntervalSec) {
		this.loggingIntervalSec = loggingIntervalSec;
	}
	
	/**
	 * If set to true, the internal statistics are reseted after a each periodical logging.
	 */
	public void setResetAfterLogging(boolean resetAfterLogging) {
		this.resetAfterLogging = resetAfterLogging;
	}

	public synchronized void start() {
		if (thread != null) throw new IllegalStateException();
		thread = new Thread("StatisticsCollector") {
			@Override
			public void run() {
				while(!shutdown) {
					try {
						Thread.sleep(loggingIntervalSec*1000L);
						log();
						if (resetAfterLogging) {
							reset();
						}
					}
					catch(InterruptedException e) {
						// ignore
					}
					catch(Exception e) {
						logger.error("",e);
					}
				}
			}
		};
		thread.start();
	}

	public synchronized void shutdown() {
		if (shutdown)
			return;
		thread.interrupt();
		shutdown = true;
		
		log();
	}

	@Override
	public void submit(String measurePointId, int elementCount, long elapsedTime, TimeUnit timeUnit) {
		if (measurePointId == null) throw new NullPointerException();
		if (measurePointId.isEmpty()) throw new IllegalArgumentException();
		if (elapsedTime < 0) throw new IllegalArgumentException();
		if (elementCount < 0) throw new IllegalArgumentException();
		if (timeUnit == null) throw new NullPointerException();
		
		StatSet ss = map.get(measurePointId);
		if (ss == null) {
			synchronized (mutex) {
				ss = map.get(measurePointId);
				if (ss == null) {
					ss = new StatSet(measurePointId);
					Map<String,StatSet> map2 = new HashMap<String, StatSet>(map);
					map2.put(measurePointId, ss);
					map = map2;
				}
			}
		}
		final long delta = timeUnit.toMicros(elapsedTime);
		synchronized (ss) {
			ss.elapsedTimeMicros += delta;
			ss.elementCount += elementCount;
			ss.count++;
		}
	}

	public String print() {
		final Map<String, StatSet> localMap = map;
		final StringBuilder sb = new StringBuilder(1024);
		final List<StatSet> list = new ArrayList<LoggingStatisticCollector.StatSet>(localMap.values());
		Collections.sort(list,new Comparator<StatSet>() {
			@Override
			public int compare(StatSet o1, StatSet o2) {
				return o1.mpId.compareToIgnoreCase(o2.mpId);
			}
		});
		for (StatSet ss : list) {
			sb.append(toString(ss));
			sb.append("\n");
		}
		if (sb.length()>0) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
	
	public String print(String mpId) {
		final Map<String, StatSet> localMap = map;
		StatSet ss = localMap.get(mpId);
		if (ss == null) {
			return "-";
		}
		else {
			return toString(ss);
		}
	}
	
	private void log() {
		final Map<String, StatSet> localMap = map;
		final List<StatSet> list = new ArrayList<LoggingStatisticCollector.StatSet>(localMap.values());
		Collections.sort(list,new Comparator<StatSet>() {
			@Override
			public int compare(StatSet o1, StatSet o2) {
				return o1.mpId.compareToIgnoreCase(o2.mpId);
			}
		});
		for (StatSet ss : list) {
			statLogger.info(toString(ss));
		}
	}
	
	private String toString(StatSet ss) {
		final String DOTS = ".................................................1";
		long elementCount = 0L;
		long count = 0L;
		long elapsedTimeMicros = 0L;
		synchronized (ss) {
			elementCount = ss.elementCount;
			count = ss.count > 0 ? ss.count : 1;
			elapsedTimeMicros = ss.elapsedTimeMicros;
		}
		final long avgElementCount = elementCount/count;
		final double avgTimePerElement = elementCount > 0 ? (double)elapsedTimeMicros/(double)elementCount/1000.0 : 0.0;
		final double avgTimePerExecution = count > 0 ? (double)elapsedTimeMicros/(double)count/1000.0 : 0.0;
		return String.format("%1$55.55s #elements=%2$6d; avgCount=%3$6d; avgTime/Element=%4$12.5f msec; avgTime/Exec=%5$12.5f msec", ss.mpId+DOTS, elementCount, avgElementCount, avgTimePerElement, avgTimePerExecution);
	}
	
	public void reset() {
		logger.debug("Attention! Resetting current statistics");
		final Map<String, StatSet> localMap = map;
		for (StatSet ss : localMap.values()) {
			synchronized (ss) {
				ss.reset();
			}
		}
	}

	
}

