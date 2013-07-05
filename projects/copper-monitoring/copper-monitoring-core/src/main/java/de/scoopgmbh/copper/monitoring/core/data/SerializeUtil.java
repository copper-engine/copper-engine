package de.scoopgmbh.copper.monitoring.core.data;

import java.util.ArrayList;
import java.util.Date;

import com.esotericsoftware.kryo.Kryo;

import de.scoopgmbh.copper.monitoring.core.model.AdapterCallInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfNotifyInfo;
import de.scoopgmbh.copper.monitoring.core.model.LogEvent;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;

public class SerializeUtil{
	
	private static Kryo kryo;

	public static Kryo getKryo(){
		if (kryo==null){
			kryo = new Kryo();
			kryo.register(Date.class);
			kryo.register(ArrayList.class);
			kryo.register(AdapterCallInfo.class);
			kryo.register(AdapterWfNotifyInfo.class);
			kryo.register(AdapterWfLaunchInfo.class);
			kryo.register(MeasurePointData.class);
			kryo.register(LogEvent.class);
			kryo.register(SystemResourcesInfo.class);
		}
		return kryo;
	}

}
