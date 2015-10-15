/*
 * Copyright 2002-2015 SCOOP Software GmbH
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

import org.copperengine.monitoring.core.model.MonitoringDataProviderInfo;
import org.copperengine.monitoring.server.monitoring.MonitoringDataCollector;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class MonitoringLogbackDataProvider extends AppenderBase<ILoggingEvent> implements MonitoringDataProvider {

    public static final String APPENDER_NAME = "MonitoringLogDataProviderAppender";

    MonitoringDataCollector monitoringDataCollector;

    public MonitoringLogbackDataProvider(MonitoringDataCollector monitoringDataCollector) {
        super();
        this.monitoringDataCollector = monitoringDataCollector;

        this.setName(APPENDER_NAME);
    }

    public void addToRootLogger() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        setContext(root.getLoggerContext());
        root.addAppender(this);
        start();
    }

    public void removeFromRootLogger() {
        stop();
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        setContext(root.getLoggerContext());
        root.detachAppender(this);
    }

    @Override
    protected void append(ILoggingEvent event) {
        final StackTraceElement stackTraceElement = event.getCallerData()[0];
        monitoringDataCollector.submitLogEvent(new Date(event.getTimeStamp()), event.getLevel().toString(),
                stackTraceElement.getClassName() + ":" + stackTraceElement.getLineNumber()
                , event.getFormattedMessage());
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
