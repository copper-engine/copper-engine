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
package org.copperengine.monitoring.server.testfixture;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class LogbackFixture {

    public static interface LogContentAssertion {
        public void executeLogCreatingAction();

        public void assertLogContent(List<MessageAndLogLevel> logContent);
    }

    public static abstract class NoErrorLogContentAssertion implements LogContentAssertion {
        @Override
        public void assertLogContent(List<MessageAndLogLevel> logContent) {
            for (MessageAndLogLevel messageAndLogLevel : logContent) {
                if (messageAndLogLevel.loglevel.equals(Level.ERROR)) {
                    Assert.fail("Log contains error message:" + messageAndLogLevel.message);
                }
            }
        }
    }

    public static class MessageAndLogLevel {
        public String message;
        public Level loglevel;

        public MessageAndLogLevel(String message, Level loglevel) {
            super();
            this.message = message;
            this.loglevel = loglevel;
        }
    }

    public void assertLogContent(LogContentAssertion logContentAssertion) {
        final ArrayList<MessageAndLogLevel> log = new ArrayList<MessageAndLogLevel>();
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        AppenderBase<ILoggingEvent> appender = new AppenderBase<ILoggingEvent>() {

            @Override
            protected void append(ILoggingEvent event) {
                log.add(new MessageAndLogLevel(event.getFormattedMessage(), event.getLevel()));
            }
        };
        appender.start();
        root.addAppender(appender);
        logContentAssertion.executeLogCreatingAction();
        logContentAssertion.assertLogContent(log);
        root.detachAppender(appender);
    }

    public void assertNoError(NoErrorLogContentAssertion logContentAssertion) {
        assertLogContent(logContentAssertion);
    }

}
