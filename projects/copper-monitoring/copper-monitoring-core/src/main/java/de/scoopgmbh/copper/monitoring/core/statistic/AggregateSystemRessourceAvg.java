package de.scoopgmbh.copper.monitoring.core.statistic;

import java.util.List;

import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;

public class AggregateSystemRessourceAvg implements AggregateFunction<SystemResourcesInfo, SystemResourcesInfo> {
	private static final long serialVersionUID = 3878459713886031263L;

	@Override
	public SystemResourcesInfo doAggregate(List<SystemResourcesInfo> group, TimeframeGroup<SystemResourcesInfo,SystemResourcesInfo> usedGroup) {
		double systemCpuLoadAvg= 0;
		long freePhysicalMemorySizeAvg= 0;
		double processCpuLoadAvg= 0;
		double heapMemoryUsageAvg= 0;
		long liveThreadsCountAvg= 0;
		long totalLoadedClassCountAvg= 0;
		for (SystemResourcesInfo value: group){
			systemCpuLoadAvg += value.getSystemCpuLoad();
			freePhysicalMemorySizeAvg += value.getFreePhysicalMemorySize();
			processCpuLoadAvg += value.getProcessCpuLoad();
			heapMemoryUsageAvg += value.getHeapMemoryUsage();
			liveThreadsCountAvg += value.getLiveThreadsCount();
			totalLoadedClassCountAvg += value.getTotalLoadedClassCount();
		}
		int size = group.size();
		if (size==0){
			size=1;
		}
		return new SystemResourcesInfo(usedGroup.getFrom(),
				systemCpuLoadAvg/size, 
				freePhysicalMemorySizeAvg/size, 
				processCpuLoadAvg/size, 
				heapMemoryUsageAvg/size, 
				liveThreadsCountAvg/size,
				totalLoadedClassCountAvg/size);
	}
}