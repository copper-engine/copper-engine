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
package de.scoopgmbh.copper.monitoring;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.audit.DummyPostProcessor;
import de.scoopgmbh.copper.audit.MessagePostProcessor;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.monitor.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassVersionInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceHistory;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceMetaDataInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowRepositoryInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummary;
import de.scoopgmbh.copper.persistent.PersistentScottyEngine;
import de.scoopgmbh.copper.persistent.ScottyDBStorage;
import de.scoopgmbh.copper.persistent.ScottyDBStorageInterface;

public class RmiMonitoringView implements CopperMonitorInterface{
	private static final long serialVersionUID = 1829707298427309206L;

	private final MonitoringEventQueue monitoringQueue;
	private final ScottyDBStorageInterface dbStorage;;
	private CopperInterfaceSettings copperInterfaceSettings= new CopperInterfaceSettings(false);
	
	public CopperInterfaceSettings getCopperInterfaceSettings() {
		return copperInterfaceSettings;
	}

//	public void setCopperInterfaceSettings(CopperInterfaceSettings copperInterfaceSettings) {
//		this.copperInterfaceSettings = copperInterfaceSettings;
//	}

	public RmiMonitoringView(final MonitoringEventQueue monitoringQueue, ScottyDBStorageInterface dbStorage){
		this.monitoringQueue= monitoringQueue;
		this.dbStorage = dbStorage;
		
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
		return monitoringQueue.callAndWait(new MonitoringDataAwareCallable<List<WorkflowClassVersionInfo>>() {
			@Override
			public List<WorkflowClassVersionInfo> call() throws Exception {
				WorkflowRepository workflowRepository = monitoringData.getWorkflowRepository(engineId);
				if (workflowRepository!=null){
					return workflowRepository.getAllWorkflowsInfos();
				} else {
					return Collections.emptyList();
				}
			}
		});
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

	private static Sigar sigar = new Sigar();
	@Override
	public SystemResourcesInfo getSystemResourceInfo() throws RemoteException {
		//http://docs.oracle.com/javase/7/docs/jre/api/management/extension/com/sun/management/OperatingSystemMXBean.html
//		java.lang.management.OperatingSystemMXBean operatingSystemMXBean= ManagementFactory.getOperatingSystemMXBean();
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		java.lang.management.ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ClassLoadingMXBean classLoadingMXBean  = ManagementFactory.getClassLoadingMXBean();
		
		Mem mem = null;
        try {
            mem = sigar.getMem();
        } catch (SigarException se) {
            se.printStackTrace();
        }
		CpuPerc cpuPerc = null;
		try {
			cpuPerc = sigar.getCpuPerc();
        } catch (SigarException se) {
            se.printStackTrace();
        }		
				
		return new SystemResourcesInfo(new Date(),
				Math.max(cpuPerc.getCombined(),0),
				mem.getActualFree() / 1024 / 1024,
				Math.max(cpuPerc.getUser(),0),
				memoryMXBean.getHeapMemoryUsage().getUsed(),
				threadMXBean.getThreadCount(),
				classLoadingMXBean.getTotalLoadedClassCount());
	}

	@Override
	public void restartErroneousInstance(final String workflowInstanceId, final String engineid) throws RemoteException {
		monitoringQueue.put(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				if (monitoringData.engines.get(engineid) instanceof PersistentScottyEngine){//TODO why is that not implemented for Transient
					try {
						((PersistentScottyEngine)monitoringData.engines.get(engineid)).restart(workflowInstanceId);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
	}

	@Override
	public void restartAllErroneousInstances(final String engineid) throws RemoteException {
		monitoringQueue.put(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				if (monitoringData.engines.get(engineid) instanceof PersistentScottyEngine){//TODO why is that not implemented for Transient
					try {
						((PersistentScottyEngine)monitoringData.engines.get(engineid)).restartAll();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
	}

	@Override
	public void setNumberOfThreads(final String engineid, final String processorPoolId, final int numberOfThreads) throws RemoteException {
		monitoringQueue.put(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringData.getPool(processorPoolId, engineid).setNumberOfThreads(numberOfThreads);
			}
		});//PersistentScottyEngine
	}

	@Override
	public void setThreadPriority(final String engineid, final String processorPoolId, final int threadPriority) throws RemoteException {
		monitoringQueue.put(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringData.getPool(processorPoolId, engineid).setThreadPriority(threadPriority);
			}
		});
	}

	@Override
	public List<ProcessingEngineInfo> getProccessingEngineList() throws RemoteException {
		return monitoringQueue.callAndWait(new MonitoringDataAwareCallable<List<ProcessingEngineInfo>>() {
			@Override
			public List<ProcessingEngineInfo> call() throws Exception {
				return new ArrayList<ProcessingEngineInfo>(monitoringData.createProcessingEngineInfos());
			}
		});
	}

	@Override
	public List<MeasurePointData> getMeasurePoints(String engineid) throws RemoteException {
		return monitoringQueue.callAndWait(new MonitoringDataAwareCallable<List<MeasurePointData>>() {
			@Override
			public List<MeasurePointData> call() throws Exception {
				return new ArrayList<MeasurePointData>(monitoringData.measurePoints.values());
			}
		});
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
		monitoringQueue.put(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringData.measurePoints.clear();
			}
		});
	}

	@Override
	public WorkflowRepositoryInfo getWorkflowRepositoryInfo(final String engineid) throws RemoteException {
		return monitoringQueue.callAndWait(new MonitoringDataAwareCallable<WorkflowRepositoryInfo>() {
			@Override
			public WorkflowRepositoryInfo call() throws Exception {
				return monitoringData.createProcessingEngineInfos(engineid).getRepositoryInfo();
			}
		});
	}

	@Override
	public void setBatcherNumThreads(final int numThread, final String engineid) {
		monitoringQueue.put(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				ProcessingEngine engine =  monitoringData.engines.get(engineid);
				if (engine instanceof PersistentScottyEngine){
					ScottyDBStorageInterface storage =  ((PersistentScottyEngine)engine).getDbStorage();
					if (storage instanceof ScottyDBStorage){
						((ScottyDBStorage)storage).getBatcher().setNumThreads(numThread);
					}
				}
			}
		});
	}

	@Override
	public List<WorkflowInstanceHistory> getWorkflowInstanceHistory() {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				
			}
		});
		return monitoringQueue.callAndWait(new MonitoringDataAwareCallable<List<WorkflowInstanceHistory>>() {
			@Override
			public List<WorkflowInstanceHistory> call() throws Exception {
				return monitoringData.workflowInstanceHistorys;
			}
		});
	}
	

}
