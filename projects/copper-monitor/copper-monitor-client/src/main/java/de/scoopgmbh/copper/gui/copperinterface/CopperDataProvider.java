package de.scoopgmbh.copper.gui.copperinterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CopperDataProvider {

	public List<WorkflowInstancesInfoModel> getWorkflowInstancesInfos(){
		ArrayList<WorkflowInstancesInfoModel> result = new ArrayList<>();
		result.add(new WorkflowInstancesInfoModel("id1", "42", 1, "processorPoolId", new Date()));
		result.add(new WorkflowInstancesInfoModel("id2", "42", 1, "processorPoolId", new Date()));
		result.add(new WorkflowInstancesInfoModel("id3", "42", 2, "processorPoolId", new Date()));
		return result;
	}
	
	
}
