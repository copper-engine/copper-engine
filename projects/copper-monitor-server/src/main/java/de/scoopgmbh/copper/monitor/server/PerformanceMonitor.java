package de.scoopgmbh.copper.monitor.server;

import java.lang.management.ManagementFactory;
import java.util.Date;

import de.scoopgmbh.copper.monitor.adapter.model.SystemResourcesInfo;

public class PerformanceMonitor { 

    public double getCpuUsage() {
       return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getSystemCpuLoad();
    }
    
    //http://docs.oracle.com/javase/7/docs/jre/api/management/extension/com/sun/management/OperatingSystemMXBean.html
    public SystemResourcesInfo getRessourcenInfo(){
    	com.sun.management.OperatingSystemMXBean operatingSystemMXBean= ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean());
    	return new SystemResourcesInfo(new Date(),
    			operatingSystemMXBean.getSystemCpuLoad(),
    			operatingSystemMXBean.getFreePhysicalMemorySize(),
    			operatingSystemMXBean.getProcessCpuLoad());
    }

}