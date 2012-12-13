package de.scoopgmbh.copper.gui.adapter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.scoopgmbh.copper.gui.model.AuditTrailFilterModel;
import de.scoopgmbh.copper.gui.model.AuditTrailInfoModel;
import de.scoopgmbh.copper.gui.model.WorkflowClassInfoModel;
import de.scoopgmbh.copper.gui.model.WorkflowInstancesInfoModel;
import de.scoopgmbh.copper.gui.ui.workflowsummery.result.WorkflowSummeryResultModel;
import de.scoopgmbh.copper.monitor.adapter.CopperDataProvider;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummery;

public class GuiCopperDataProvider {
	
	private final CopperDataProvider copperDataProvider;
	
	public GuiCopperDataProvider(CopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider=copperDataProvider;
	}

	public int getWorkflowInstancesInfosCount(){
		return 3;
	}
	
	public List<WorkflowInstancesInfoModel> getWorkflowInstancesInfos(int fromCount, int toCount){
		List<WorkflowInstanceInfo> infos;
		try {
			infos = copperDataProvider.getWorkflowInstancesInfos("dummySession",fromCount, toCount);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowInstancesInfoModel> result = new ArrayList<>();
		for (WorkflowInstanceInfo workflowInstancesInfo: infos){
			result.add(new WorkflowInstancesInfoModel(workflowInstancesInfo.getId(),workflowInstancesInfo.getProcessorPoolId(),workflowInstancesInfo.getPriority(), workflowInstancesInfo.getProcessorPoolId(), workflowInstancesInfo.getTimeout()));
		}
		return result;
	}
	
	public int getWorkflowInstancesInfosCount(String worklfowId){
		return 3;
	}
	
	public List<WorkflowInstancesInfoModel> getWorkflowInstancesInfos(String worklfowId, int fromCount, int toCount){
		ArrayList<WorkflowInstancesInfoModel> result = new ArrayList<>();
		result.add(new WorkflowInstancesInfoModel("id1", "42", 1, "processorPoolId", new Date()));
		result.add(new WorkflowInstancesInfoModel("id2", "42", 1, "processorPoolId", new Date()));
		result.add(new WorkflowInstancesInfoModel("id3", "42", 2, "processorPoolId", new Date()));
		return result;
	}
	
	public int getWorkflowClassInfosCount(String worklfowId){
		return 3;
	}
	
	public List<WorkflowClassInfoModel> getWorklowClassesInfos(){
		ArrayList<WorkflowClassInfoModel> result = new ArrayList<>();
		result.add(new WorkflowClassInfoModel("dummyWorkflow.classname", "version1", "alias1"));
		result.add(new WorkflowClassInfoModel("dummyWorkflow.classname", "version2", "alias2"));
		result.add(new WorkflowClassInfoModel("dummyWorkflow.classname", "version3", "alias3"));
		return result;
	}
	
	List<AuditTrailInfoModel> getAuditTrails(AuditTrailFilterModel filter){
		return java.util.Collections.emptyList();
	}
	
	String getAuditTrailMessage(long id){
		return "bla";
	}

	public List<WorkflowSummeryResultModel> getWorkflowSummery(String workflowclass, String minorversion, String majorversion) {
		List<WorkflowSummery> summeries;
		try {
			summeries = copperDataProvider.getWorkflowSummery(workflowclass, minorversion, majorversion);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowSummeryResultModel> result = new ArrayList<>();
		for (WorkflowSummery workflowSummery: summeries){
			result.add(new WorkflowSummeryResultModel(workflowSummery));
		}
		return result;
	}
	
//		getAuditTrails(String transactionId, String conversationId, String correlationId, Integer level, int maxResult)
//	
//	byte[] getMessage(long id);
	
}
