package de.scoopgmbh.copper.monitoring.core.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import de.scoopgmbh.copper.monitoring.core.model.AdapterCallInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfNotifyInfo;
import de.scoopgmbh.copper.monitoring.core.model.LogEvent;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;

/**
 * wrapper for {@link MonitoringDataStorage} to add Data 
 */
public class MonitoringDataAdder{
	
	MonitoringDataStorage monitoringDataStorage;
	
	Kryo kryo = SerializeUtil.getKryo();;


	public MonitoringDataAdder(MonitoringDataStorage monitoringDataStorage) {
		super();
		
		this.monitoringDataStorage = monitoringDataStorage;
	}

	private void addInternal(Object object, Date referenceDate) {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Output output = new Output(outputStream);
		kryo.writeClassAndObject(output, object);
		try {
			final byte[] bytes = output.toBytes();
			monitoringDataStorage.write(referenceDate,bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public MonitoringDataAdder getData(){
		return new MonitoringDataAdder(monitoringDataStorage);
	}
	
	public void add(AdapterCallInfo adapterCall){
		addInternal(adapterCall,adapterCall.getTimestamp());
	}
	
	public void add(AdapterWfNotifyInfo adapterWfNotifyInfo){
		addInternal(adapterWfNotifyInfo,adapterWfNotifyInfo.getTimestamp());
	}
	
	public void add(AdapterWfLaunchInfo adapterWfLaunch){
		addInternal(adapterWfLaunch,adapterWfLaunch.getTimestamp());
	}
	
	public void add(MeasurePointData measurePoint){
		addInternal(measurePoint,measurePoint.getTime());
	}
	
	public void add(LogEvent logEvent){
		addInternal(logEvent,logEvent.getTime());
	}

	public void add(SystemResourcesInfo resourcesInfo) {
		addInternal(resourcesInfo,resourcesInfo.getTimestamp());
	}

}