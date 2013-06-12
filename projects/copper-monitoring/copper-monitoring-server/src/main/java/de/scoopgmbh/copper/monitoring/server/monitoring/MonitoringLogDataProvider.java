package de.scoopgmbh.copper.monitoring.server.monitoring;

import java.util.Date;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class MonitoringLogDataProvider extends AppenderSkeleton{
	
	public static final String APPENDER_NAME="MonitoringLogDataProviderAppender";
	
	MonitoringDataCollector monitoringDataCollector;
	
	
	public MonitoringLogDataProvider(MonitoringDataCollector monitoringDataCollector) {
		super();
		this.monitoringDataCollector = monitoringDataCollector;
		
		this.setName(APPENDER_NAME);
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.addAppender(this);
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
		monitoringDataCollector.submitLogEvent(new Date(event.getTimeStamp()),event.getLevel().toString(),event.getLocationInformation().fullInfo,event.getRenderedMessage());
	}

}
