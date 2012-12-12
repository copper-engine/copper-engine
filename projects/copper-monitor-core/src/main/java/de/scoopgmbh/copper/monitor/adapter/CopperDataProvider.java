package de.scoopgmbh.copper.monitor.adapter;

import java.rmi.Remote;
import java.util.List;

public interface CopperDataProvider extends Remote {
	
	public int getWorkflowInstancesInfosCount();
	
	public List<WorkflowInfo> getWorkflowInstancesInfos(int fromCount, int toCount);
	
	public int getWorkflowInstancesInfosCount(String worklfowId);
	
	public List<WorkflowInfo> getWorkflowInstancesInfos(String worklfowId, int fromCount, int toCount);
	
	public List<WorkflowClassesInfo> getWorklowClassesInfos();
	
	public List<AuditTrailInfo> getAuditTrails(String transactionId, String conversationId, String correlationId, Integer level, int maxResult);
	
	public String getAuditTrailMessage(long id);
	
	public WorkflowInstanceMetaData getWorkflowInstanceMetaData(String worklfowId);

	public int getProcessorPoolNumberOfThreads();

	public int getProcessorPoolThreadPriority();
	
	public int getProcessorPoolMemoryQueueSize();

}

