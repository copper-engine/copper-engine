package de.scoopgmbh.copper.monitor.adapter;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperStatusInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceMetaDataInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummery;

public interface CopperDataProvider extends Remote, Serializable {

	public List<WorkflowSummery> getWorkflowSummery(String workflowclass, String minorversion, String majorversion) throws RemoteException;

	public List<WorkflowInstanceInfo> getWorkflowInstancesInfos(String worklfowId, int fromCount, int toCount) throws RemoteException;

	public List<WorkflowClassesInfo> getWorklowClassesInfos() throws RemoteException;

	public List<AuditTrailInfo> getAuditTrails(String transactionId, String conversationId, String correlationId, Integer level,
			int maxResult) throws RemoteException;

	public String getAuditTrailMessage(long id) throws RemoteException;

	public WorkflowInstanceMetaDataInfo getWorkflowInstanceMetaData(String worklfowId) throws RemoteException;

	public CopperStatusInfo getCopperStatus() throws RemoteException;

}

