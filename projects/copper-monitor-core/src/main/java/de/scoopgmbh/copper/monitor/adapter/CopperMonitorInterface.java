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
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummery;

public interface CopperMonitorInterface extends Remote, Serializable {

	public List<WorkflowSummery> getWorkflowSummery(String workflowclass, String minorversion, String majorversion) throws RemoteException;

	public List<WorkflowInstanceInfo> getWorkflowInstanceList(WorkflowInstanceState state, Integer priority) throws RemoteException;
	
	/**set max result count for all query from this interface (to reduce load from underlying database)
	 * @param count
	 */
	public void setMaxResultCount(int count) throws RemoteException;

	public List<AuditTrailInfo> getAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level) throws RemoteException;

	public String getAuditTrailMessage(long id) throws RemoteException;

	public WorkflowInstanceMetaDataInfo getWorkflowInstanceMetaData(String workflowInstanceId) throws RemoteException;
	
	public List<WorkflowClassesInfo> getWorkflowClassesList() throws RemoteException;

	public CopperStatusInfo getCopperStatus() throws RemoteException;
}

