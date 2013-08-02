package de.scoopgmbh.copper.monitoring.example.monitoringprovider;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;

import de.scoopgmbh.copper.monitoring.core.model.GenericMonitoringData;
import de.scoopgmbh.copper.monitoring.core.model.GenericMonitoringData.ContentType;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;
import de.scoopgmbh.copper.monitoring.server.provider.RepeatingMonitoringDataProviderBase;

public class GcDataProvider extends RepeatingMonitoringDataProviderBase{

	public GcDataProvider(MonitoringDataCollector monitoringDataCollector) {
		super(monitoringDataCollector);
	}

	@Override
	protected void provideData() {
		StringBuilder result = new StringBuilder();
		result.append("<html>");
		List<java.lang.management.GarbageCollectorMXBean> gcmxb = ManagementFactory.getGarbageCollectorMXBeans();
		for (java.lang.management.GarbageCollectorMXBean ob : gcmxb) {
			result.append("name of memory manager:" + ob.getName()+"<br/>");
			result.append("CollectionTime:" + ob.getCollectionTime()+"<br/>");
		}
		result.append("</html>");
		GenericMonitoringData  dat = new GenericMonitoringData(new Date(),result.toString(),ContentType.HTML,"GcData");
		monitoringDataCollector.submitGenericMonitoringData(dat);
	}

}
