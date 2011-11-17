/*
 * Copyright 2002-2011 SCOOP Software GmbH
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
package de.scoopgmbh.copper.management;

import java.beans.ConstructorProperties;
import java.util.Date;

public class WorkflowInfo {
	
	private String id;
	private String state;
	private int priority;
	private String processorPoolId;
	private Date timeout;
	
	public WorkflowInfo() {
	}
	
	@ConstructorProperties({"id", "state", "priority","processorPoolId","timeout"}) 
	public WorkflowInfo(String id, String state, int priority, String processorPoolId, Date timeout) {
		super();
		this.id = id;
		this.state = state;
		this.priority = priority;
		this.processorPoolId = processorPoolId;
		this.timeout = timeout;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getProcessorPoolId() {
		return processorPoolId;
	}

	public void setProcessorPoolId(String processorPoolId) {
		this.processorPoolId = processorPoolId;
	}

	public Date getTimeout() {
		return timeout;
	}

	public void setTimeout(Date timeout) {
		this.timeout = timeout;
	}
	
	
}
