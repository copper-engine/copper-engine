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
package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;

public class WorkflowInstanceHistory implements Serializable{
	private static final long serialVersionUID = -7316596553420665952L;
	
	long timestamp;
	String stateName;
	String instanceId;
	String classname;
	
	public WorkflowInstanceHistory(long timestamp, String stateName, String instanceId, String classname) {
		super();
		this.timestamp = timestamp;
		this.stateName = stateName;
		this.instanceId = instanceId;
		this.classname = classname;
	}
	
	public WorkflowInstanceHistory() {
		super();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
	
	
}
