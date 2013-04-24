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
package de.scoopgmbh.copper.monitoring.server;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Date;

import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;

public class PerformanceMonitor { 

    private double boundValue(double value){
    	return Math.max(value, 0);
    }
    
    //http://docs.oracle.com/javase/7/docs/jre/api/management/extension/com/sun/management/OperatingSystemMXBean.html
    public SystemResourcesInfo getRessourcenInfo(){
    	OperatingSystemMXBean operatingSystemMXBean= ManagementFactory.getOperatingSystemMXBean();
    	MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    	java.lang.management.ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    	ClassLoadingMXBean classLoadingMXBean  = ManagementFactory.getClassLoadingMXBean();
    	
    	double processCpuLoad=0;
    	double systemCpuLoad=0;
    	long freePhysicalMemorySize=0;
    	try {
    		//part of com.sun.management.OperatingSystemMXBean only available at >= java  1.7
    		Method method = operatingSystemMXBean.getClass().getMethod("getProcessCpuLoad");
    		method.setAccessible(true);
			processCpuLoad = (Double)method.invoke(operatingSystemMXBean);
    		Method method2 = operatingSystemMXBean.getClass().getMethod("getSystemCpuLoad");
    		method2.setAccessible(true);
			systemCpuLoad = (Double)method2.invoke(operatingSystemMXBean);
    		freePhysicalMemorySize = (Long)operatingSystemMXBean.getClass().getMethod("getFreePhysicalMemorySize").invoke(operatingSystemMXBean);
    	} catch (Exception e) {
    		//workaround to support legacy java versions the 
    		//Exception means no support
    	}
    	return new SystemResourcesInfo(new Date(),
    			boundValue(systemCpuLoad),
    			freePhysicalMemorySize,
    			boundValue(processCpuLoad),
    			memoryMXBean.getHeapMemoryUsage().getUsed(),
    			threadMXBean.getThreadCount(),
    			classLoadingMXBean.getTotalLoadedClassCount());
    }

}