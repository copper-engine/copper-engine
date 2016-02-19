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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.copperengine.monitoring.server.provider.MonitoringLogbackDataProvider;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class LogbackConfigManagerTest {

    @Test
    public void test_getConfig() {
        LogbackConfigManager logbackConfigManager = new LogbackConfigManager(Mockito.mock(MonitoringLogbackDataProvider.class));
        assertNotNull(logbackConfigManager.getLogConfig());
    }

    @Test
    public void test_updateConfig() {
        LogbackConfigManager logbackConfigManager = new LogbackConfigManager(Mockito.mock(MonitoringLogbackDataProvider.class));
        Logger root1 = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root1.setLevel(Level.INFO);
        assertEquals(Level.INFO, root1.getLevel());
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
        Logger root2 = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        assertEquals(Level.DEBUG, root2.getLevel());
    }

    @Test
    public void test_update_and_get_config() {
        LogbackConfigManager logbackConfigManager = new LogbackConfigManager(Mockito.mock(MonitoringLogbackDataProvider.class));
        final String config = "<configuration scan=\"true\" scanPeriod=\"15 seconds\">\r\n" +
                "  <appender name=\"CONSOLE\" class=\"ch.qos.logback.core.ConsoleAppender\">\r\n" +
                "     <layout class=\"ch.qos.logback.classic.PatternLayout\">\r\n" +
                "      <Pattern>%d{HH:mm:ss.SSS} [%thread] %.-1level %logger{36} - %msg%n</Pattern>\r\n" +
                "    </layout>\r\n" +
                "  </appender>\r\n" +
                " \r\n" +
                "  <root level=\"DEBUG\">\r\n" +
                "    <appender-ref ref=\"CONSOLE\" />\r\n" +
                "  </root>\r\n" +
                "</configuration>";
        logbackConfigManager.updateLogConfig(
                config);

        assertEquals(config, logbackConfigManager.getLogConfig());
    }
}
