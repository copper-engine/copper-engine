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

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.audit.DummyPostProcessor;
import de.scoopgmbh.copper.audit.MessagePostProcessor;
import de.scoopgmbh.copper.monitor.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassVersionInfo;
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
		return monitoringQueue.callAndWait(new MonitoringDataAwareCallable<List<WorkflowSummary>>() {
			@Override
			public List<WorkflowSummary> call() throws Exception {
				return dbStorage.selectWorkflowSummary(poolid,  classname);
			}
		});
	}

	@Override
	public List<WorkflowInstanceInfo> getWorkflowInstanceList(final String poolid, final String classname,
			final WorkflowInstanceState state, final Integer priority, final long resultRowLimit) throws RemoteException {
		return monitoringQueue.callAndWait(new MonitoringDataAwareCallable<List<WorkflowInstanceInfo>>() {
			@Override
			public List<WorkflowInstanceInfo> call() throws Exception {
				return dbStorage.selectWorkflowInstanceList(poolid,  classname, state, priority, resultRowLimit);
			}
		});
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
	public List<WorkflowClassVersionInfo> getWorkflowClassesList() throws RemoteException {
		return monitoringQueue.callAndWait(new MonitoringDataAwareCallable<List<WorkflowClassVersionInfo>>() {
			@Override
			public List<WorkflowClassVersionInfo> call() throws Exception {
				if (monitoringData.workflowRepository!=null){
					return monitoringData.workflowRepository.getAllWorkflowsInfos();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SystemResourcesInfo getSystemResourceInfo() throws RemoteException {
		return monitoringQueue.callAndWait(new MonitoringDataAwareCallable<SystemResourcesInfo>() {
			@Override
			public SystemResourcesInfo call() throws Exception {
				//http://docs.oracle.com/javase/7/docs/jre/api/management/extension/com/sun/management/OperatingSystemMXBean.html
				com.sun.management.OperatingSystemMXBean operatingSystemMXBean= ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean());
				MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
				java.lang.management.ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
				ClassLoadingMXBean classLoadingMXBean  = ManagementFactory.getClassLoadingMXBean();
				return new SystemResourcesInfo(new Date(),
						Math.max(operatingSystemMXBean.getSystemCpuLoad(),0),
						operatingSystemMXBean.getFreePhysicalMemorySize(),
						Math.max(operatingSystemMXBean.getProcessCpuLoad(),0),
						memoryMXBean.getHeapMemoryUsage().getUsed(),
						threadMXBean.getThreadCount(),
						classLoadingMXBean.getTotalLoadedClassCount());
			}
		});
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
	

}
