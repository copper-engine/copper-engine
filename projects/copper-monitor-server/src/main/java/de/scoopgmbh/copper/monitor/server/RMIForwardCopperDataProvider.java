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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.scoopgmbh.copper.monitor.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitor.adapter.model.CopperLoadInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperStatusInfo;
import de.scoopgmbh.copper.monitor.adapter.model.SystemResourcesInfo;
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
	public List<WorkflowSummery> getWorkflowSummery(String workflowclass, Long majorversion, Long minorversion, Long patchlevel, long resultRowLimit) throws RemoteException {
		ArrayList<WorkflowSummery> result = new ArrayList<>();
		WorkflowSummery workflowSummery = new WorkflowSummery();
		workflowSummery.setWorkflowClass("worklfowclass1");
		result.add(workflowSummery);
		workflowSummery = new WorkflowSummery();
		workflowSummery.setWorkflowClass("worklfowclass2");
		workflowSummery.setWorkflowMajorVersion(1);
		workflowSummery.setWorkflowMinorVersion(2);
		result.add(workflowSummery);
		workflowSummery = new WorkflowSummery();
		workflowSummery.setWorkflowClass("worklfowclass2");
		workflowSummery.setWorkflowMajorVersion(2);
		result.add(workflowSummery);
		workflowSummery = new WorkflowSummery();
		workflowSummery.setWorkflowClass("worklfowclass2");
		workflowSummery.setWorkflowMajorVersion(3);
		result.add(workflowSummery);
		workflowSummery = new WorkflowSummery();
		workflowSummery.setWorkflowClass("worklfowclass3");
		result.add(workflowSummery);
		
		workflowSummery = new WorkflowSummery();
		workflowSummery.setWorkflowClass(workflowclass);
		workflowSummery.setWorkflowMajorVersion(majorversion!=null?majorversion:0);
		workflowSummery.setWorkflowMinorVersion(minorversion!=null?minorversion:0);
		workflowSummery.setWorkflowPatchLevel(patchlevel!=null?patchlevel:0);
		result.add(workflowSummery);
		
		return result;
	}

	@Override
	public List<WorkflowInstanceInfo> getWorkflowInstanceList(String workflowclass, Long majorversion, Long minorversion, Long patchlevel,
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
	public List<WorkflowClassesInfo> getWorkflowClassesList() throws RemoteException {
		ArrayList<WorkflowClassesInfo> result = new ArrayList<>();
		result.add(new WorkflowClassesInfo("blubclass1",0,+(int)(Math.random()*100),0));
		result.add(new WorkflowClassesInfo("blubclass2",1,+(int)(Math.random()*100),0));
		result.add(new WorkflowClassesInfo("blubclass2",1,+(int)(Math.random()*100),0));
		result.add(new WorkflowClassesInfo("blubclass2",1,+(int)(Math.random()*100),0));
		result.add(new WorkflowClassesInfo("blubclass2",2,+(int)(Math.random()*100),0));
		result.add(new WorkflowClassesInfo("blubclass2",2,+(int)(Math.random()*100),0));
		result.add(new WorkflowClassesInfo("blubclass2",2,+(int)(Math.random()*100),0));
		result.add(new WorkflowClassesInfo("blubclass3",3,+(int)(Math.random()*100),0));
		return result;
	}

	@Override
	public WorkflowInstanceMetaDataInfo getWorkflowInstanceDetails(String workflowInstanceId) {
		// TODO Auto-generated method stub
		return new WorkflowInstanceMetaDataInfo();
	}


	@Override
	public CopperLoadInfo getCopperLoadInfo() throws RemoteException {
		Map<WorkflowInstanceState,Integer> map = new HashMap<>();
		for (WorkflowInstanceState workflowInstanceState: WorkflowInstanceState.values()){
			map.put(workflowInstanceState, (int)(Math.random()*100));
		}
		return new CopperLoadInfo(map);
	}


	@Override
	public CopperInterfaceSettings getSettings() throws RemoteException {
		return new CopperInterfaceSettings(true);
	}


	@Override
	public List<String[]> executeSqlQuery(String query, long resultRowLimit) {
		List<String[]>  result = new ArrayList<>();
		result.add(new String[]{"column1","column2","column3"});
		return result;
	}
	
	
	@Override
	public SystemResourcesInfo getSystemResourceInfo() throws RemoteException {
		return new PerformanceMonitor().getRessourcenInfo();
	}

	
}
