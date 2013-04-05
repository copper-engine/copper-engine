package de.scoopgmbh.copper.monitoring;

import java.util.HashMap;
import java.util.Map;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.common.PriorityProcessorPool;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;

public class MonitoringData {
	public final Map<String,MeasurePointData> measurePoints = new HashMap<String, MeasurePointData>();
	public final Map<String,ProcessingEngineInfo> engineInfos = new HashMap<String,ProcessingEngineInfo>();
	private final Map<String,PriorityProcessorPool> pools = new HashMap<String,PriorityProcessorPool>();
	public final Map<String,ProcessingEngine> engines = new HashMap<String,ProcessingEngine>();
	public WorkflowRepository workflowRepository;
	
	public void putPool(String poolId, String engineid, PriorityProcessorPool pool){
		pools.put(poolId +engineid, pool);
	}
	
	public PriorityProcessorPool getPool(String poolId, String engineid){
		return pools.get(poolId +engineid);
	}
	
	public MonitoringData() {
		super();
	}

}
