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
package de.scoopgmbh.copper.monitoring.server;

import java.util.List;

import de.scoopgmbh.copper.management.ProcessingEngineMXBean;
import de.scoopgmbh.copper.management.StatisticsCollectorMXBean;
import de.scoopgmbh.copper.monitoring.core.CopperMonitorInterface;
import de.scoopgmbh.copper.monitoring.server.persistent.MonitoringDbStorage;
import de.scoopgmbh.copper.monitoring.server.workaround.HistoryCollectorMXBean;

public class DefaultCopperMonitorInterfaceFactory implements CopperMonitorInterfaceFactory{

	private final MonitoringDbStorage dbStorage;
	private final StatisticsCollectorMXBean statisticsCollectorMXBean;
	private final HistoryCollectorMXBean historyCollectorMXBean;
	private final List<ProcessingEngineMXBean> engineList;
	
	public DefaultCopperMonitorInterfaceFactory(MonitoringDbStorage dbStorage, 
			StatisticsCollectorMXBean statisticsCollectorMXBean,
			List<ProcessingEngineMXBean> engineList, 
			HistoryCollectorMXBean historyCollectorMXBean){
		this.dbStorage = dbStorage;
		this.statisticsCollectorMXBean = statisticsCollectorMXBean;
		this.historyCollectorMXBean = historyCollectorMXBean;
		this.engineList=engineList;
	}

	@Override
	public CopperMonitorInterface createCopperMonitorInterface() {
		return new DefaultCopperMonitorInterface(dbStorage, statisticsCollectorMXBean, engineList, historyCollectorMXBean);
	}

}
