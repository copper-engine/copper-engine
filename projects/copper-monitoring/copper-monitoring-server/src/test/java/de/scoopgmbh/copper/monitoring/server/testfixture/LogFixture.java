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
package de.scoopgmbh.copper.monitoring.server.testfixture;

import java.util.ArrayList;
import java.util.List;


import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;

public class LogFixture {
	
	public static interface LogContentAssertion{
		public void executeLogCreatingAction();
		public void assertLogContent(List<MessageAndLogLevel> logContent);
	}
	
	public static abstract class NoErrorLogContentAssertion implements LogContentAssertion{
		@Override
		public void assertLogContent(List<MessageAndLogLevel> logContent){
			for (MessageAndLogLevel messageAndLogLevel: logContent){
				if (messageAndLogLevel.loglevel.equals(Level.ERROR)){
					Assert.fail("Log contains error message:"+messageAndLogLevel.message);
				}
			}
		}
	}
	
	public static class MessageAndLogLevel{
		public String message;
		public Level loglevel;
		public MessageAndLogLevel(String message, Level loglevel) {
			super();
			this.message = message;
			this.loglevel = loglevel;
		}
	}
	
	
	public void assertLogContent(LogContentAssertion logContentAssertion){
		final ArrayList<MessageAndLogLevel> log = new ArrayList<MessageAndLogLevel>();
		Logger l = Logger.getRootLogger();
		AppenderSkeleton appender = new AppenderSkeleton() {
			@Override
			public void close() {
			}

			@Override
			public boolean requiresLayout() {
				return false;
			}

			@Override
			protected void append(LoggingEvent event) {
				log.add(new MessageAndLogLevel(event.getRenderedMessage(),event.getLevel()));
			}
		};
		l.addAppender(appender);
		logContentAssertion.executeLogCreatingAction();
		logContentAssertion.assertLogContent(log);
		l.removeAppender(appender);
	}
	
	public void assertNoError(NoErrorLogContentAssertion logContentAssertion){
		assertLogContent(logContentAssertion);
	}

}
