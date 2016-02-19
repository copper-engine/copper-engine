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
package org.copperengine.monitoring.server.logging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.copperengine.monitoring.server.provider.MonitoringLogbackDataProvider;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;

public class LogbackConfigManager implements LogConfigManager {

    private final MonitoringLogbackDataProvider dataProvider;
    private final LogbackConfigLocationLocator logbackConfigLocationLocator;

    private LoggerContext loggerContext;

    public LogbackConfigManager(MonitoringLogbackDataProvider dataProvider, LogbackConfigLocationLocator logbackConfigLocationLocator) {
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        this.dataProvider = dataProvider;
        this.logbackConfigLocationLocator = logbackConfigLocationLocator;
    }

    public LogbackConfigManager(MonitoringLogbackDataProvider dataProvider) {
        this(dataProvider, new DefaultLogbackConfigLocationLocator());
    }

    private String config;

    @Override
    public void updateLogConfig(String config) {
        this.config = config;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(config.getBytes());
            dataProvider.removeFromRootLogger();
            reload(is);
            dataProvider.addToRootLogger();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (is!=null){
                    is.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getLogConfig() {
        if (config != null) {
            return config;
        }

        InputStream inputStream = logbackConfigLocationLocator.getLogbackConfigLocation();
        ;
        try {
            if (inputStream == null) {
                return "no logback config found";
            }
            return org.copperengine.monitoring.server.util.FileUtil.convertStreamToString(inputStream);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // copy from JMXConfigurator to fix api
    private void reload(InputStream in) throws JoranException {

        // StatusListenerAsList statusListenerAsList = new StatusListenerAsList();
        //
        // addStatusListener(statusListenerAsList);
        // addInfo("Resetting context: " + loggerContext.getName());
        loggerContext.reset();
        // after a reset the statusListenerAsList gets removed as a listener
        // addStatusListener(statusListenerAsList);

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            configurator.doConfigure(in);
            // addInfo("Context: " + loggerContext.getName() + " reloaded.");
        } finally {
            // removeStatusListener(statusListenerAsList);
            // if (debug) {
            // StatusPrinter.print(statusListenerAsList.getStatusList());
            // }
        }

    }

    public static interface LogbackConfigLocationLocator {
        public InputStream getLogbackConfigLocation();
    }

    public static class DefaultLogbackConfigLocationLocator implements LogbackConfigLocationLocator {
        @Override
        public InputStream getLogbackConfigLocation() {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ContextInitializer ci = new ContextInitializer(loggerContext);
            try {
                return ci.findURLOfDefaultConfigurationFile(true).openStream();
            } catch (Exception e) {
                return null;
            }
        }
    }

}
