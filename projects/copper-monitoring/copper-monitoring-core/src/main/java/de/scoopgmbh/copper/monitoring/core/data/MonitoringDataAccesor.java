package de.scoopgmbh.copper.monitoring.core.data;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;

import de.scoopgmbh.copper.monitoring.core.data.filter.MonitoringDataFilter;
import de.scoopgmbh.copper.monitoring.core.statistic.StatisticCreator;

/**

 *
 */
public class MonitoringDataAccesor implements Serializable, MonitoringDataQuerys{
	private static final Logger logger = LoggerFactory.getLogger(MonitoringDataAccesor.class);
	private static final long serialVersionUID = 1L;

	MonitoringDataStorage monitoringDataStorage; 
	public MonitoringDataAccesor(MonitoringDataStorage readableInput) {
		this.monitoringDataStorage = readableInput;
	}
	
	@Override
	public <T> List<T> getList(final MonitoringDataFilter<T> filter, Date from, Date to, long maxCount){
		ArrayList<T> result = new ArrayList<T>();
		int counter=0;
		final Iterable<Input> read = monitoringDataStorage.read(from,to);
		for (Input input: read){
			Object object = serialize(input);
			if (filter.isValid(object)){
				result.add(filter.castValid(object));
				counter++;
			}
			if (counter>=maxCount){
				break;
			}
		}
		return result;
	}
	
	private Object serialize(Input input){
		Object object=null;
		try {
			object = SerializeUtil.getKryo().readClassAndObject(input);
		} catch (KryoException e){
			logger.warn("cant serialize old monitoring data", e);
		}
		return object;
	}
	
	@Override
	public Date getMonitoringhDataMinDate(){
		return monitoringDataStorage.getMinDate();
	}
	
	@Override
	public Date getMonitoringhDataMaxDate(){
		return monitoringDataStorage.getMaxDate();	
	}

	@Override
	public <T, R extends Serializable> List<List<R>> createStatistic(MonitoringDataFilter<T> filter,
			List<StatisticCreator<T, R>> statisticCreators, Date from, Date to) throws RemoteException {
		final Iterable<Input> read = monitoringDataStorage.read(from,to);
		for (Input input: read){
			Object object = serialize(input);
			if (filter.isValid(object)){
				for (StatisticCreator<T,R> statisticCreator: statisticCreators){
					statisticCreator.add(filter.castValid(object));
				}
			}
		}
		List<List<R>> result = new ArrayList<List<R>>();
		for (StatisticCreator<T,R> statisticCreator: statisticCreators){
			result.add(statisticCreator.getAggregatedResult());
		}
		return result;
	}
}