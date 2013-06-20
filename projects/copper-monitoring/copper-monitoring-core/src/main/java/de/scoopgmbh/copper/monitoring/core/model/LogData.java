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
package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LogData implements Serializable{
	private static final long serialVersionUID = -7676407159766000087L;

	List<LogEvent> logEvents = new ArrayList<LogEvent>();
	
	String logConfig;

	public List<LogEvent> getLogEvents() {
		return logEvents;
	}

	public void setLogEvents(List<LogEvent> logEvents) {
		this.logEvents = logEvents;
	}

	public String getLogConfig() {
		return logConfig;
	}

	public void setLogConfig(String logConfig) {
		this.logConfig = logConfig;
	}

	public LogData(List<LogEvent> logEvents, String logConfig) {
		super();
		this.logEvents = logEvents;
		this.logConfig = logConfig;
	}

	public LogData() {
		super();
	}
	
	
	

}
