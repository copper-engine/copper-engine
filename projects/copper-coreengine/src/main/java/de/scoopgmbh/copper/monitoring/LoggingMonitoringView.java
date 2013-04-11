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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;


/**
 * periodically logs monitoring infos
 */
public class LoggingMonitoringView{
	private static final Logger statLogger = LoggerFactory.getLogger("stat");
	
	public LoggingMonitoringView(final MonitoringEventQueue monitoringQueue, final int loggingIntervalSec){
		Thread output = new Thread("Updater"){
			public void run() {
				while(true) {
					try {
						Thread.sleep(loggingIntervalSec*1000L);
						monitoringQueue.put(new MonitoringDataAwareRunnable() {
							@Override
							public void run() {
								log(monitoringData);
							}
						});
					} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
				
			};
		};
		output.setDaemon(true);
		output.start();
	}
	
	public LoggingMonitoringView(final MonitoringEventQueue monitoringQueue){
		this(monitoringQueue,3);
	}
	
	public LoggingMonitoringView(){
		this(new MonitoringEventQueue());
	}

	void log(MonitoringData monitoringData) {
		StringBuilder builder = new StringBuilder("\nCopper Monitoring\n");
		builder.append("----------Configuration:----------\n");
		for (ProcessingEngineInfo engineInfo: monitoringData.createProcessingEngineInfos()){
			builder.append(engineInfo.toString()+"\n");
		}
		final Map<String, MeasurePointData> localMap = monitoringData.measurePoints;
		final List<MeasurePointData> list = new ArrayList<MeasurePointData>(localMap.values());
		Collections.sort(list,new Comparator<MeasurePointData>() {
			@Override
			public int compare(MeasurePointData o1, MeasurePointData o2) {
				return o1.getMeasurePointId().compareToIgnoreCase(o2.getMeasurePointId());
			}
		});
		builder.append("----------Statistic:----------\n");
		for (MeasurePointData measurePointData : list) {
			builder.append(measurePointData.toString()+"\n");
		}
		
		statLogger.info(builder.toString());
	}
	

	


}
