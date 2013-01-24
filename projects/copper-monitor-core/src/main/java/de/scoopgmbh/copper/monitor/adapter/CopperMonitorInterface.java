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
package de.scoopgmbh.copper.monitor.adapter;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitor.adapter.model.CopperStatusInfo;
import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassVersionInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceMetaDataInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummery;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummery;

public interface CopperMonitorInterface extends Remote, Serializable {

	public List<WorkflowSummery> getWorkflowSummery(ProcessingEngineInfo engine, WorkflowClassVersionInfo workflowClassDescription, long resultRowLimit) throws RemoteException;

	public List<WorkflowInstanceInfo> getWorkflowInstanceList(ProcessingEngineInfo engine, WorkflowClassVersionInfo workflowClassDescription, WorkflowInstanceState state, Integer priority, long resultRowLimit) throws RemoteException;

	public List<AuditTrailInfo> getAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level, long resultRowLimit) throws RemoteException;

	public String getAuditTrailMessage(long id) throws RemoteException;
	
	public List<WorkflowClassVersionInfo> getWorkflowClassesList() throws RemoteException;

	public CopperStatusInfo getCopperStatus() throws RemoteException;
	
	public WorkflowInstanceMetaDataInfo getWorkflowInstanceDetails(String workflowInstanceId)  throws RemoteException;

	public WorkflowStateSummery getAggregatedWorkflowStateSummery(ProcessingEngineInfo engine) throws RemoteException;
	
	public CopperInterfaceSettings getSettings() throws RemoteException;

	public List<String[]> executeSqlQuery(String query, long resultRowLimit) throws RemoteException;
	
	public SystemResourcesInfo getSystemResourceInfo() throws RemoteException;

	/**
	 * Trigger restart a workflow instance that is in the error state.
	 * @param workflowInstanceId
	 */
	public void restart(String workflowInstanceId, ProcessingEngineInfo engine) throws RemoteException;

	/**
	 * Trigger restart all workflow instances that are in error state.
	 */
	public void restartAll(ProcessingEngineInfo engine) throws RemoteException;
	
	public List<ProcessingEngineInfo> getProccessingEngineList() throws RemoteException;
	
	public List<MeasurePointData> getMeasurePoints(ProcessingEngineInfo engine) throws RemoteException;
	
	
	
}

