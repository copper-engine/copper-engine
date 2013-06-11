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
package de.scoopgmbh.copper.monitoring.client.adapter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.SimpleLongProperty;
import de.scoopgmbh.copper.monitoring.client.form.enginefilter.EngineFilterModelBase;
import de.scoopgmbh.copper.monitoring.client.form.enginefilter.EnginePoolModel;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.fiter.AdapterMonitoringFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.AdapterMonitoringResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.result.AuditTrailResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.filter.CustomMeasurePointFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.message.filter.MessageFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.message.result.MessageResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.sql.filter.SqlFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.sql.result.SqlResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.result.WorkflowInstanceResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowsummary.filter.WorkflowSummaryFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowsummary.result.WorkflowSummaryResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultModel;
import de.scoopgmbh.copper.monitoring.client.util.WorkflowVersion;
import de.scoopgmbh.copper.monitoring.core.CopperMonitoringService;
import de.scoopgmbh.copper.monitoring.core.model.AdapterHistoryInfo;
import de.scoopgmbh.copper.monitoring.core.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitoring.core.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.core.model.MessageInfo;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowClassVersionInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowSummary;

public class GuiCopperDataProvider {
	
	private final CopperMonitoringService copperDataProvider;
	
	public GuiCopperDataProvider(final CopperMonitoringService copperDataProvider) {
		super();
		this.copperDataProvider=copperDataProvider;
	}

	public List<WorkflowInstanceResultModel> getWorkflowInstanceList(WorkflowInstanceFilterModel filter, int maxResultCount){
		List<WorkflowInstanceInfo> list;
		try {
			list = copperDataProvider.getWorkflowInstanceList(getPoolId(filter.enginePoolModel),filter.version.classname.get(),
					filter.state.get(), getFilterReadyInteger(filter.priority.get()),filter.from.get(),filter.to.get(), maxResultCount);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowInstanceResultModel> result = new ArrayList<WorkflowInstanceResultModel>();
		for (WorkflowInstanceInfo workflowInstanceInfo: list){
			result.add(new WorkflowInstanceResultModel(workflowInstanceInfo));
		}
		return result;
	}
	
	private Integer getFilterReadyInteger(String string){
		try {
			return string==null?null:Integer.valueOf(string);
		} catch (NumberFormatException e){
			return null;
		}
	}

	public String getPoolId(EnginePoolModel enginePoolModel) {
		return enginePoolModel.selectedPool.get().getId();
	}
	
	public List<AuditTrailResultModel> getAuditTrails(de.scoopgmbh.copper.monitoring.client.ui.audittrail.filter.AuditTrailFilterModel  filter, int maxResultCount){
		
		List<AuditTrailInfo> list;
		try {
			list = copperDataProvider.getAuditTrails(filter.workflowClass.getValue(), filter.workflowInstanceId.getValue(), filter.correlationId.getValue(), getFilterReadyInteger(filter.level.getValue()), maxResultCount);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<AuditTrailResultModel> result = new ArrayList<AuditTrailResultModel>();
		for (AuditTrailInfo auditTrailInfo: list){
			result.add(new AuditTrailResultModel(auditTrailInfo));
		}
		return result;
	}

	public List<WorkflowSummaryResultModel> getWorkflowSummery(WorkflowSummaryFilterModel filter) {
		List<WorkflowSummary> summeries;
		try {
			summeries = copperDataProvider.getWorkflowSummary(getPoolId(filter.enginePoolModel), filter.version.classname.get());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowSummaryResultModel> result = new ArrayList<WorkflowSummaryResultModel>();
		for (WorkflowSummary workflowSummery: summeries){
			result.add(new WorkflowSummaryResultModel(workflowSummery));
		}
		return result;
	}
	
	public List<WorkflowVersion> getWorkflowClassesList(final String engineId){
		List<WorkflowClassVersionInfo> list;
		try {
			list = copperDataProvider.getWorkflowClassesList(engineId);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowVersion> result = new ArrayList<WorkflowVersion>();
		for (WorkflowClassVersionInfo workflowClassesInfo: list){
			result.add(new WorkflowVersion(workflowClassesInfo));
		}
		return result;
	}

	public WorkflowInstanceDetailResultModel getWorkflowDetails(WorkflowInstanceDetailFilterModel filter ) {
		try {
			return new WorkflowInstanceDetailResultModel(copperDataProvider.getWorkflowInstanceDetails(filter.workflowInstanceId.getValue()));
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public WorkflowStateSummary getCopperLoadInfo(ProcessingEngineInfo engine) {
		try {
			return  copperDataProvider.getAggregatedWorkflowStateSummary(engine.getId());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public String getAuditTrailMessage(SimpleLongProperty id) {
		try {
			return  copperDataProvider.getAuditTrailMessage(id.getValue());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<SqlResultModel> executeSqlQuery(SqlFilterModel filter, int maxResultCount){
		List<String[]> list;
		try {
			list = copperDataProvider.executeSqlQuery(filter.sqlQuery.getValue(), maxResultCount);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<SqlResultModel> result = new ArrayList<SqlResultModel>();
		for (String[] row: list){
			result.add(new SqlResultModel(row));
		}
		return result;
	}

	public SystemResourcesInfo getSystemRessources() {
		try {
			return copperDataProvider.getSystemResourceInfo();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<ProcessingEngineInfo> getEngineList() {
		try {
			return copperDataProvider.getProccessingEngineList();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<MeasurePointData> getMeasurePoints(EngineFilterModelBase engineFilter) {
		try {
			return copperDataProvider.getMeasurePoints(engineFilter.enginePoolModel.selectedEngine.get().getId());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setNumberOfThreads(String engineid, String processorPoolId, int numberOfThreads){
		try {
			copperDataProvider.setNumberOfThreads(engineid, processorPoolId, numberOfThreads);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public void setThreadPriority(String engineid, String processorPoolId, int threadPriority){
		try {
			copperDataProvider.setThreadPriority(engineid, processorPoolId, threadPriority);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}		
	}

	public void resetMeasurePoints() {
		try {
			copperDataProvider.resetMeasurePoints();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}	
	}

	public void setBatcherNumThreads(String id, int numThread) {
		try {
			copperDataProvider.setBatcherNumThreads(numThread, id);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}	
	}

	public List<MessageResultModel> getMessageList(MessageFilterModel filter, final int maxResultCount) {
		List<MessageInfo> list;
		try {
			list = copperDataProvider.getMessageList(filter.ignoreProcessed.get(), maxResultCount);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<MessageResultModel> result = new ArrayList<MessageResultModel>();
		for (MessageInfo message: list){
			result.add(new MessageResultModel(message)); 
		}
		return result;
	}

	public void restartAllError(String engineid) {
		try {
			copperDataProvider.restartAllErroneousInstances(engineid);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public CopperInterfaceSettings getInterfaceSettings() {
		try {
			return copperDataProvider.getSettings();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public AdapterMonitoringResultModel getAdapterHistoryInfo(AdapterMonitoringFilterModel filter) {
		try {
			AdapterHistoryInfo  adapterHistory = copperDataProvider.getAdapterHistoryInfos(filter.adapterId.get());
			Collections.reverse(adapterHistory.getAdapterCalls());
			return new AdapterMonitoringResultModel(adapterHistory);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void restartInstance(final String workflowInstanceId, String engineid) {
		try {
			copperDataProvider.restartWorkflowInstance(workflowInstanceId, engineid);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public List<MeasurePointData> getMonitoringMeasurePoints(CustomMeasurePointFilterModel filter, long limit) {
		try {
			return copperDataProvider.getMonitoringMeasurePoints(filter.measurePointId.get(),limit);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<String> getMonitoringMeasurePointIds() {
		try {
			return copperDataProvider.getMonitoringMeasurePointIds();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
}
