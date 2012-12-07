/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper;

public class WorkflowInstanceDescr<T> {
	
	private final String wfName;
	private T data;
	private Integer priority;
	private String processorPoolId;
	private String id;
	private WorkflowVersion workflowVersion;
	
	public WorkflowInstanceDescr(String wfName) {
		this(wfName,null,null,null,null);
	}
	
	public WorkflowInstanceDescr(String wfName, T data) {
		this(wfName,data,null,null,null);
	}
	
	public WorkflowInstanceDescr(String wfName, T data, String id, Integer priority, String processorPoolId) {
		if (wfName == null) throw new IllegalArgumentException("wfName is null");
		this.wfName = wfName;
		this.data = data;
		this.priority = priority;
		this.processorPoolId = processorPoolId;
		this.id = id;
	}

	public String getWfName() {
		return wfName;
	}

	public T getData() {
		return data;
	}

	public Integer getPriority() {
		return priority;
	}

	public String getProcessorPoolId() {
		return processorPoolId;
	}
	
	public String getId() {
		return id;
	}

	public void setData(T data) {
		this.data = data;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public void setProcessorPoolId(String processorPoolId) {
		this.processorPoolId = processorPoolId;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public WorkflowVersion getVersion() {
		return workflowVersion;
	}
	
	public void setVersion(WorkflowVersion workflowVersion) {
		this.workflowVersion = workflowVersion;
	}
}
