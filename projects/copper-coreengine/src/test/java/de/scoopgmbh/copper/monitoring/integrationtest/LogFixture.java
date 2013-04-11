package de.scoopgmbh.copper.monitoring.integrationtest;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class LogFixture {
	
	public static interface LogContentAssertion{
		public void executeLogCreatingAction();
		public void assertLogContent(List<String> logContent);
	}
	
	public void assertLogContent(LogContentAssertion logContentAssertion){
		final ArrayList<String> log = new ArrayList<String>();
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
				log.add(event.getRenderedMessage());
			}
		};
		l.addAppender(appender);
		logContentAssertion.executeLogCreatingAction();
		logContentAssertion.assertLogContent(log);
		l.removeAppender(appender);
		
	}

}
