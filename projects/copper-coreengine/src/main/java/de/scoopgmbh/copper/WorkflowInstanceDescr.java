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
	
	private final String wfClassname;
	private T data;
	private Integer priority;
	private String processorPoolId;
	private String id;
	
	public WorkflowInstanceDescr(String wfClassname) {
		this(wfClassname,null,null,null,null);
	}
	
	public WorkflowInstanceDescr(String wfClassname, T data) {
		this(wfClassname,data,null,null,null);
	}
	
	public WorkflowInstanceDescr(String wfClassname, T data, String id, Integer priority, String processorPoolId) {
		if (wfClassname == null) throw new IllegalArgumentException("wfClassname is null");
		this.wfClassname = wfClassname;
		this.data = data;
		this.priority = priority;
		this.processorPoolId = processorPoolId;
		this.id = id;
	}

	public String getWfClassname() {
		return wfClassname;
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
	
}
