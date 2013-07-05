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
package de.scoopgmbh.copper.monitoring.server.provider;

import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;

public class MonitoringDataProviderFactory {
	private final MonitoringDataCollector monitoringDataCollector;
	
	public MonitoringDataProviderFactory(MonitoringDataCollector monitoringDataCollector) {
		super();
		this.monitoringDataCollector = monitoringDataCollector;
	}

	public void createAndStartProvider(){
		new MonitoringLog4jDataProvider(monitoringDataCollector);
		new SystemRessourceDataProvider(monitoringDataCollector).start();
	}

}
