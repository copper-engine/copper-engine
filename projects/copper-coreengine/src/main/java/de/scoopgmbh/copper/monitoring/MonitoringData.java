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
