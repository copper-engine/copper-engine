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
package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;

public class WorkflowSummery implements Serializable {
	private static final long serialVersionUID = 4867510351238162279L;
	
	private String workflowClass;
	private String alias;
	private long workflowMajorVersion;
	private long workflowMinorVersion;
	private long workflowPatchLevel;
	private String status;
	private int count;
	
	public WorkflowSummery() {
		super();
	}
	
	public long getWorkflowPatchLevel() {
		return workflowPatchLevel;
	}

	public void setWorkflowPatchLevel(long workflowPatchLevel) {
		this.workflowPatchLevel = workflowPatchLevel;
	}


	public WorkflowSummery(String workflowClass, String alias, long workflowMajorVersion, long workflowMinorVersion, long workflowPatchLevel,
			String status, int count) {
		super();
		this.workflowClass = workflowClass;
		this.alias = alias;
		this.workflowMajorVersion = workflowMajorVersion;
		this.workflowMinorVersion = workflowMinorVersion;
		this.workflowPatchLevel = workflowPatchLevel;
		this.status = status;
		this.count = count;
	}


	public String getWorkflowClass() {
		return workflowClass;
	}

	public void setWorkflowClass(String workflowClass) {
		this.workflowClass = workflowClass;
	}

	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	public long getWorkflowMajorVersion() {
		return workflowMajorVersion;
	}

	public void setWorkflowMajorVersion(long workflowMajorVersion) {
		this.workflowMajorVersion = workflowMajorVersion;
	}

	public long getWorkflowMinorVersion() {
		return workflowMinorVersion;
	}

	public void setWorkflowMinorVersion(long workflowMinorVersion) {
		this.workflowMinorVersion = workflowMinorVersion;
	}
	
	
	
	
}
