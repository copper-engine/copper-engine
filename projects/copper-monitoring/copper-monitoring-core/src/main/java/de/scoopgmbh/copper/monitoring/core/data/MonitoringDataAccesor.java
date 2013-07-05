package de.scoopgmbh.copper.monitoring.core.data;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.esotericsoftware.kryo.io.Input;

import de.scoopgmbh.copper.monitoring.core.model.AdapterCallInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterHistoryInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfNotifyInfo;
import de.scoopgmbh.copper.monitoring.core.model.LogEvent;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitoring.core.statistic.StatisticCreator;
import de.scoopgmbh.copper.monitoring.core.util.ReadableInput;

/**

 *
 */
public class MonitoringDataAccesor implements Serializable{
	private static final long serialVersionUID = 1L;

	ReadableInput readableInput; 
	
	public MonitoringDataAccesor(ReadableInput readableInput) {
		this.readableInput = readableInput;
	}
	
	public MonitoringDataAccesor getData(){
		return new MonitoringDataAccesor(readableInput);
	}

	public List<AdapterCallInfo> getAdapterCalls() {
		return this.<AdapterCallInfo>getList(AdapterCallInfo.class);
	}

	public List<AdapterWfNotifyInfo> getAdapterWfNotifies() {
		return this.<AdapterWfNotifyInfo>getList(AdapterWfNotifyInfo.class);
	}

	public List<AdapterWfLaunchInfo> getAdapterWfLaunches() {
		return this.<AdapterWfLaunchInfo>getList(AdapterWfLaunchInfo.class);
	}

	public List<MeasurePointData> getMeasurePoints() {
		return this.<MeasurePointData>getList(MeasurePointData.class);
	}
	
	public List<LogEvent> getLogEvents() {
		return this.<LogEvent>getList(LogEvent.class);
	}

	public List<MeasurePointData> getMonitoringMeasurePoints(final String measurePoint, final long limit) throws RemoteException {
		ArrayList<MeasurePointData> result = new ArrayList<MeasurePointData>();
		final List<MeasurePointData> measurePoints = getMeasurePoints();
		Collections.reverse(measurePoints);
		for (MeasurePointData measurePointData: measurePoints){
			if (measurePoint==null || measurePoint.isEmpty() || measurePoint.equals(measurePointData.getMeasurePointId())){
				result.add(measurePointData);
			}
			if (result.size()>=limit){
				break;
			}
		}
		return result;
	}
	
	public List<LogEvent> getFilteredLogEvents(){
		final List<LogEvent> logEvents = getLogEvents();
		if (logEvents.isEmpty()){
			logEvents.add(new LogEvent(new Date(),"No logs found. probably missing: MonitoringLog4jDataProvider","","ERROR"));
		}
		return logEvents;
	}
	
	public AdapterHistoryInfo getAdapterHistoryInfos(final String adapterId) throws RemoteException {
		final List<AdapterCallInfo> adapterCalls = new ArrayList<AdapterCallInfo>();
		for (AdapterCallInfo adapterCallInfo : getAdapterCalls()) {
			if (adapterId == null || adapterId.isEmpty() || adapterId.equals(adapterCallInfo.getAdapterName())) {
				adapterCalls.add(adapterCallInfo);
			}
		}
		final List<AdapterWfLaunchInfo> adapterWfLaunches = new ArrayList<AdapterWfLaunchInfo>();
		for (AdapterWfLaunchInfo adapterWfLaunchInfo : getAdapterWfLaunches()) {
			if (adapterId == null || adapterId.isEmpty() || adapterId.equals(adapterWfLaunchInfo.getAdapterName())) {
				adapterWfLaunches.add(adapterWfLaunchInfo);
			}
		}
		final List<AdapterWfNotifyInfo> adapterWfNotifies = new ArrayList<AdapterWfNotifyInfo>();
		for (AdapterWfNotifyInfo adapterWfNotifyInfo : getAdapterWfNotifies()) {
			if (adapterId == null || adapterId.isEmpty() || adapterId.equals(adapterWfNotifyInfo.getAdapterName())) {
				adapterWfNotifies.add(adapterWfNotifyInfo);
			}
		}
		return new AdapterHistoryInfo(adapterCalls, adapterWfLaunches, adapterWfNotifies);
	}
	
	public List<String> getMonitoringMeasurePointIds() throws RemoteException {
		HashSet<String> ids = new HashSet<String>();
		for (MeasurePointData measurePointData : getMeasurePoints()){
			ids.add(measurePointData.getMeasurePointId());
		}
		return new ArrayList<String>(ids);
	}

	public List<SystemResourcesInfo> getSystemResourcesInfos() {
		return getList(SystemResourcesInfo.class);
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<T> clazz){
		ArrayList<T> result = new ArrayList<T>();
		for (Input input: readableInput.read()){
			Object object = SerializeUtil.getKryo().readClassAndObject(input);
			if (object.getClass().isAssignableFrom(clazz)){
				result.add((T)object);
			}
		}
		return result;
	}
	
	public Date getMinDate(){
		return readableInput.getMinDate();
	}
	
	public Date getMaxDate(){
		return readableInput.getMaxDate();	
	}
	
	@SuppressWarnings("unchecked")
	public <T,U> List<U> getListGrouped(Class<T> clazz, StatisticCreator<T,U> statisticCreator){
		for (Input input: readableInput.read()){
			Object object = SerializeUtil.getKryo().readClassAndObject(input);
			if (object.getClass().isAssignableFrom(clazz)){
				statisticCreator.add((T)object);
			}
		}
		return statisticCreator.getAggregatedResult();
	}

	public ReadableInput getReadableInput() {
		return readableInput;
	}
}