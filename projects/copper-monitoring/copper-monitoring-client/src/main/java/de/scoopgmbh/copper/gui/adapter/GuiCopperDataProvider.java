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
package de.scoopgmbh.copper.gui.adapter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import de.scoopgmbh.copper.gui.form.enginefilter.EngineFilterModelBase;
import de.scoopgmbh.copper.gui.form.enginefilter.EnginePoolModel;
import de.scoopgmbh.copper.gui.ui.audittrail.result.AuditTrailResultModel;
import de.scoopgmbh.copper.gui.ui.sql.filter.SqlFilterModel;
import de.scoopgmbh.copper.gui.ui.sql.result.SqlResultModel;
import de.scoopgmbh.copper.gui.ui.workflowhistory.filter.WorkflowHistoryFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowhistory.result.WorkflowHistoryResultModel;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowinstance.result.WorkflowInstanceResultModel;
import de.scoopgmbh.copper.gui.ui.workflowsummary.filter.WorkflowSummaryFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowsummary.result.WorkflowSummaryResultModel;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultModel;
import de.scoopgmbh.copper.gui.util.WorkflowVersion;
import de.scoopgmbh.copper.monitor.core.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.core.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitor.core.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.core.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowClassVersionInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowInstanceHistory;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowSummary;

public class GuiCopperDataProvider {
	
	private final CopperMonitorInterface copperDataProvider;
	
	public GuiCopperDataProvider(final CopperMonitorInterface copperDataProvider) {
		super();
		this.copperDataProvider=copperDataProvider;
		maxResultCount= new SimpleObjectProperty<Integer>(1000);
	}
	
	SimpleObjectProperty<Integer> maxResultCount;
	public SimpleObjectProperty<Integer> getMaxResultCount(){
		return maxResultCount;
	}
	
	public List<WorkflowInstanceResultModel> getWorkflowInstanceList(WorkflowInstanceFilterModel filter){
		List<WorkflowInstanceInfo> list;
		try {
			list = copperDataProvider.getWorkflowInstanceList(getPoolId(filter.enginePoolModel),filter.version.classname.getValue(),
					filter.state.getValue(), getFilterReadyInteger(filter.priority.getValue()), maxResultCount.getValue());
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
	
	public List<AuditTrailResultModel> getAuditTrails(de.scoopgmbh.copper.gui.ui.audittrail.filter.AuditTrailFilterModel  filter){
		
		List<AuditTrailInfo> list;
		try {
			list = copperDataProvider.getAuditTrails(filter.workflowClass.getValue(), filter.workflowInstanceId.getValue(), filter.correlationId.getValue(), getFilterReadyInteger(filter.level.getValue()), maxResultCount.getValue());
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
	
	public List<SqlResultModel> executeSqlQuery(SqlFilterModel filter){
		List<String[]> list;
		try {
			list = copperDataProvider.executeSqlQuery(filter.sqlQuery.getValue(), maxResultCount.getValue());
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
			return copperDataProvider.getMeasurePoints(engineFilter.enginePoolModel.selectedEngine.getValue().getId());
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

	public List<WorkflowHistoryResultModel> getWorkflowInstanceHistory(WorkflowHistoryFilterModel filter) {
		List<WorkflowInstanceHistory> list;
		try {
			list = copperDataProvider.getWorkflowInstanceHistory();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowHistoryResultModel> result = new ArrayList<WorkflowHistoryResultModel>();
		for (WorkflowInstanceHistory workflowInstanceHistory: list){
			result.add(new WorkflowHistoryResultModel(workflowInstanceHistory)); 
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
	
}
