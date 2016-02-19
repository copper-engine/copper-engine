/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.monitoring.core.util;

import java.lang.reflect.Method;

import org.copperengine.monitoring.core.model.SystemResourcesInfo;

public class CachingPerformanceMonitor {

    private Method method_getProcessCpuLoad;
    private Method method_getSystemCpuLoad;
    private Method method_getFreePhysicalMemorySize;

    public CachingPerformanceMonitor(final PerformanceMonitor performanceMonitor) {
        cachedSystemResourcesInfo=performanceMonitor.createResourcesInfo();
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    cachedSystemResourcesInfo=performanceMonitor.createResourcesInfo();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    volatile SystemResourcesInfo cachedSystemResourcesInfo;

    /**
     *
     * @return cached SystemResources updated automatically every 500ms
     */
    public SystemResourcesInfo getCachedSystemResourcesInfo(){
        return cachedSystemResourcesInfo;
    }

}