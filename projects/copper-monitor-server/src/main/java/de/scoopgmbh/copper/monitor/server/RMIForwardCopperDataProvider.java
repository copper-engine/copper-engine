package de.scoopgmbh.copper.monitor.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import de.scoopgmbh.copper.monitor.adapter.CopperDataProvider;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperStatusInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceMetaDataInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummery;

public class RMIForwardCopperDataProvider extends UnicastRemoteObject implements CopperDataProvider {
	private static final long serialVersionUID = -5757718583261293846L;
	
	protected RMIForwardCopperDataProvider() throws RemoteException {
		super();
	}



	@Override
	public List<WorkflowInstanceInfo> getWorkflowInstancesInfos(String worklfowId, int fromCount, int toCount) throws RemoteException {
		ArrayList<WorkflowInstanceInfo> result = new ArrayList<>();
		WorkflowInstanceInfo workflowInfo = new WorkflowInstanceInfo();
		workflowInfo.setId("1");
		result.add(workflowInfo);
		workflowInfo = new WorkflowInstanceInfo();
		workflowInfo.setId("2");
		result.add(workflowInfo);
		workflowInfo = new WorkflowInstanceInfo();
		workflowInfo.setId("3");
		result.add(workflowInfo);
		return result;
	}

	@Override
	public List<WorkflowClassesInfo> getWorklowClassesInfos() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuditTrailInfo> getAuditTrails(String transactionId, String conversationId, String correlationId, Integer level,
			int maxResult) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuditTrailMessage(long id) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstanceMetaDataInfo getWorkflowInstanceMetaData(String worklfowId) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CopperStatusInfo getCopperStatus() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public List<WorkflowSummery> getWorkflowSummery(String workflowclass, String minorversion, String majorversion) throws RemoteException {
		ArrayList<WorkflowSummery> result = new ArrayList<>();
		WorkflowSummery workflowSummery = new WorkflowSummery();
		workflowSummery.setClazz("worklfowclass1");
		result.add(workflowSummery);
		workflowSummery = new WorkflowSummery();
		workflowSummery.setClazz("worklfowclass2");
		result.add(workflowSummery);
		workflowSummery = new WorkflowSummery();
		workflowSummery.setClazz("worklfowclass3");
		result.add(workflowSummery);
		return result;
	}


	
	
	
}
