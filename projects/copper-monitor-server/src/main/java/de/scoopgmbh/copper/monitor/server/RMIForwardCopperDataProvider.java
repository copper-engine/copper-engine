package de.scoopgmbh.copper.monitor.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.scoopgmbh.copper.monitor.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperLoadInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperStatusInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceMetaDataInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummery;

public class RMIForwardCopperDataProvider extends UnicastRemoteObject implements CopperMonitorInterface {
	private static final long serialVersionUID = -5757718583261293846L;
	
	protected RMIForwardCopperDataProvider() throws RemoteException {
		super();
	}


	@Override
	public String getAuditTrailMessage(long id) throws RemoteException {
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



	@Override
	public List<WorkflowInstanceInfo> getWorkflowInstanceList(WorkflowInstanceState state, Integer priority) throws RemoteException {
		ArrayList<WorkflowInstanceInfo> result = new ArrayList<>();
		WorkflowInstanceInfo workflowInfo = new WorkflowInstanceInfo();
		workflowInfo.setId("1");
		result.add(workflowInfo);
		workflowInfo = new WorkflowInstanceInfo();
		workflowInfo.setId("2");
		result.add(workflowInfo);
		workflowInfo = new WorkflowInstanceInfo();
		workflowInfo.setId("3");
		workflowInfo.setTimeout(new Date());
		result.add(workflowInfo);
		return result;
	}

	@Override
	public void setMaxResultCount(int count) {
		System.out.println("new count: "+count);
	}

	@Override
	public List<AuditTrailInfo> getAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level)
			throws RemoteException {
		ArrayList<AuditTrailInfo> result = new ArrayList<>();
		AuditTrailInfo auditTrailInfo = new AuditTrailInfo();
		auditTrailInfo.setId(1);
		result.add(auditTrailInfo);
		auditTrailInfo = new AuditTrailInfo();
		auditTrailInfo.setId(2);
		result.add(auditTrailInfo);
		auditTrailInfo = new AuditTrailInfo();
		auditTrailInfo.setId(3);
		auditTrailInfo.setOccurrence(new Date());
		result.add(auditTrailInfo);
		return result;
	}


	@Override
	public WorkflowInstanceMetaDataInfo getWorkflowInstanceMetaData(String workflowInstanceId) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CopperStatusInfo getCopperStatus() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<WorkflowClassesInfo> getWorkflowClassesList() throws RemoteException {
		ArrayList<WorkflowClassesInfo> result = new ArrayList<>();
		result.add(new WorkflowClassesInfo("blubclass1",""+Math.random(),""));
		result.add(new WorkflowClassesInfo("blubclass2",""+Math.random(),""));
		result.add(new WorkflowClassesInfo("blubclass3",""+Math.random(),""));
		return result;
	}


	@Override
	public WorkflowInstanceMetaDataInfo getWorkflowInstanceDetails(String workflowInstanceId) {
		// TODO Auto-generated method stub
		return new WorkflowInstanceMetaDataInfo();
	}


	@Override
	public CopperLoadInfo getCopperLoadInfo() throws RemoteException {
		return new CopperLoadInfo((int)(Math.random()*100),(int)(Math.random()*100));
	}

	
}
