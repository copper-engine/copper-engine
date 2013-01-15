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
import de.scoopgmbh.copper.gui.ui.audittrail.result.AuditTrailResultModel;
import de.scoopgmbh.copper.gui.ui.sql.filter.SqlFilterModel;
import de.scoopgmbh.copper.gui.ui.sql.result.SqlResultModel;
import de.scoopgmbh.copper.gui.ui.workflowclasssesctree.WorkflowClassesModel;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowinstance.result.WorkflowInstanceResultModel;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowsummery.result.WorkflowSummeryResultModel;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultModel;
import de.scoopgmbh.copper.monitor.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperLoadInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummery;

public class GuiCopperDataProvider {
	
	private final CopperMonitorInterface copperDataProvider;
	
	public GuiCopperDataProvider(final CopperMonitorInterface copperDataProvider) {
		super();
		this.copperDataProvider=copperDataProvider;
		maxResultCount= new SimpleObjectProperty<>(1000);
	}
	
	SimpleObjectProperty<Integer> maxResultCount;
	public SimpleObjectProperty<Integer> getMaxResultCount(){
		return maxResultCount;
	}
	
	public List<WorkflowInstanceResultModel> getWorkflowInstanceList(WorkflowInstanceFilterModel filter){
		List<WorkflowInstanceInfo> list;
		try {
			list = copperDataProvider.getWorkflowInstanceList(filter.workflowSummeryFilterModel.workflowclass.getValue(),
					filter.workflowSummeryFilterModel.workflowMajorVersion.getValue(),filter.workflowSummeryFilterModel.workflowMinorVersion.getValue(),
					filter.state.getValue(), filter.priority.getValue(), maxResultCount.getValue());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowInstanceResultModel> result = new ArrayList<>();
		for (WorkflowInstanceInfo workflowInstanceInfo: list){
			result.add(new WorkflowInstanceResultModel(workflowInstanceInfo));
		}
		return result;
	}
	
	public List<AuditTrailResultModel> getAuditTrails(de.scoopgmbh.copper.gui.ui.audittrail.filter.AuditTrailFilterModel  filter){
		
		List<AuditTrailInfo> list;
		try {
			list = copperDataProvider.getAuditTrails(filter.workflowClass.getValue(), filter.workflowInstanceId.getValue(), filter.correlationId.getValue(), filter.level.getValue(), maxResultCount.getValue());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<AuditTrailResultModel> result = new ArrayList<>();
		for (AuditTrailInfo auditTrailInfo: list){
			result.add(new AuditTrailResultModel(auditTrailInfo));
		}
		return result;
	}

	public List<WorkflowSummeryResultModel> getWorkflowSummery(WorkflowSummeryFilterModel filter) {
		List<WorkflowSummery> summeries;
		try {
			summeries = copperDataProvider.getWorkflowSummery(filter.workflowclass.getValue(), filter.workflowMajorVersion.getValue(), filter.workflowMinorVersion.getValue(), maxResultCount.getValue());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowSummeryResultModel> result = new ArrayList<>();
		for (WorkflowSummery workflowSummery: summeries){
			result.add(new WorkflowSummeryResultModel(workflowSummery));
		}
		return result;
	}
	
	public List<WorkflowClassesModel> getWorkflowClassesList(){
		List<WorkflowClassesInfo> list;
		try {
			list = copperDataProvider.getWorkflowClassesList();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowClassesModel> result = new ArrayList<>();
		for (WorkflowClassesInfo workflowClassesInfo: list){
			result.add(new WorkflowClassesModel(workflowClassesInfo));
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

	public CopperLoadInfo getCopperLoadInfo() {
		try {
			return  copperDataProvider.getCopperLoadInfo();
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
		ArrayList<SqlResultModel> result = new ArrayList<>();
		for (String[] row: list){
			result.add(new SqlResultModel(row));
		}
		return result;
	}
	
}
