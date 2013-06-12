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
package de.scoopgmbh.copper.monitoring.client.ui.logs.result;

import java.util.Date;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import de.scoopgmbh.copper.monitoring.core.model.LogData;
import de.scoopgmbh.copper.monitoring.core.model.LogEvent;

public class LogsResultModel {
	
	public static class LogsRowModel{
		public final SimpleObjectProperty<Date> time;
		public final SimpleStringProperty message;
		public final SimpleStringProperty level;
		
		public LogsRowModel(LogEvent logEvent) {
			time = new SimpleObjectProperty<Date>(logEvent.getTime());
			message = new SimpleStringProperty(logEvent.getMessage());
			level = new SimpleStringProperty(logEvent.getLevel());
		}
	}
	
	public final SimpleStringProperty config;
	public final ObservableList<LogsRowModel> logs = FXCollections.observableArrayList();
	
	
	public LogsResultModel(LogData logData) {
		super();
		this.config = new SimpleStringProperty(logData.getLogConfig());
		for (LogEvent logEvent: logData.getLogEvents()){
			logs.add(new LogsRowModel(logEvent));
		}
	}
	
	
	
}
