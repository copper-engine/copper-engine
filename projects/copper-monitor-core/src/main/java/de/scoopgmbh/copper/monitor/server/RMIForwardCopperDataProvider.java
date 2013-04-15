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
package de.scoopgmbh.copper.monitor.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import de.scoopgmbh.copper.monitor.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassVersionInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceHistory;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceMetaDataInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowRepositoryInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummary;

public class RMIForwardCopperDataProvider extends UnicastRemoteObject implements CopperMonitorInterface {
	private static final long serialVersionUID = -5757718583261293846L;
	
	private CopperMonitorInterface copperMonitorInterface;
	
	public RMIForwardCopperDataProvider(String serverAdress) throws RemoteException {
		super();
		
		try {
			Registry registry = LocateRegistry.getRegistry(serverAdress,Registry.REGISTRY_PORT);
			copperMonitorInterface =  (CopperMonitorInterface)registry.lookup(CopperMonitorInterface.class.getSimpleName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public String getAuditTrailMessage(long id) throws RemoteException {
		return copperMonitorInterface.getAuditTrailMessage(id);
	}

	@Override
	public List<WorkflowSummary> getWorkflowSummary(String poolid, String classname) throws RemoteException {	
		return copperMonitorInterface.getWorkflowSummary(poolid,classname);
	}

	@Override
	public List<WorkflowInstanceInfo> getWorkflowInstanceList(String poolid, String classname,
			WorkflowInstanceState state, Integer priority, long resultRowLimit) throws RemoteException {
		return copperMonitorInterface.getWorkflowInstanceList(poolid,classname,state,priority,resultRowLimit);
	}

	@Override
	public List<AuditTrailInfo> getAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level, long resultRowLimit)
			throws RemoteException {
		return copperMonitorInterface.getAuditTrails(workflowClass,workflowInstanceId,correlationId,level,resultRowLimit);
	}

	@Override
	public List<WorkflowClassVersionInfo> getWorkflowClassesList(final String engineId) throws RemoteException {
		return copperMonitorInterface.getWorkflowClassesList(engineId);
	}

	@Override
	public WorkflowInstanceMetaDataInfo getWorkflowInstanceDetails(String workflowInstanceId) throws RemoteException  {
		return copperMonitorInterface.getWorkflowInstanceDetails(workflowInstanceId);
	}

	@Override
	public CopperInterfaceSettings getSettings() throws RemoteException {
		return copperMonitorInterface.getSettings();
	}

	@Override
	public List<String[]> executeSqlQuery(String query, long resultRowLimit) throws RemoteException  {
		return copperMonitorInterface.executeSqlQuery(query, resultRowLimit);
	}
	
	@Override
	public SystemResourcesInfo getSystemResourceInfo() throws RemoteException {
		return copperMonitorInterface.getSystemResourceInfo();
	}

	@Override
	public WorkflowStateSummary getAggregatedWorkflowStateSummary(String engineid) throws RemoteException {
		return copperMonitorInterface.getAggregatedWorkflowStateSummary(engineid);
	}

	@Override
	public void restartErroneousInstance(String workflowInstanceId, String engineid) throws RemoteException {
		copperMonitorInterface.restartErroneousInstance(workflowInstanceId,engineid);
	}

	@Override
	public void restartAllErroneousInstances(String engineid) throws RemoteException {
		copperMonitorInterface.restartAllErroneousInstances(engineid);
	}

	@Override
	public List<ProcessingEngineInfo> getProccessingEngineList() throws RemoteException {
		return copperMonitorInterface.getProccessingEngineList();
	}


	@Override
	public List<MeasurePointData> getMeasurePoints(String engineid) throws RemoteException {
		return copperMonitorInterface.getMeasurePoints(engineid);
	}


	@Override
	public void setNumberOfThreads(String engineid, String processorPoolId, int numberOfThreads) throws RemoteException  {
		copperMonitorInterface.setNumberOfThreads(engineid, processorPoolId, numberOfThreads);
	}


	@Override
	public void setThreadPriority(String engineid, String processorPoolId, int threadPriority) throws RemoteException  {
		copperMonitorInterface.setThreadPriority(engineid, processorPoolId, threadPriority);
	}


	@Override
	public void resetMeasurePoints() throws RemoteException {
		copperMonitorInterface.resetMeasurePoints();
	}


	@Override
	public WorkflowRepositoryInfo getWorkflowRepositoryInfo(String engineid) throws RemoteException {
		return copperMonitorInterface.getWorkflowRepositoryInfo(engineid);
	}


	@Override
	public void setBatcherNumThreads(int numThread, String engineid) throws RemoteException {
		copperMonitorInterface.setBatcherNumThreads(numThread,engineid);
	}


	@Override
	public List<WorkflowInstanceHistory> getWorkflowInstanceHistory() throws RemoteException {
		return copperMonitorInterface.getWorkflowInstanceHistory();
	}
	
}
