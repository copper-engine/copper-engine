/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitoring.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.scoopgmbh.copper.monitoring.core.CopperMonitoringService;
import de.scoopgmbh.copper.monitoring.core.model.AdapterHistoryInfo;
import de.scoopgmbh.copper.monitoring.core.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitoring.core.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitoring.core.model.DependencyInjectorInfo;
import de.scoopgmbh.copper.monitoring.core.model.DependencyInjectorInfo.DependencyInjectorTyp;
import de.scoopgmbh.copper.monitoring.core.model.LogData;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.core.model.MessageInfo;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo.EngineTyp;
import de.scoopgmbh.copper.monitoring.core.model.ProcessorPoolInfo;
import de.scoopgmbh.copper.monitoring.core.model.ProcessorPoolInfo.ProcessorPoolTyp;
import de.scoopgmbh.copper.monitoring.core.model.StorageInfo;
import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowClassVersionInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceMetaDataInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowRepositoryInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowRepositoryInfo.WorkflowRepositorTyp;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowSummary;

public class CopperDataProviderMock extends UnicastRemoteObject implements CopperMonitoringService {
	private static final long serialVersionUID = -5757718583261293846L;
	
	protected CopperDataProviderMock() throws RemoteException {
		super();
	}
	
	
	public static CopperMonitoringService createSecurityWarppedMock() {
		try {
			return CopperMonitorServiceSecurityProxy.secure(new CopperDataProviderMock());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	};


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
	public List<WorkflowSummary> getWorkflowSummary(String poolid, String classname) throws RemoteException {	
		Map<WorkflowInstanceState,Integer> map = new HashMap<WorkflowInstanceState,Integer>();
		for (WorkflowInstanceState workflowInstanceState: WorkflowInstanceState.values()){
			map.put(workflowInstanceState, (int)(Math.random()*100));
		}
		
		ArrayList<WorkflowSummary> result = new ArrayList<WorkflowSummary>();
		WorkflowSummary workflowSummery = new WorkflowSummary("",10,
				new WorkflowClassVersionInfo("blubclass1","alias",0L,+(long)(Math.random()*100),0L),
				new WorkflowStateSummary(map));
		result.add(workflowSummery);
		
		WorkflowSummary inputcopy = new WorkflowSummary("",10,
				new WorkflowClassVersionInfo(classname,"alias",0L,+(long)(Math.random()*100),0L),
				new WorkflowStateSummary(map));
		result.add(inputcopy);
		
		return result;
	}

	@Override
	public List<WorkflowInstanceInfo> getWorkflowInstanceList(String poolid, String classname,
			WorkflowInstanceState state, Integer priority, Date form, Date to, long resultRowLimit) throws RemoteException {
		ArrayList<WorkflowInstanceInfo> result = new ArrayList<WorkflowInstanceInfo>();
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
		ArrayList<AuditTrailInfo> result = new ArrayList<AuditTrailInfo>();
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
	public List<WorkflowClassVersionInfo> getWorkflowClassesList(final String engineId) throws RemoteException {
		ArrayList<WorkflowClassVersionInfo> result = new ArrayList<WorkflowClassVersionInfo>();
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
		List<String[]>  result = new ArrayList<String[]>();
		result.add(new String[]{"column1","column2",query});
		return result;
	}
	
	@Override
	public SystemResourcesInfo getSystemResourceInfo() throws RemoteException {
		return new PerformanceMonitor().getRessourcenInfo();
	}

	@Override
	public WorkflowStateSummary getAggregatedWorkflowStateSummary(String engineid) throws RemoteException {
		Map<WorkflowInstanceState,Integer> map = new HashMap<WorkflowInstanceState,Integer>();
		for (WorkflowInstanceState workflowInstanceState: WorkflowInstanceState.values()){
			map.put(workflowInstanceState, (int)(Math.random()*100));
		}
		return new WorkflowStateSummary(map);
	}

	@Override
	public void restartWorkflowInstance(String workflowInstanceId, String engineid) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restartAllErroneousInstances(String engineid) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ProcessingEngineInfo> getProccessingEngineList() throws RemoteException {
		WorkflowRepositoryInfo repositoryInfo = new WorkflowRepositoryInfo();
		repositoryInfo.setWorkflowRepositorTyp(WorkflowRepositorTyp.FILE);
		repositoryInfo.setSrcPaths(new ArrayList<String>());
		return Arrays.asList(
				new ProcessingEngineInfo(EngineTyp.PERSISTENT,"peId1",repositoryInfo,new DependencyInjectorInfo(DependencyInjectorTyp.POJO),new StorageInfo(), new ProcessorPoolInfo("poId1",ProcessorPoolTyp.PERSISTENT)),
				new ProcessingEngineInfo(EngineTyp.TRANSIENT,"peId2",repositoryInfo,new DependencyInjectorInfo(DependencyInjectorTyp.POJO),new StorageInfo(), new ProcessorPoolInfo("poId2",ProcessorPoolTyp.TRANSIENT), new ProcessorPoolInfo("poId3",ProcessorPoolTyp.TRANSIENT))
				);
	}


	@Override
	public List<MeasurePointData> getMeasurePoints(String engineid) {
		ArrayList<MeasurePointData> result = new ArrayList<MeasurePointData>();
		for (int i=0;i<20;i++){
			result.add(new MeasurePointData("point dhajsgdjahdgsdjasdgjasgdhjgfhjsgfjshd"+i,10,i+(int)(10000*Math.random()),10));
		}
		return result;
	}


	@Override
	public void setNumberOfThreads(String engineid, String processorPoolId, int numberOfThreads) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setThreadPriority(String engineid, String processorPoolId, int threadPriority) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void resetMeasurePoints() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBatcherNumThreads(int numThread, String engineid) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public List<MessageInfo> getMessageList(boolean ignoreproceeded ,long resultRowLimit) {
		return Collections.emptyList();
	}

	@Override
	public AdapterHistoryInfo getAdapterHistoryInfos(String adapterId) throws RemoteException {
		return new AdapterHistoryInfo();
	}


	@Override
	public List<MeasurePointData> getMonitoringMeasurePoints(String measurePoint,long limit) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<String> getMonitoringMeasurePointIds() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public LogData getLogData() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void updateLogConfig(String config) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
}
