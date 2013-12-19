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
package de.scoopgmbh.copper.monitoring.core.util;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Date;

import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;

public class PerformanceMonitor {

    private Method method_getProcessCpuLoad;
    private Method method_getSystemCpuLoad;
    private Method method_getFreePhysicalMemorySize;

    public PerformanceMonitor() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        try {
            method_getProcessCpuLoad = operatingSystemMXBean.getClass().getMethod("getProcessCpuLoad");
            method_getSystemCpuLoad = operatingSystemMXBean.getClass().getMethod("getSystemCpuLoad");
            method_getFreePhysicalMemorySize = operatingSystemMXBean.getClass().getMethod("getFreePhysicalMemorySize");
            method_getProcessCpuLoad.setAccessible(true);
            method_getSystemCpuLoad.setAccessible(true);
            method_getFreePhysicalMemorySize.setAccessible(true);
        } catch (Exception e) {
            // workaround to support legacy java versions the
            // Exception means no support
        }
    }

    private double boundValue(double value) {
        return Math.max(value, 0);
    }

    // http://docs.oracle.com/javase/7/docs/jre/api/management/extension/com/sun/management/OperatingSystemMXBean.html
    public SystemResourcesInfo createRessourcenInfo() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        java.lang.management.ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();

        double processCpuLoad = 0;
        double systemCpuLoad = 0;
        long freePhysicalMemorySize = 0;
        if (method_getProcessCpuLoad != null && method_getSystemCpuLoad != null && method_getFreePhysicalMemorySize != null) {
            try {
                processCpuLoad = (Double) method_getProcessCpuLoad.invoke(operatingSystemMXBean);
                systemCpuLoad = (Double) method_getSystemCpuLoad.invoke(operatingSystemMXBean);
                freePhysicalMemorySize = (Long) method_getFreePhysicalMemorySize.invoke(operatingSystemMXBean);
            } catch (Exception e) {
                // workaround to support legacy java versions the
                // Exception means no support
            }
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