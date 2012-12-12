package de.scoopgmbh.copper.monitor.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import de.scoopgmbh.copper.monitor.adapter.CopperDataProvider;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperStatusInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceMetaDataInfo;

public class RMIForwardCopperDataProvider implements CopperDataProvider {

	
	public RMIForwardCopperDataProvider(int port) {
		try {
			LocateRegistry.createRegistry( port );
			CopperDataProvider stub = (CopperDataProvider) UnicastRemoteObject.exportObject(this, 0);
			RemoteServer.setLog(System.out);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(CopperDataProvider.class.getSimpleName(), stub);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@Override
	public int getWorkflowInstancesInfosCount() {
		return 0;
	}

	@Override
	public List<WorkflowInstanceInfo> getWorkflowInstancesInfos(int fromCount, int toCount) {
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
	public int getWorkflowInstancesInfosCount(String worklfowId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<WorkflowInstanceInfo> getWorkflowInstancesInfos(String worklfowId, int fromCount, int toCount) {
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
	public WorkflowInstanceMetaDataInfo getWorkflowInstanceMetaData(String worklfowId) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public CopperStatusInfo getCopperStatus() {
		// TODO Auto-generated method stub
		return null;
	}


	
	
	
}
