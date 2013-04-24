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
import java.util.List;

public class WorkflowRepositoryInfo implements Serializable{
	private static final long serialVersionUID = 5658665409495683627L;
	
	public static enum WorkflowRepositorTyp{
		FILE, UNKOWN
	}
	
	WorkflowRepositorTyp workflowRepositorTyp;
	String name;
	List<String> srcPaths;
	
	
	public WorkflowRepositoryInfo() {
		super();
	}

	public WorkflowRepositoryInfo(WorkflowRepositorTyp workflowRepositorTyp, String name, List<String> srcPaths) {
		super();
		this.workflowRepositorTyp = workflowRepositorTyp;
		this.name = name;
		this.srcPaths = srcPaths;
	}
	
	public WorkflowRepositorTyp getWorkflowRepositorTyp() {
		return workflowRepositorTyp;
	}

	public void setWorkflowRepositorTyp(WorkflowRepositorTyp workflowRepositorTyp) {
		this.workflowRepositorTyp = workflowRepositorTyp;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getSrcPaths() {
		return srcPaths;
	}
	public void setSrcPaths(List<String> srcPaths) {
		this.srcPaths = srcPaths;
	}

}
