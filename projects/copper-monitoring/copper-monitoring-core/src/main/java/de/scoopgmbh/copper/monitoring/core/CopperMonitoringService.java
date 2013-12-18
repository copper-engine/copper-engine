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
package de.scoopgmbh.copper.monitoring.core;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import de.scoopgmbh.copper.monitoring.core.data.MonitoringDataQuerys;
import de.scoopgmbh.copper.monitoring.core.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitoring.core.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.core.model.MessageInfo;
import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataProviderInfo;
import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataStorageInfo;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowClassMetaData;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceMetaData;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowSummary;

public interface CopperMonitoringService extends Remote, Serializable, MonitoringDataQuerys {

	public List<WorkflowSummary> getWorkflowSummary(final String poolid, final String classname) throws RemoteException;

	public List<WorkflowInstanceInfo> getWorkflowInstanceList(final String poolid, final String classname,
			final WorkflowInstanceState state, final Integer priority, Date from, Date to, long resultRowLimit) throws RemoteException;

	public List<AuditTrailInfo> getAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level, long resultRowLimit) throws RemoteException;

	public String getAuditTrailMessage(long id) throws RemoteException;
	
	public List<WorkflowClassMetaData> getWorkflowClassesList(final String engineId) throws RemoteException;
	
	public WorkflowInstanceMetaData getWorkflowInstanceDetails(String workflowInstanceId, String engineId)  throws RemoteException;

	public WorkflowStateSummary getAggregatedWorkflowStateSummary(String engineid) throws RemoteException;
	
	public CopperInterfaceSettings getSettings() throws RemoteException;

	/**
	 * Executes an sql query on the database of the copper runtime this monitor interface is connected to.
	 * @param query sql query
	 * @param resultRowLimit maximum number of rows to return
	 * @return the query result as a list of rows, each row is a String array.
	 * @throws RemoteException
	 */
	public List<String[]> executeSqlQuery(String query, long resultRowLimit) throws RemoteException;
	
	/**
	 * Trigger restart of a workflow instance that is in the error state.
	 */
	public void restartWorkflowInstance(String workflowInstanceId, String engineid) throws RemoteException;

	/**
	 * Trigger restart of all workflow instances that are in error state.
	 */
	public void restartAllErroneousInstances(String engineid) throws RemoteException;
	
	public void setNumberOfThreads(String engineid, String processorPoolId, int numberOfThreads) throws RemoteException;
	
	public void setThreadPriority(String engineid, String processorPoolId, int threadPriority) throws RemoteException;
	
	public List<ProcessingEngineInfo> getProccessingEngineList() throws RemoteException;
	
	public List<MeasurePointData> getMeasurePoints(String engineid) throws RemoteException;
	
	public void resetMeasurePoints() throws RemoteException;
	
	public void setBatcherNumThreads(int numThread, String engineid) throws RemoteException;
	
	public List<MessageInfo> getMessageList(final boolean ignoreProcessed, long resultRowLimit) throws RemoteException;
	
	public String getLogConfig() throws RemoteException;

	public void updateLogConfig(String config) throws RemoteException;
	
	public String getDatabaseMonitoringHtmlReport() throws RemoteException;
	
	public String getDatabaseMonitoringHtmlDetailReport(String sqlid) throws RemoteException;
	
	public String getDatabaseMonitoringRecommendationsReport(String sqlid) throws RemoteException;
	
	public List<MonitoringDataProviderInfo> getMonitoringDataProviderInfos() throws RemoteException;

	public void startMonitoringDataProvider(String name) throws RemoteException;

	public void stopMonitoringDataProvider(String name) throws RemoteException;
	
	public MonitoringDataStorageInfo getMonitroingDataStorageInfo() throws RemoteException;

}

