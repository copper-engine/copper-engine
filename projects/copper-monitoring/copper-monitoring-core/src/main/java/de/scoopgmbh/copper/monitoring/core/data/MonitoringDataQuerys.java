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
