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
package org.copperengine.monitoring.server.provider;

import java.util.Date;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.copperengine.monitoring.core.model.MonitoringDataProviderInfo;
import org.copperengine.monitoring.server.monitoring.MonitoringDataCollector;

public class MonitoringLog4jDataProvider extends AppenderSkeleton implements MonitoringDataProvider {

    MonitoringDataCollector monitoringDataCollector;

    public MonitoringLog4jDataProvider(MonitoringDataCollector monitoringDataCollector) {
        super();
        this.monitoringDataCollector = monitoringDataCollector;
    }

    public void addToRootLogger() {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.addAppender(this);
    }

    public void removeFromRootLogger() {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.removeAppender(this);
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    protected void append(LoggingEvent event) {
        monitoringDataCollector.submitLogEvent(new Date(event.getTimeStamp()), event.getLevel().toString(), event.getLocationInformation().fullInfo, event.getRenderedMessage());
    }

    public Status status = Status.CREATED;

    @Override
    public void startProvider() {
        status = Status.STARTED;
        addToRootLogger();
    }

    @Override
    public void stopProvider() {
        removeFromRootLogger();
        status = Status.STOPPED;
    }

    @Override
    public Status getProviderStatus() {
        return status;
    }

    @Override
    public String getProviderName() {
        return getClass().getSimpleName();
    }

    @Override
    public MonitoringDataProviderInfo createInfo() {
        return new MonitoringDataProviderInfo(getProviderName(), getProviderStatus().toString());
    }

}
