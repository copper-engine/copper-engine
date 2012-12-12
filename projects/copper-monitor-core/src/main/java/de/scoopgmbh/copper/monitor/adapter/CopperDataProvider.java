package de.scoopgmbh.copper.monitor.adapter;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperStatusInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceMetaDataInfo;

public interface CopperDataProvider extends Remote {

	public int getWorkflowInstancesInfosCount(String session) throws RemoteException;

	public List<WorkflowInstanceInfo> getWorkflowInstancesInfos(String session, int fromCount, int toCount) throws RemoteException;

	public int getWorkflowInstancesInfosCount(String session, String worklfowId) throws RemoteException;

	public List<WorkflowInstanceInfo> getWorkflowInstancesInfos(String session, String worklfowId, int fromCount, int toCount) throws RemoteException;

	public List<WorkflowClassesInfo> getWorklowClassesInfos(String session) throws RemoteException;

	public List<AuditTrailInfo> getAuditTrails(String session, String transactionId, String conversationId, String correlationId, Integer level,
			int maxResult) throws RemoteException;

	public String getAuditTrailMessage(String session, long id) throws RemoteException;

	public WorkflowInstanceMetaDataInfo getWorkflowInstanceMetaData(String session, String worklfowId) throws RemoteException;

	public CopperStatusInfo getCopperStatus(String session) throws RemoteException;

}

