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
package de.scoopgmbh.copper.monitor.server;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.scoopgmbh.copper.audit.DummyPostProcessor;
import de.scoopgmbh.copper.audit.MessagePostProcessor;
import de.scoopgmbh.copper.management.FileBasedWorkflowRepositoryMXBean;
import de.scoopgmbh.copper.management.PersistentPriorityProcessorPoolMXBean;
import de.scoopgmbh.copper.management.PersistentProcessingEngineMXBean;
import de.scoopgmbh.copper.management.ProcessingEngineMXBean;
import de.scoopgmbh.copper.management.ProcessorPoolMXBean;
import de.scoopgmbh.copper.management.StatisticsCollectorMXBean;
import de.scoopgmbh.copper.management.WorkflowRepositoryMXBean;
import de.scoopgmbh.copper.management.model.EngineType;
import de.scoopgmbh.copper.management.model.WorkflowClassInfo;
import de.scoopgmbh.copper.monitor.core.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.core.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.BatcherInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitor.core.adapter.model.DependencyInjectorInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.DependencyInjectorInfo.DependencyInjectorTyp;
import de.scoopgmbh.copper.monitor.core.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.core.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.ProcessingEngineInfo.EngineTyp;
import de.scoopgmbh.copper.monitor.core.adapter.model.ProcessorPoolInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.ProcessorPoolInfo.ProcessorPoolTyp;
import de.scoopgmbh.copper.monitor.core.adapter.model.StorageInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowClassVersionInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowInstanceHistory;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowInstanceMetaDataInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowRepositoryInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowSummary;
import de.scoopgmbh.copper.monitor.server.persistent.MonitoringDbStorage;
import de.scoopgmbh.copper.monitor.server.workaround.HistoryCollectorMXBean;

public class RmiMonitoringView implements CopperMonitorInterface{
	private static final long serialVersionUID = 1829707298427309206L;

	private final MonitoringDbStorage dbStorage;
	private final CopperInterfaceSettings copperInterfaceSettings;
	private final StatisticsCollectorMXBean statisticsCollectorMXBean;
	private final HistoryCollectorMXBean historyCollectorMXBean;
	private final List<ProcessorPoolMXBean> pools;
	private final Map<String,ProcessingEngineMXBean> engines;
	
	public RmiMonitoringView(MonitoringDbStorage dbStorage,
			CopperInterfaceSettings copperInterfaceSettings, 
			StatisticsCollectorMXBean statisticsCollectorMXBean,
			List<ProcessingEngineMXBean> engineList, 
			List<ProcessorPoolMXBean> processorPoolMXBean,
			HistoryCollectorMXBean historyCollectorMXBean){
		this.dbStorage = dbStorage;
		this.copperInterfaceSettings = copperInterfaceSettings;
		this.statisticsCollectorMXBean = statisticsCollectorMXBean;
		this.historyCollectorMXBean = historyCollectorMXBean;
		this.pools = processorPoolMXBean;
		
		engines = new HashMap<String,ProcessingEngineMXBean>();
		for (ProcessingEngineMXBean engine: engineList){
			engines.put(engine.getEngineId(),engine);
		}
		
		try {
			System.setProperty("java.rmi.server.randomIDs", "true");
			//http://www.drdobbs.com/jvm/building-secure-java-rmi-servers/184405197
			//Up until now, I have assumed that if a client doesn't go through the login object, it can't access any of the proxy objects.
			//An interesting thing happens when you set the java.rmi.server.randomIDs property to True.
			//Object Id stops being a counter and becomes a securely generated 64-bit random number. 
			//So if someone wants to access an object by using the wire protocol, it must find the correct UID of the object and get the 64-bit number right. 
			//This constitutes a sparse space large enough for preventing most attacks. 
			
			LocateRegistry.createRegistry( Registry.REGISTRY_PORT );
			UnicastRemoteObject.exportObject(this, 0);
			RemoteServer.setLog(System.out);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(CopperMonitorInterface.class.getSimpleName(), this);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<WorkflowSummary> getWorkflowSummary(final String poolid, final String classname)
			throws RemoteException {
		return dbStorage.selectWorkflowSummary(poolid,  classname);
	}

	@Override
	public List<WorkflowInstanceInfo> getWorkflowInstanceList(final String poolid, final String classname,
			final WorkflowInstanceState state, final Integer priority, final long resultRowLimit) throws RemoteException {
		return dbStorage.selectWorkflowInstanceList(poolid,  classname, state, priority, resultRowLimit);
	}

	@Override
	public List<AuditTrailInfo> getAuditTrails(final String workflowClass, final String workflowInstanceId, final String correlationId, final Integer level, final long resultRowLimit)
			throws RemoteException {
		return dbStorage.selectAuditTrails(workflowClass, workflowInstanceId, correlationId, level, resultRowLimit);
	}

	@Override
	public String getAuditTrailMessage(final long id) throws RemoteException {
		return dbStorage.selectAuditTrailMessage(id,messagePostProcessor);
	}

	@Override
	public List<WorkflowClassVersionInfo> getWorkflowClassesList(final String engineId) throws RemoteException {
		WorkflowRepositoryMXBean workflowRepository = getWorkflowRepository(engineId);
		if (workflowRepository!=null){
			List<WorkflowClassVersionInfo> result = new ArrayList<WorkflowClassVersionInfo>();
			for (WorkflowClassInfo workflowClassInfo: workflowRepository.getWorkflows()){
				result.add(new WorkflowClassVersionInfo(
						workflowClassInfo.getClassname(), 
						workflowClassInfo.getAlias(),
						workflowClassInfo.getMajorVersion(), 
						workflowClassInfo.getMinorVersion(), 
						workflowClassInfo.getPatchLevel()));
			}
			return result;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public WorkflowInstanceMetaDataInfo getWorkflowInstanceDetails(String workflowInstanceId) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowStateSummary getAggregatedWorkflowStateSummary(String engineid) throws RemoteException {
		return dbStorage.selectTotalWorkflowStateSummary();
	}

	@Override
	public CopperInterfaceSettings getSettings() throws RemoteException {
		return copperInterfaceSettings;
	}

	@Override
	public List<String[]> executeSqlQuery(String query, long resultRowLimit) throws RemoteException {
		return dbStorage.executeMonitoringQuery(query, resultRowLimit);
	}

	@Override
	public SystemResourcesInfo getSystemResourceInfo() throws RemoteException {
		//http://docs.oracle.com/javase/7/docs/jre/api/management/extension/com/sun/management/OperatingSystemMXBean.html
//		java.lang.management.OperatingSystemMXBean operatingSystemMXBean= ManagementFactory.getOperatingSystemMXBean();
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		java.lang.management.ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ClassLoadingMXBean classLoadingMXBean  = ManagementFactory.getClassLoadingMXBean();

		return new SystemResourcesInfo(new Date(),
				Math.max(0,0),
				0,
				Math.max(0,0),
				memoryMXBean.getHeapMemoryUsage().getUsed(),
				threadMXBean.getThreadCount(),
				classLoadingMXBean.getTotalLoadedClassCount());
	}

	@Override
	public void restartErroneousInstance(final String workflowInstanceId, final String engineid) throws RemoteException {
		if (engines.get(engineid) instanceof PersistentProcessingEngineMXBean) {// TODO why is that not implemented for Transient
			try {
				((PersistentProcessingEngineMXBean) engines.get(engineid)).restart(workflowInstanceId);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void restartAllErroneousInstances(final String engineid) throws RemoteException {
		if (engines.get(engineid) instanceof PersistentProcessingEngineMXBean){//TODO why is that not implemented for Transient
			try {
				((PersistentProcessingEngineMXBean)engines.get(engineid)).restartAll();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void setNumberOfThreads(final String engineid, final String processorPoolId, final int numberOfThreads) throws RemoteException {
		getPool(processorPoolId, engineid).setNumberOfThreads(numberOfThreads);
	}

	@Override
	public void setThreadPriority(final String engineid, final String processorPoolId, final int threadPriority) throws RemoteException {
		getPool(processorPoolId, engineid).setThreadPriority(threadPriority);
	}

	@Override
	public List<ProcessingEngineInfo> getProccessingEngineList() throws RemoteException {
		return new ArrayList<ProcessingEngineInfo>(createProcessingEngineInfos());
	}

	@Override
	public List<MeasurePointData> getMeasurePoints(String engineid) throws RemoteException {
		List<MeasurePointData> result = new ArrayList<MeasurePointData>();
		for (de.scoopgmbh.copper.management.model.MeasurePointData jmxmeasurePointData: statisticsCollectorMXBean.queryAll()){
			result.add(new MeasurePointData(
					jmxmeasurePointData.getMpId(),
					jmxmeasurePointData.getElementCount(),
					jmxmeasurePointData.getElapsedTimeMicros(),
				    jmxmeasurePointData.getCount()));
		}
		return result;
	}
	
	MessagePostProcessor messagePostProcessor = new DummyPostProcessor();

	public MessagePostProcessor getMessagePostProcessor() {
		return messagePostProcessor;
	}

	public void setAuditrailMessagePostProcessor(MessagePostProcessor messagePostProcessor) {
		this.messagePostProcessor = messagePostProcessor;
	}
	
	@Override
	public void resetMeasurePoints() {
		statisticsCollectorMXBean.reset();
	}

	@Override
	public WorkflowRepositoryInfo getWorkflowRepositoryInfo(final String engineid) throws RemoteException {
		return createProcessingEngineInfos(engineid).getRepositoryInfo();
	}

	@Override
	public void setBatcherNumThreads(final int numThread, final String engineid) {
//		ProcessingEngineMXBean engine =  engines.get(engineid);
//		if (engine instanceof PersistentProcessingEngineMXBean){
//			??? storage =  ((PersistentProcessingEngineMXBean)engine).getDbStorage();
////					if (storage instanceof ScottyDBStorage){
////						((ScottyDBStorage)storage).getBatcher().setNumThreads(numThread);
////					}
//		}

	}

	@Override
	public List<WorkflowInstanceHistory> getWorkflowInstanceHistory() {
		List<WorkflowInstanceHistory> result = new ArrayList<WorkflowInstanceHistory>();
//		for (de.scoopgmbh.copper.management.model.???? jmxmeasurePointData: historyCollectorMXBean.queryAll()){
//			result.add(new WorkflowInstanceHistory(???));
//		}
		return result;
	}

	private ProcessorPoolMXBean getPool(String poolId, String engineid){
		ProcessingEngineMXBean engine = engines.get(engineid);
		if (engine!=null){
			for (ProcessorPoolMXBean pool: engine.getProcessorPools()){
				if (pool.getId().equals(poolId)){
					return pool;
				}
			}
		}
		return null;
	}
	
	private List<ProcessingEngineInfo> createProcessingEngineInfos(){
		ArrayList<ProcessingEngineInfo> result = new ArrayList<ProcessingEngineInfo>();
		for (ProcessingEngineMXBean engine: engines.values()){
			WorkflowRepositoryInfo workflowRepositoryInfo = new WorkflowRepositoryInfo();
			workflowRepositoryInfo.setName(engine.getWorkflowRepository().getDescription());
			if (workflowRepositoryInfo instanceof FileBasedWorkflowRepositoryMXBean){
				workflowRepositoryInfo.setSrcPaths(((FileBasedWorkflowRepositoryMXBean)engine.getWorkflowRepository()).getSourceDirs());
			}
			
			
			DependencyInjectorInfo dependencyInjectorInfo = new DependencyInjectorInfo(DependencyInjectorTyp.UNKNOWN);
			
			StorageInfo storageInfo = new StorageInfo();
			storageInfo.setName("UNKNOWN");
			BatcherInfo batcher= new BatcherInfo();
			batcher.setName("UNKNOWN");
			batcher.setNumThreads(0);
			storageInfo.setBatcher(batcher);
			
			List<ProcessorPoolInfo> enginepools = new ArrayList<ProcessorPoolInfo>();
			for (ProcessorPoolMXBean pool: engine.getProcessorPools()){
				boolean isPersistent = pool instanceof PersistentPriorityProcessorPoolMXBean;
				enginepools.add(new ProcessorPoolInfo(
						pool.getId(), 
						isPersistent?ProcessorPoolTyp.PERSISTENT:ProcessorPoolTyp.TRANSIENT, 
						isPersistent?((PersistentPriorityProcessorPoolMXBean)pool).getLowerThreshold():0, 
						isPersistent?((PersistentPriorityProcessorPoolMXBean)pool).getUpperThreshold():0, 
						isPersistent?((PersistentPriorityProcessorPoolMXBean)pool).getUpperThresholdReachedWaitMSec():0, 
						isPersistent?((PersistentPriorityProcessorPoolMXBean)pool).getEmptyQueueWaitMSec():0, 
						isPersistent?((PersistentPriorityProcessorPoolMXBean)pool).getDequeueBulkSize():0, 
						pool.getThreadPriority(), 
						pool.getNumberOfThreads(), 
						pool.getMemoryQueueSize()));
			}
			ProcessingEngineInfo engineInfo = new ProcessingEngineInfo(
					engine.getEngineType()==EngineType.persistent?EngineTyp.PERSISTENT:EngineTyp.TRANSIENT, 
							engine.getEngineId(), workflowRepositoryInfo, dependencyInjectorInfo, storageInfo, enginepools.toArray(new ProcessorPoolInfo[0]));
			result.add(engineInfo);
			
		}
		return result; 
	}
	
	private ProcessingEngineInfo createProcessingEngineInfos(String engineId){
		List<ProcessingEngineInfo> list = createProcessingEngineInfos();
		for (ProcessingEngineInfo processingEngineInfo: list){
			if (processingEngineInfo.getId().equals(engineId)){
				return processingEngineInfo;
			}
		}
		return null; 
	}
	
	private WorkflowRepositoryMXBean getWorkflowRepository(String engineId){
		if (engines.containsKey(engineId)){
			return engines.get(engineId).getWorkflowRepository();
		}
		return null; 
	}

}
