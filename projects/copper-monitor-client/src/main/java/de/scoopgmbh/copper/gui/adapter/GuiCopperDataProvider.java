package de.scoopgmbh.copper.gui.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.scoopgmbh.copper.gui.model.AuditTrailFilterModel;
import de.scoopgmbh.copper.gui.model.AuditTrailInfoModel;
import de.scoopgmbh.copper.gui.model.WorkflowClassInfoModel;
import de.scoopgmbh.copper.gui.model.WorkflowInstancesInfoModel;

public class GuiCopperDataProvider {
	
	private final CopperDataProvider copperDataProvider;
	
	public GuiCopperDataProvider(CopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

	public int getWorkflowInstancesInfosCount(){
		return 3;
	}
	
	public List<WorkflowInstancesInfoModel> getWorkflowInstancesInfos(int fromCount, int toCount){
		ArrayList<WorkflowInstancesInfoModel> result = new ArrayList<>();
		result.add(new WorkflowInstancesInfoModel("id1", "42", 1, "processorPoolId", new Date()));
		result.add(new WorkflowInstancesInfoModel("id2", "42", 1, "processorPoolId", new Date()));
		result.add(new WorkflowInstancesInfoModel("id3", "42", 2, "processorPoolId", new Date()));
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
	
//		getAuditTrails(String transactionId, String conversationId, String correlationId, Integer level, int maxResult)
//	
//	byte[] getMessage(long id);
	
}
