package de.scoopgmbh.copper.monitoring;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.common.PriorityProcessorPool;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo.EngineTyp;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessorPoolInfo;

public class DefaultMonitoringDataCollector implements MonitoringDataCollector{
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultMonitoringDataCollector.class);
	private final MonitoringEventQueue monitoringQueue;
	
	public DefaultMonitoringDataCollector(final MonitoringEventQueue monitoringQueue){
		this.monitoringQueue = monitoringQueue; 
		
	}
	
	@Override
	public void submitMeasurePoint(final String measurePointId, final int elementCount, final long elapsedTime, final TimeUnit timeUnit) {
		if (measurePointId == null) throw new NullPointerException();
		if (measurePointId.isEmpty()) throw new IllegalArgumentException();
		if (elapsedTime < 0) throw new IllegalArgumentException();
		if (elementCount < 0) throw new IllegalArgumentException();
		if (timeUnit == null) throw new NullPointerException();
		
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				MeasurePointData measurePointData = monitoringData.measurePoints.get(measurePointId);
				if (measurePointData == null) {
					measurePointData = new MeasurePointData(measurePointId);
					monitoringData.measurePoints.put(measurePointId, measurePointData);
				}
				final long delta = timeUnit.toMicros(elapsedTime);
				measurePointData.update(elementCount, delta);
			}
		});
	}

	@Override
	public void resgisterEngine(final String engineId, final EngineTyp typ, final ProcessingEngine engine) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringData.engineInfos.put(engineId,new ProcessingEngineInfo(typ, engineId));
				monitoringData.engines.put(engineId, engine);
			}
		});
	}
	
	@Override
	public void registerPool(final ProcessorPoolInfo processorPoolInfo, final String engineId, final PriorityProcessorPool pool) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				ProcessingEngineInfo processingEngineInfo = monitoringData.engineInfos.get(engineId);
				if (processingEngineInfo==null){
					logger.info("cant find engine for pool: '"+processorPoolInfo.getId()+"'");
				} else {
					processingEngineInfo.getPools().add(processorPoolInfo);
				}
				monitoringData.putPool(pool.getId(),engineId, pool);
			}
		});
	}
	
	@Override
	public void registerWorkflowRepository(final WorkflowRepository workflowRepository) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringData.workflowRepository=workflowRepository;
			}
		});
	}

}
