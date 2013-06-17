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
import java.util.Date;

public class WorkflowInstanceInfo implements Serializable{
	private static final long serialVersionUID = 3115987442310287971L;
	
	private String id;
	private WorkflowInstanceState state;
	private int priority;
	private String processorPoolId;
	private Date timeout;
	
	private Date lastActivityTimestamp;
    private long overallLifetimeInMs;
    private Date startTime;
    private Date finishTime;
    private Date lastErrorTime;
    private String errorInfos;
    private String classname;
	
	public WorkflowInstanceInfo() {
	}
	
	public WorkflowInstanceInfo(String id, WorkflowInstanceState state, int priority, String processorPoolId, Date timeout,
			Date lastActivityTimestamp, long overallLifetimeInMs, Date startTime, Date finishTime, Date lastErrorTime, String errorInfos, String classname) {
		super();
		this.id = id;
		this.state = state;
		this.priority = priority;
		this.processorPoolId = processorPoolId;
		this.timeout = timeout;
		this.lastActivityTimestamp = lastActivityTimestamp;
		this.overallLifetimeInMs = overallLifetimeInMs;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.lastErrorTime = lastErrorTime;
		this.errorInfos = errorInfos;
		this.classname = classname;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public WorkflowInstanceState getState() {
		return state;
	}

	public void setState(WorkflowInstanceState state) {
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

	@Override
	public String toString() {
		return "WorkflowInfo [id=" + id + ", state=" + state + ", priority="
				+ priority + ", processorPoolId=" + processorPoolId
				+ ", timeout=" + timeout + "]";
	}

	public Date getLastActivityTimestamp() {
		return lastActivityTimestamp;
	}

	public void setLastActivityTimestamp(Date lastActivityTimestamp) {
		this.lastActivityTimestamp = lastActivityTimestamp;
	}

	public long getOverallLifetimeInMs() {
		return overallLifetimeInMs;
	}

	public void setOverallLifetimeInMs(long overallLifetimeInMs) {
		this.overallLifetimeInMs = overallLifetimeInMs;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public Date getLastErrorTime() {
		return lastErrorTime;
	}

	public void setLastErrorTime(Date lastErrorTime) {
		this.lastErrorTime = lastErrorTime;
	}

	public String getErrorInfos() {
		return errorInfos;
	}

	public void setErrorInfos(String errorInfos) {
		this.errorInfos = errorInfos;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}
	
	
	
}
