/*
 * Copyright 2002-2012 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitor.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.scoopgmbh.copper.monitor.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitor.adapter.model.CopperStatusInfo;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo.EngineTyp;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessorPoolInfo;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessorPoolInfo.ProcessorPoolTyp;
import de.scoopgmbh.copper.monitor.adapter.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassVersionInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceMetaDataInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummery;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummery;

public class RMIForwardCopperDataProvider extends UnicastRemoteObject implements CopperMonitorInterface {
	private static final long serialVersionUID = -5757718583261293846L;
	
	protected RMIForwardCopperDataProvider() throws RemoteException {
		super();
	}


	@Override
	public String getAuditTrailMessage(long id) throws RemoteException {
		String json = "{\r\n" + 
				"		    \"firstName\": \"John\",\r\n" + 
				"		    \"lastName\": \"Smith\",\r\n" + 
				"		    \"age\": 25,\r\n" + 
				"		    \"address\": {\r\n" + 
				"		        \"streetAddress\": \"21 2nd Street\",\r\n" + 
				"		        \"city\": \"New York\",\r\n" + 
				"		        \"state\": \"NY\",\r\n" + 
				"		        \"postalCode\": 10021\r\n" + 
				"		    },\r\n" + 
				"		    \"phoneNumber\": [\r\n" + 
				"		        {\r\n" + 
				"		            \"type\": \"home\",\r\n" + 
				"		            \"number\": \"212 555-1234\"\r\n" + 
				"		        },\r\n" + 
				"		        {\r\n" + 
				"		            \"type\": \"fax\",\r\n" + 
				"		            \"number\": \"646 555-4567\"\r\n" + 
				"		        }\r\n" + 
				"		    ]\r\n" + 
				"		}";
		
		String xml = 
				"<?xml version=\"1.0\"?>\r\n" + 
				"<note>\r\n" + 
				"    <to>Tove</to>\r\n" + 
				"    <from>Jani</from>\r\n" + 
				"    <heading>Reminder</heading>\r\n" + 
				"    <body>Don't forget me this weekend!</body>\r\n" + 
				"</note>\r\n" + 
				"";
		
		String text = "blablablbalabl";
		
		int rnd = new Random().nextInt(3);
		if (rnd==0){
			return json;
		}
		if (rnd==1){
			return xml;
		}
		if (rnd==2){
			return text;
		}
		return "";
	}

	@Override
	public List<WorkflowSummery> getWorkflowSummery(ProcessingEngineInfo engine, WorkflowClassVersionInfo workflowClassDescription,
			long resultRowLimit) throws RemoteException {		Map<WorkflowInstanceState,Integer> map = new HashMap<>();
		for (WorkflowInstanceState workflowInstanceState: WorkflowInstanceState.values()){
			map.put(workflowInstanceState, (int)(Math.random()*100));
		}
		
		ArrayList<WorkflowSummery> result = new ArrayList<>();
		WorkflowSummery workflowSummery = new WorkflowSummery("",10,
				new WorkflowClassVersionInfo("blubclass1","alias",0L,+(long)(Math.random()*100),0L),
				new WorkflowStateSummery(map));
		result.add(workflowSummery);
		
		WorkflowSummery inputcopy = new WorkflowSummery("",10,
				workflowClassDescription,
				new WorkflowStateSummery(map));
		result.add(inputcopy);
		
		return result;
	}

	@Override
	public List<WorkflowInstanceInfo> getWorkflowInstanceList(ProcessingEngineInfo engine, WorkflowClassVersionInfo workflowClassDescription,
			WorkflowInstanceState state, Integer priority, long resultRowLimit) throws RemoteException {
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
	public List<AuditTrailInfo> getAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level, long resultRowLimit)
			throws RemoteException {
		ArrayList<AuditTrailInfo> result = new ArrayList<>();
		AuditTrailInfo auditTrailInfo = new AuditTrailInfo();
		auditTrailInfo.setId(1);
		auditTrailInfo.setLoglevel(1);
		result.add(auditTrailInfo);
		auditTrailInfo = new AuditTrailInfo();
		auditTrailInfo.setId(2);
		auditTrailInfo.setLoglevel(2);
		result.add(auditTrailInfo);
		auditTrailInfo = new AuditTrailInfo();
		auditTrailInfo.setId(3);
		auditTrailInfo.setLoglevel(3);
		auditTrailInfo.setOccurrence(new Date());
		auditTrailInfo.setMessageType("json");
		result.add(auditTrailInfo);
		return result;
	}


	@Override
	public CopperStatusInfo getCopperStatus() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowClassVersionInfo> getWorkflowClassesList() throws RemoteException {
		ArrayList<WorkflowClassVersionInfo> result = new ArrayList<>();
		result.add(new WorkflowClassVersionInfo("blubclass1","alias",0L,+(long)(Math.random()*100),0L));
		result.add(new WorkflowClassVersionInfo("blubclass2","alias",1L,+(long)(Math.random()*100),0L));
		result.add(new WorkflowClassVersionInfo("blubclass2","alias",1L,+(long)(Math.random()*100),0L));
		result.add(new WorkflowClassVersionInfo("blubclass2","alias",1L,+(long)(Math.random()*100),0L));
		result.add(new WorkflowClassVersionInfo("blubclass2","alias",2L,+(long)(Math.random()*100),0L));
		result.add(new WorkflowClassVersionInfo("blubclass2","alias",2L,+(long)(Math.random()*100),0L));
		result.add(new WorkflowClassVersionInfo("blubclass2","alias",2L,+(long)(Math.random()*100),0L));
		result.add(new WorkflowClassVersionInfo("blubclass3","alias",3L,+(long)(Math.random()*100),0L));
		return result;
	}

	@Override
	public WorkflowInstanceMetaDataInfo getWorkflowInstanceDetails(String workflowInstanceId) {
		// TODO Auto-generated method stub
		return new WorkflowInstanceMetaDataInfo();
	}

	@Override
	public CopperInterfaceSettings getSettings() throws RemoteException {
		return new CopperInterfaceSettings(true);
	}

	@Override
	public List<String[]> executeSqlQuery(String query, long resultRowLimit) {
		List<String[]>  result = new ArrayList<>();
		result.add(new String[]{"column1","column2",query});
		return result;
	}
	
	@Override
	public SystemResourcesInfo getSystemResourceInfo() throws RemoteException {
		return new PerformanceMonitor().getRessourcenInfo();
	}

	@Override
	public WorkflowStateSummery getAggregatedWorkflowStateSummery(ProcessingEngineInfo engine) throws RemoteException {
		Map<WorkflowInstanceState,Integer> map = new HashMap<>();
		for (WorkflowInstanceState workflowInstanceState: WorkflowInstanceState.values()){
			map.put(workflowInstanceState, (int)(Math.random()*100));
		}
		return new WorkflowStateSummery(map);
	}

	@Override
	public void restart(String workflowInstanceId, ProcessingEngineInfo engine) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restartAll(ProcessingEngineInfo engine) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ProcessingEngineInfo> getProccessingEngineList() throws RemoteException {
		return Arrays.asList(
				new ProcessingEngineInfo(EngineTyp.PERSISTENT,"peId1", new ProcessorPoolInfo("poId1",ProcessorPoolTyp.PERSISTENT)),
				new ProcessingEngineInfo(EngineTyp.TRANSIENT,"peId2", new ProcessorPoolInfo("poId2",ProcessorPoolTyp.TRANSIENT), new ProcessorPoolInfo("poId3",ProcessorPoolTyp.PRIORITY_TRANSIENT))
				);
	}
	
}
