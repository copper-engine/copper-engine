package de.scoopgmbh.copper.monitor.adapter;

import java.util.List;

public class JMXCopperDataProvider implements CopperDataProvider {

	
	public JMXCopperDataProvider(String serverAdress, String port) {
	
	}
	
	
	@Override
	public int getWorkflowInstancesInfosCount() {
		return 0;
	}

	@Override
	public List<WorkflowInfo> getWorkflowInstancesInfos(int fromCount, int toCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getWorkflowInstancesInfosCount(String worklfowId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<WorkflowInfo> getWorkflowInstancesInfos(String worklfowId, int fromCount, int toCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowClassesInfo> getWorklowClassesInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuditTrailInfo> getAuditTrails(String transactionId, String conversationId, String correlationId, Integer level,
			int maxResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuditTrailMessage(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstanceMetaData getWorkflowInstanceMetaData(String worklfowId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getProcessorPoolNumberOfThreads() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getProcessorPoolThreadPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getProcessorPoolMemoryQueueSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
}
