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
package de.scoopgmbh.copper.monitoring.core.data;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.scoopgmbh.copper.monitoring.core.data.filter.MonitoringDataFilter;
import de.scoopgmbh.copper.monitoring.core.model.MonitoringData;
import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataStorageInfo;
import de.scoopgmbh.copper.monitoring.core.statistic.StatisticCreator;

/**

 * warpper for {@link MonitoringDataStorage } to read data
 */
public class MonitoringDataAccesor implements Serializable, MonitoringDataQuerys{
	private static final long serialVersionUID = 1L;

	MonitoringDataStorage monitoringDataStorage; 
	public MonitoringDataAccesor(MonitoringDataStorage readableInput) {
		this.monitoringDataStorage = readableInput;
	}
	
	@Override
	public <T> List<T> getList(final MonitoringDataFilter<T> filter, Date from, Date to, long maxCount){
		ArrayList<T> result = new ArrayList<T>();
		int counter=0;
		final Iterable<MonitoringData> read = monitoringDataStorage.readReverse(from,to);
		for (MonitoringData data: read){
			if (filter.isValid(data)){
				result.add(filter.castValid(data));
				counter++;
			}
			if (counter>=maxCount){
				break;
			}
		}
		return result;
	}
	
	@Override
	public Date getMonitoringDataMinDate(){
		return monitoringDataStorage.getMinDate();
	}
	
	@Override
	public Date getMonitoringDataMaxDate(){
		return monitoringDataStorage.getMaxDate();	
	}

	@Override
	public <T, R extends Serializable> List<List<R>> createStatistic(MonitoringDataFilter<T> filter,
			List<StatisticCreator<T, R>> statisticCreators, Date from, Date to) throws RemoteException {
		final Iterable<MonitoringData> read = monitoringDataStorage.read(from,to);
		for (MonitoringData data: read){
			if (filter.isValid(data)){
				for (StatisticCreator<T,R> statisticCreator: statisticCreators){
					statisticCreator.add(filter.castValid(data));
				}
			}
		}
		List<List<R>> result = new ArrayList<List<R>>();
		for (StatisticCreator<T,R> statisticCreator: statisticCreators){
			result.add(statisticCreator.getAggregatedResult());
		}
		return result;
	}

	public MonitoringDataStorageInfo getMonitroingDataStorageInfo() {
		return monitoringDataStorage.getMonitroingDataStorageInfo();
	}
}