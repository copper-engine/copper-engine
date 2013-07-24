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
package de.scoopgmbh.copper.monitoring.server.logging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import de.scoopgmbh.copper.monitoring.server.provider.MonitoringLogbackDataProvider;

public class LogbackConfigManager implements LogConfigManager {
	
	private final MonitoringLogbackDataProvider dataProvider;

	private LoggerContext loggerContext;
	public LogbackConfigManager(MonitoringLogbackDataProvider dataProvider){
		loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		this.dataProvider = dataProvider;
	}
	
	private String config;
	@Override
	public void updateLogConfig(String config) {
		this.config=config;
		InputStream is = null;	
		try {
			is	= new ByteArrayInputStream(config.getBytes());
			dataProvider.removeFromRoot();
			reload(is);
			dataProvider.appendToRoot();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public String getLogConfig() {
		if (config!=null){
			return config;
		}
		ContextInitializer ci = new ContextInitializer(loggerContext);
		URL url = ci.findURLOfDefaultConfigurationFile(true);
		
		InputStream inputStream=null;
		try {
			if (url==null){
				return "no logback config found";
			}
			inputStream = url.openStream();
			return de.scoopgmbh.copper.monitoring.server.util.FileUtil.convertStreamToString(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (inputStream!=null){
					inputStream.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	//copy from JMXConfigurator to fix api
	private void reload(InputStream in) throws JoranException {
		
//		StatusListenerAsList statusListenerAsList = new StatusListenerAsList();
//
//		addStatusListener(statusListenerAsList);
//		addInfo("Resetting context: " + loggerContext.getName());
		loggerContext.reset();
		// after a reset the statusListenerAsList gets removed as a listener
//		addStatusListener(statusListenerAsList);

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			configurator.doConfigure(in);
//			addInfo("Context: " + loggerContext.getName() + " reloaded.");
		} finally {
//			removeStatusListener(statusListenerAsList);
//			if (debug) {
//				StatusPrinter.print(statusListenerAsList.getStatusList());
//			}
		}
		
	}

}
