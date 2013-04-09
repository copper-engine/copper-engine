package de.scoopgmbh.copper.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.common.PriorityProcessorPool;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;

/**
 *	Contains the data for monitoring.
 *  Should only be accessed via the {@link MonitoringEventQueue}
 */
public class MonitoringData {
	public final Map<String,MeasurePointData> measurePoints = new HashMap<String, MeasurePointData>();
	public final List<PriorityProcessorPool> pools = new ArrayList<PriorityProcessorPool>();
	public final Map<String,ProcessingEngine> engines = new HashMap<String,ProcessingEngine>();
	public WorkflowRepository workflowRepository;
	
	public PriorityProcessorPool getPool(String poolId, String engineid){
		for (PriorityProcessorPool pool: pools){
			if (pool.getEngine().getEngineId().equals(engineid)){
				return pool;
			}
		}
		return null;
	}
	
	public MonitoringData() {
		super();
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
	
}
