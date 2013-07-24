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
package de.scoopgmbh.copper.monitoring.server.provider;

import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.monitoring.server.logging.LogbackConfigManager;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;


public class MonitoringLogbackDataProviderTest {

	@Test
	public void test(){
		MonitoringDataCollector mock = Mockito.mock(MonitoringDataCollector.class);
		new MonitoringLogbackDataProvider(mock);
		org.slf4j.Logger logger = LoggerFactory.getLogger(MonitoringLogbackDataProviderTest.class);
	    logger.info("Hello world.");
	    Mockito.verify(mock).submitLogEvent(Mockito.any(Date.class), Mockito.eq("INFO"), Mockito.anyString(), Mockito.eq("Hello world."));
	};
	
	@Test
	public void test_after_config_update(){
		MonitoringDataCollector mock = Mockito.mock(MonitoringDataCollector.class);
		MonitoringLogbackDataProvider monitoringLogbackDataProvider = new MonitoringLogbackDataProvider(mock);
		org.slf4j.Logger logger = LoggerFactory.getLogger(MonitoringLogbackDataProviderTest.class);
	    logger.info("Hello world.");
	    Mockito.verify(mock).submitLogEvent(Mockito.any(Date.class), Mockito.eq("INFO"), Mockito.anyString(), Mockito.eq("Hello world."));
	    
	    LogbackConfigManager logbackConfigManager = new LogbackConfigManager(monitoringLogbackDataProvider);
	    logbackConfigManager.updateLogConfig(
	    		"<configuration scan=\"true\" scanPeriod=\"15 seconds\">\r\n" + 
				"  <appender name=\"CONSOLE\" class=\"ch.qos.logback.core.ConsoleAppender\">\r\n" + 
				"     <layout class=\"ch.qos.logback.classic.PatternLayout\">\r\n" + 
				"      <Pattern>%d{HH:mm:ss.SSS} [%thread] %.-1level %logger{36} - %msg%n</Pattern>\r\n" + 
				"    </layout>\r\n" + 
				"  </appender>\r\n" + 
				" \r\n" + 
				"  <root level=\"DEBUG\">\r\n" + 
				"    <appender-ref ref=\"CONSOLE\" />\r\n" + 
				"  </root>\r\n" + 
				"</configuration>");
	    
	    logger.debug("Hello world.");
	    Mockito.verify(mock).submitLogEvent(Mockito.any(Date.class), Mockito.eq("DEBUG"), Mockito.anyString(), Mockito.eq("Hello world."));
	    
	    logger.trace("Hello world.");
	    Mockito.verifyNoMoreInteractions(mock);
	};
}
