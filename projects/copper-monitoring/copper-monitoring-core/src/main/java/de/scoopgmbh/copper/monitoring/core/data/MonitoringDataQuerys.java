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
import java.util.Date;
import java.util.List;

import de.scoopgmbh.copper.monitoring.core.data.filter.MonitoringDataFilter;
import de.scoopgmbh.copper.monitoring.core.statistic.StatisticCreator;

public interface MonitoringDataQuerys {
	
	public <T> List<T> getList(final MonitoringDataFilter<T> filter, Date from, Date to, long maxCount) throws RemoteException;

	public <T,R extends Serializable> List<List<R>> createStatistic(final MonitoringDataFilter<T> filter, final List<StatisticCreator<T, R>> statisticCreators,Date from, Date to) throws RemoteException;

	public Date getMonitoringDataMinDate() throws RemoteException;

	public Date getMonitoringDataMaxDate() throws RemoteException;
}
