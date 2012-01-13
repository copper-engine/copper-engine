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
package de.scoopgmbh.copper.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.Workflow;

/**
 * Default implementation of the {@link TicketPoolManager} interface, using a java maps.
 * 
 * This implementation is thread safe.
 * 
 * @author austermann
 *
 */
public class DefaultTicketPoolManager implements TicketPoolManager {
	
	private static final Logger logger = Logger.getLogger(DefaultTicketPoolManager.class);
	public static final String DEFAULT_POOL_ID = "DEFAULT";
	private volatile Map<String, TicketPool> map = new HashMap<String, TicketPool>();
	
	private final Object mutex = new Object();
	private final Map<String, String> mappingDesc = new HashMap<String, String>();
	private volatile Map<String, TicketPool> concreteMapping = Collections.emptyMap();
	
	public DefaultTicketPoolManager() {
		map.put(DEFAULT_POOL_ID, new TicketPool(DefaultTicketPoolManager.DEFAULT_POOL_ID, 2000));
	}

	@Override
	public boolean exists(String id) {
		return map.containsKey(id);
	}

	@Override
	public TicketPool getTicketPool(String id) {
		if (id == null) throw new NullPointerException();
		if (id.length() == 0) throw new IllegalArgumentException();
		
		final Map<String, TicketPool> map = this.map;
		TicketPool tp = map.get(id);
		if (tp == null) {
			logger.error("Ticket pool '"+id+"' does not exist - returning default pool");
			tp = map.get(DEFAULT_POOL_ID);
		}
		assert tp != null;
		return tp;
	}

	@Override
	public synchronized void add(TicketPool tp) {
		HashMap<String, TicketPool> newMap = new HashMap<String, TicketPool>(map);
		newMap.put(tp.getId(),tp);
		map = newMap;
	}

	@Override
	public synchronized void remove(TicketPool tp) {
		HashMap<String, TicketPool> newMap = new HashMap<String, TicketPool>(map);
		newMap.remove(tp.getId());
		map = newMap;
	}

	@Override
	public void shutdown() {
		// empty
	}

	@Override
	public void startup() {
		// empty
	}

	@Override
	public synchronized void setTicketPools(List<TicketPool> ticketPools) {
		HashMap<String, TicketPool> newMap = new HashMap<String, TicketPool>(map);
		for (TicketPool tp : ticketPools) {
			newMap.put(tp.getId(),tp);
		}
		map = newMap;
	}

	@Override
	public void obtain(Workflow<?> wf) {
		findPool(wf.getClass().getName()).obtain();
	}
	
	/**
	 * For testing..
	 * @param wf
	 */
	public String obtainAndReturnTicketPoolId(Workflow<?> wf) {
		TicketPool tp = findPool(wf.getClass().getName());
		tp.obtain();
		return tp.getId();
	}	

	@Override
	public void release(Workflow<?> wf) {
		findPool(wf.getClass().getName()).release();
	}

	@Override
	public void addMapping(String workflowClass, String ticketPoolId) {
		if (workflowClass == null) throw new NullPointerException();
		if (ticketPoolId == null) throw new NullPointerException();
		if (!map.containsKey(ticketPoolId)) {
			throw new IllegalArgumentException("TicketPool '"+ticketPoolId+"' does not exist");
		}
		synchronized (mutex) {
			mappingDesc.put(workflowClass, ticketPoolId);
			concreteMapping = Collections.emptyMap();
		}
	}

	@Override
	public void removeMapping(String workflowClass) {
		if (workflowClass == null) throw new NullPointerException();
		synchronized (mutex) {
			mappingDesc.remove(workflowClass);
			concreteMapping = Collections.emptyMap();
		}
	}

	@Override
	public void setMapping(Map<String, String> mapping) {
		if (mapping == null) throw new NullPointerException();
		synchronized (mutex) {
			for (Map.Entry<String, String> entry : mapping.entrySet()) {
				if (entry.getKey() == null) throw new NullPointerException();
				if (entry.getValue() == null) throw new NullPointerException();
				if (!map.containsKey(entry.getValue())) {
					throw new IllegalArgumentException("TicketPool '"+entry.getValue()+"' does not exist");
				}
				mappingDesc.put(entry.getKey(), entry.getValue());
			}
			concreteMapping = Collections.emptyMap();
		}
	}
	
	private TicketPool findPool(String wfClass) {
		TicketPool tp = concreteMapping.get(wfClass);
		if (tp == null) {
			synchronized (mutex) {
				tp = concreteMapping.get(wfClass);
				if (tp == null) {
					HashMap<String, TicketPool> newConcreteMapping = new HashMap<String, TicketPool>(concreteMapping);
					for (Map.Entry<String, String> entry : mappingDesc.entrySet()) {
						String c = entry.getKey();
						if (wfClass.equals(c)) {
							tp = map.get(entry.getValue());
						}
					}
					if (tp == null) {
						tp = map.get(DEFAULT_POOL_ID);
					}
					logger.info("Mapping workflow class '"+wfClass+"' to ticket pool "+tp.getId());
					newConcreteMapping.put(wfClass, tp);
					concreteMapping = newConcreteMapping;
				}
			}
		}
		assert tp != null;
		return tp;
	}

	@Override
	public void obtain(String workflowClass) {
		findPool(workflowClass).obtain();
		
	}

	@Override
	public void release(String workflowClass) {
		findPool(workflowClass).release();
	}

}
