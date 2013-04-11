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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.common.PriorityProcessorPool;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceHistory;

/**
 *	Contains the data for monitoring.
 *  Should only be accessed via the {@link MonitoringEventQueue}
 */
public class MonitoringData {
	public final Map<String,MeasurePointData> measurePoints = new HashMap<String, MeasurePointData>();
	public final List<PriorityProcessorPool> pools = new ArrayList<PriorityProcessorPool>();
	public final Map<String,ProcessingEngine> engines = new HashMap<String,ProcessingEngine>();
	public List<WorkflowInstanceHistory> workflowInstanceHistorys= new LinkedList<WorkflowInstanceHistory>();
	private long historyLimit=1000;
	
	public PriorityProcessorPool getPool(String poolId, String engineid){
		for (PriorityProcessorPool pool: pools){
			if (pool.getEngine().getEngineId().equals(engineid)){
				return pool;
			}
		}
		return null;
	}
	
	public MonitoringData(long historyLimit) {
		super();
		this.historyLimit=historyLimit;
	}
	
	public MonitoringData() {
		super();
	}
	
	public void addWorkflowInstanceHistorywitdhLimit(WorkflowInstanceHistory workflowInstanceHistory){
		if(workflowInstanceHistorys.size()>=historyLimit){
			workflowInstanceHistorys.remove(0);
		}
		workflowInstanceHistorys.add(workflowInstanceHistory);
	}
	
	
	public List<ProcessingEngineInfo> createProcessingEngineInfos(){
		ArrayList<ProcessingEngineInfo> result = new ArrayList<ProcessingEngineInfo>();
		for (ProcessingEngine engine: engines.values()){
			ProcessingEngineInfo engineInfo = engine.getEngineInfo();
			result.add(engineInfo);
			for (PriorityProcessorPool pool: pools){
				if (pool.getEngine()==engine){
					engineInfo.getPools().add(pool.getProcessorPoolInfo());
				}
			}
		}
		return result; 
	}
	
	public ProcessingEngineInfo createProcessingEngineInfos(String engineId){
		List<ProcessingEngineInfo> list = createProcessingEngineInfos();
		for (ProcessingEngineInfo processingEngineInfo: list){
			if (processingEngineInfo.getId().equals(engineId)){
				return processingEngineInfo;
			}
		}
		return null; 
	}
	
	public WorkflowRepository getWorkflowRepository(String engineId){
		if (engines.containsKey(engineId)){
			return engines.get(engineId).getWfRepository();
		}
		return null; 
	}
	
}
