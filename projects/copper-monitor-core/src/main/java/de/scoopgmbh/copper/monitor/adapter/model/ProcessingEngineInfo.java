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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessingEngineInfo implements Serializable {
	private static final long serialVersionUID = -4083220310307902745L;
	
	private EngineTyp typ;
	private String id;
	private List<ProcessorPoolInfo> pools = new ArrayList<>();
	private WorkflowRepositoryInfo repositoryInfo;
	private DependencyInjectorInfo dependencyInjectorInfo;
	private StorageInfo storageInfo;
	
	public static enum EngineTyp{
		TRANSIENT, PERSISTENT
	}

	public ProcessingEngineInfo() {
	}

	public ProcessingEngineInfo(EngineTyp typ, String id, WorkflowRepositoryInfo repositoryInfo,
			DependencyInjectorInfo dependencyInjectorInfo, StorageInfo storageInfo, ProcessorPoolInfo... pools) {
		super();
		this.typ = typ;
		this.id = id;
		this.repositoryInfo = repositoryInfo;
		this. dependencyInjectorInfo =  dependencyInjectorInfo;
		this.pools.addAll(Arrays.asList(pools));
		this.storageInfo = storageInfo;
	}

	
	
	public StorageInfo getStorageInfo() {
		return storageInfo;
	}

	public void setStorageInfo(StorageInfo storageInfo) {
		this.storageInfo = storageInfo;
	}

	public DependencyInjectorInfo getDependencyInjectorInfo() {
		return dependencyInjectorInfo;
	}

	public void setDependencyInjectorInfo(DependencyInjectorInfo dependencyInjectorInfo) {
		this.dependencyInjectorInfo = dependencyInjectorInfo;
	}

	public WorkflowRepositoryInfo getRepositoryInfo() {
		return repositoryInfo;
	}

	public void setRepositoryInfo(WorkflowRepositoryInfo repositoryInfo) {
		this.repositoryInfo = repositoryInfo;
	}

	public EngineTyp getTyp() {
		return typ;
	}

	public void setTyp(EngineTyp typ) {
		this.typ = typ;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ProcessorPoolInfo> getPools() {
		return pools;
	}

	public void setPools(List<ProcessorPoolInfo> pools) {
		this.pools = pools;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("ProcessingEngine: "+id+", ");
		result.append("Typ: "+typ+", ");
		result.append("Pools:\n");
		for (ProcessorPoolInfo pool: pools){
			result.append(pool.toString());
		}
		return result.toString();
	}
	
	
}
