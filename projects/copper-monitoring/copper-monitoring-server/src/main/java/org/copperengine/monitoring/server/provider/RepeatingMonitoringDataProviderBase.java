/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.server.provider;

import org.copperengine.monitoring.server.monitoring.MonitoringDataCollector;

public abstract class RepeatingMonitoringDataProviderBase extends MonitoringDataProviderBase {

    private final class PollThread extends Thread {
        public volatile boolean stop;

        {
            setDaemon(true);
        }

        @Override
        public void run() {
            while (!stop) {
                long starttime = System.currentTimeMillis();
                provideData();
                long passedTime = System.currentTimeMillis() - starttime;
                try {
                    sleep(Math.max(getMinInterval(), (passedTime * 10)));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public boolean isStoped() {
            return stop;
        }

        public void stopThread() {
            stop = true;
        }
    }

    protected int getMinInterval() {
        return 1000;
    }

    protected final MonitoringDataCollector monitoringDataCollector;
    private PollThread thread;

    public RepeatingMonitoringDataProviderBase(MonitoringDataCollector monitoringDataCollector) {
        super();
        this.monitoringDataCollector = monitoringDataCollector;
    }

    protected abstract void provideData();

    @Override
    public void startProvider() {
        super.startProvider();
        if (thread == null || thread.isStoped()) {
            thread = new PollThread();
            thread.start();
        }
    }

    @Override
    public void stopProvider() {
        thread.stopThread();
        super.stopProvider();
    }

}
