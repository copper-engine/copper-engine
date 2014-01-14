/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.management.BatcherMXBean;
import org.copperengine.management.DBStorageMXBean;
import org.copperengine.management.FileBasedWorkflowRepositoryMXBean;
import org.copperengine.management.PersistentPriorityProcessorPoolMXBean;
import org.copperengine.management.PersistentProcessingEngineMXBean;
import org.copperengine.management.ProcessingEngineMXBean;
import org.copperengine.management.ProcessorPoolMXBean;
import org.copperengine.management.ScottyDBStorageMXBean;
import org.copperengine.management.StatisticsCollectorMXBean;
import org.copperengine.management.WorkflowRepositoryMXBean;
import org.copperengine.management.model.EngineType;
import org.copperengine.management.model.WorkflowClassInfo;
import org.copperengine.monitoring.core.CopperMonitoringService;
import org.copperengine.monitoring.core.data.filter.MonitoringDataFilter;
import org.copperengine.monitoring.core.model.AuditTrailInfo;
import org.copperengine.monitoring.core.model.BatcherInfo;
import org.copperengine.monitoring.core.model.CopperInterfaceSettings;
import org.copperengine.monitoring.core.model.DependencyInjectorInfo;
import org.copperengine.monitoring.core.model.MeasurePointData;
import org.copperengine.monitoring.core.model.MessageInfo;
import org.copperengine.monitoring.core.model.MonitoringDataProviderInfo;
import org.copperengine.monitoring.core.model.MonitoringDataStorageInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo.EngineTyp;
import org.copperengine.monitoring.core.model.ProcessorPoolInfo;
import org.copperengine.monitoring.core.model.ProcessorPoolInfo.ProcessorPoolTyp;
import org.copperengine.monitoring.core.model.StorageInfo;
import org.copperengine.monitoring.core.model.WorkflowClassMetaData;
import org.copperengine.monitoring.core.model.WorkflowInstanceInfo;
import org.copperengine.monitoring.core.model.WorkflowInstanceMetaData;
import org.copperengine.monitoring.core.model.WorkflowInstanceState;
import org.copperengine.monitoring.core.model.WorkflowRepositoryInfo;
import org.copperengine.monitoring.core.model.WorkflowRepositoryInfo.WorkflowRepositorTyp;
import org.copperengine.monitoring.core.model.WorkflowStateSummary;
import org.copperengine.monitoring.core.model.WorkflowSummary;
import org.copperengine.monitoring.core.statistic.StatisticCreator;
import org.copperengine.monitoring.server.debug.WorkflowInstanceIntrospector;
import org.copperengine.monitoring.server.logging.LogConfigManager;
import org.copperengine.monitoring.server.monitoring.MonitoringDataAccessQueue;
import org.copperengine.monitoring.server.monitoring.MonitoringDataAwareCallable;
import org.copperengine.monitoring.server.persistent.MonitoringDbStorage;
import org.copperengine.monitoring.server.provider.MonitoringDataProvider;
import org.copperengine.monitoring.server.provider.MonitoringDataProviderManager;

import com.google.common.base.Optional;

public class DefaultCopperMonitoringService implements CopperMonitoringService {
    private static final long serialVersionUID = 1829707298427309206L;

    private final MonitoringDbStorage dbStorage;
    private final CopperInterfaceSettings copperInterfaceSettings;
    private final StatisticsCollectorMXBean statisticsCollectorMXBean;
    private final Map<String, ProcessingEngineMXBean> engines;
    private final MonitoringDataAccessQueue monitoringDataAccessQueue;
    final WorkflowInstanceIntrospector workflowInstanceIntrospector;
    private final LogConfigManager logManager;
    private final MonitoringDataProviderManager monitoringDataProviderManager;

    public DefaultCopperMonitoringService(MonitoringDbStorage dbStorage,
            StatisticsCollectorMXBean statisticsCollectorMXBean,
            List<ProcessingEngineMXBean> engineList,
            MonitoringDataAccessQueue monitoringDataAccessQueue,
            boolean enableSql,
            WorkflowInstanceIntrospector workflowInstanceIntrospector,
            LogConfigManager logManager,
            MonitoringDataProviderManager monitoringDataProviderManager) {
        this(dbStorage, new CopperInterfaceSettings(enableSql), statisticsCollectorMXBean, engineList
                , monitoringDataAccessQueue, workflowInstanceIntrospector, logManager, monitoringDataProviderManager);
    }

    public DefaultCopperMonitoringService(MonitoringDbStorage dbStorage,
            CopperInterfaceSettings copperInterfaceSettings,
            StatisticsCollectorMXBean statisticsCollectorMXBean,
            List<ProcessingEngineMXBean> engineList,
            MonitoringDataAccessQueue monitoringDataAccessQueue,
            WorkflowInstanceIntrospector workflowInstanceIntrospector,
            LogConfigManager logManager,
            MonitoringDataProviderManager monitoringDataProviderManager) {
        this.dbStorage = dbStorage;
        this.copperInterfaceSettings = copperInterfaceSettings;
        this.statisticsCollectorMXBean = statisticsCollectorMXBean;
        this.monitoringDataAccessQueue = monitoringDataAccessQueue;
        this.workflowInstanceIntrospector = workflowInstanceIntrospector;
        this.logManager = logManager;
        this.monitoringDataProviderManager = monitoringDataProviderManager;

        engines = new HashMap<String, ProcessingEngineMXBean>();
        for (ProcessingEngineMXBean engine : engineList) {
            engines.put(engine.getEngineId(), engine);
        }
    }

    @Override
    public List<WorkflowSummary> getWorkflowSummary(final String poolid, final String classname)
            throws RemoteException {
        return dbStorage.selectWorkflowSummary(poolid, classname);
    }

    @Override
    public List<WorkflowInstanceInfo> getWorkflowInstanceList(final String poolid, final String classname,
            final WorkflowInstanceState state, final Integer priority, Date from, Date to, final long resultRowLimit) throws RemoteException {
        return dbStorage.selectWorkflowInstanceList(poolid, classname, state, priority, from, to, null, resultRowLimit);
    }

    @Override
    public List<AuditTrailInfo> getAuditTrails(final String workflowClass, final String workflowInstanceId, final String correlationId, final Integer level, final long resultRowLimit)
            throws RemoteException {
        return dbStorage.selectAuditTrails(workflowClass, workflowInstanceId, correlationId, level, resultRowLimit);
    }

    @Override
    public String getAuditTrailMessage(final long id) throws RemoteException {
        return dbStorage.selectAuditTrailMessage(id);
    }

    @Override
    public List<WorkflowClassMetaData> getWorkflowClassesList(final String engineId) throws RemoteException {
        WorkflowRepositoryMXBean workflowRepository = getWorkflowRepository(engineId);
        if (workflowRepository != null) {
            List<WorkflowClassMetaData> result = new ArrayList<WorkflowClassMetaData>();
            for (WorkflowClassInfo workflowClassInfo : workflowRepository.getWorkflows()) {
                result.add(new WorkflowClassMetaData(
                        workflowClassInfo.getClassname(),
                        workflowClassInfo.getAlias(),
                        workflowClassInfo.getMajorVersion(),
                        workflowClassInfo.getMinorVersion(),
                        workflowClassInfo.getPatchLevel(),
                        workflowClassInfo.getSourceCode()));
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public WorkflowInstanceMetaData getWorkflowInstanceDetails(String workflowInstanceId, String engineId) throws RemoteException {
        List<WorkflowClassMetaData> classList = getWorkflowClassesList(engineId);
        String classname = dbStorage.selectWorkflowInstanceList(null, null, null, null, null, null, workflowInstanceId, 1).get(0).getClassname();

        WorkflowClassMetaData workflowClassMetaData = null;
        for (WorkflowClassMetaData workflowClassMetaDataI : classList) {
            if (classname.equals(workflowClassMetaDataI.getClassname())) {
                workflowClassMetaData = workflowClassMetaDataI;
            }
        }

        try {
            return new WorkflowInstanceMetaData(workflowClassMetaData, workflowInstanceIntrospector.getInstanceInfo(workflowInstanceId));
        } catch (Exception e) {
            throw new RemoteException("Bad", e);
        }
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
        if (copperInterfaceSettings.isCanExecuteSql()) {
            return dbStorage.executeMonitoringQuery(query, resultRowLimit);
        }
        return Collections.emptyList();
    }

    @Override
    public void restartWorkflowInstance(final String workflowInstanceId, final String engineid) throws RemoteException {
        if (engines.get(engineid) instanceof PersistentProcessingEngineMXBean) {
            // TODO why is that not implemented for Transient
            try {
                ((PersistentProcessingEngineMXBean) engines.get(engineid)).restart(workflowInstanceId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void restartAllErroneousInstances(final String engineid) throws RemoteException {
        if (engines.get(engineid) instanceof PersistentProcessingEngineMXBean) {
            // TODO why is that not implemented for Transient
            try {
                ((PersistentProcessingEngineMXBean) engines.get(engineid)).restartAll();
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
        for (org.copperengine.management.model.MeasurePointData jmxmeasurePointData : statisticsCollectorMXBean.queryAll()) {
            result.add(new MeasurePointData(
                    jmxmeasurePointData.getMpId(),
                    jmxmeasurePointData.getElementCount(),
                    jmxmeasurePointData.getElapsedTimeMicros(),
                    jmxmeasurePointData.getCount()));
        }
        return result;
    }

    @Override
    public void resetMeasurePoints() {
        statisticsCollectorMXBean.reset();
    }

    @Override
    public void setBatcherNumThreads(final int numThread, final String engineid) {
        ProcessingEngineMXBean engine = engines.get(engineid);
        if (engine instanceof PersistentProcessingEngineMXBean) {
            DBStorageMXBean storage = ((PersistentProcessingEngineMXBean) engine).getDBStorage();
            if (storage instanceof ScottyDBStorageMXBean) {
                BatcherMXBean batcherMXBean = ((ScottyDBStorageMXBean) storage).getBatcherMXBean();
                if (batcherMXBean != null) {
                    batcherMXBean.setNumThreads(numThread);
                }
            }
        }
    }

    @Override
    public List<MessageInfo> getMessageList(final boolean ignoreProcceded, long resultRowLimit) {
        return dbStorage.selectMessages(ignoreProcceded, resultRowLimit);
    }

    private ProcessorPoolMXBean getPool(String poolId, String engineid) {
        ProcessingEngineMXBean engine = engines.get(engineid);
        if (engine != null) {
            for (ProcessorPoolMXBean pool : engine.getProcessorPools()) {
                if (pool.getId().equals(poolId)) {
                    return pool;
                }
            }
        }
        return null;
    }

    private List<ProcessingEngineInfo> createProcessingEngineInfos() {
        ArrayList<ProcessingEngineInfo> result = new ArrayList<ProcessingEngineInfo>();
        for (ProcessingEngineMXBean engine : engines.values()) {
            WorkflowRepositoryInfo workflowRepositoryInfo = new WorkflowRepositoryInfo();
            WorkflowRepositoryMXBean workflowRepository = engine.getWorkflowRepository();
            workflowRepositoryInfo.setName(workflowRepository.getDescription());
            workflowRepositoryInfo.setSrcPaths(Collections.<String>emptyList());
            workflowRepositoryInfo.setWorkflowRepositorTyp(WorkflowRepositorTyp.UNKOWN);
            if (workflowRepository instanceof FileBasedWorkflowRepositoryMXBean) {
                workflowRepositoryInfo.setWorkflowRepositorTyp(WorkflowRepositorTyp.FILE);
                FileBasedWorkflowRepositoryMXBean fileBasedRepo = (FileBasedWorkflowRepositoryMXBean) workflowRepository;
                List<String> srcPaths = new ArrayList<String>(fileBasedRepo.getSourceDirs());
                srcPaths.addAll(fileBasedRepo.getSourceArchiveUrls());
                workflowRepositoryInfo.setSrcPaths(srcPaths);
            }

            DependencyInjectorInfo dependencyInjectorInfo = new DependencyInjectorInfo(engine.getDependencyInjectorType());

            StorageInfo storageInfo = new StorageInfo();

            BatcherInfo batcher = new BatcherInfo();
            if (engine instanceof PersistentProcessingEngineMXBean) {
                DBStorageMXBean dbStorageTmp = ((PersistentProcessingEngineMXBean) engine).getDBStorage();
                storageInfo.setDescription(dbStorageTmp.getDescription());
                if (dbStorageTmp instanceof ScottyDBStorageMXBean) {
                    BatcherMXBean batcherMXBean = ((ScottyDBStorageMXBean) dbStorageTmp).getBatcherMXBean();
                    if (batcherMXBean != null) {
                        batcher.setDescription(batcherMXBean.getDescription());
                        batcher.setNumThreads(batcherMXBean.getNumThreads());
                    }
                }
            }

            storageInfo.setBatcher(batcher);

            List<ProcessorPoolInfo> enginepools = new ArrayList<ProcessorPoolInfo>();
            for (ProcessorPoolMXBean pool : engine.getProcessorPools()) {
                boolean isPersistent = pool instanceof PersistentPriorityProcessorPoolMXBean;
                enginepools.add(new ProcessorPoolInfo(
                        pool.getId(),
                        isPersistent ? ProcessorPoolTyp.PERSISTENT : ProcessorPoolTyp.TRANSIENT,
                        isPersistent ? ((PersistentPriorityProcessorPoolMXBean) pool).getLowerThreshold() : 0,
                        isPersistent ? ((PersistentPriorityProcessorPoolMXBean) pool).getUpperThreshold() : 0,
                        isPersistent ? ((PersistentPriorityProcessorPoolMXBean) pool).getUpperThresholdReachedWaitMSec() : 0,
                        isPersistent ? ((PersistentPriorityProcessorPoolMXBean) pool).getEmptyQueueWaitMSec() : 0,
                        isPersistent ? ((PersistentPriorityProcessorPoolMXBean) pool).getDequeueBulkSize() : 0,
                        pool.getNumberOfThreads(),
                        pool.getThreadPriority(),
                        pool.getMemoryQueueSize()));
            }
            ProcessingEngineInfo engineInfo = new ProcessingEngineInfo(
                    engine.getEngineType() == EngineType.persistent ? EngineTyp.PERSISTENT : EngineTyp.TRANSIENT,
                    engine.getEngineId(), workflowRepositoryInfo, dependencyInjectorInfo,
                    engine.getStatisticsCollectorType(), storageInfo, enginepools.toArray(new ProcessorPoolInfo[0]));
            result.add(engineInfo);

        }
        return result;
    }

    private WorkflowRepositoryMXBean getWorkflowRepository(String engineId) {
        if (engines.containsKey(engineId)) {
            return engines.get(engineId).getWorkflowRepository();
        }
        return null;
    }

    @Override
    public String getLogConfig() throws RemoteException {
        return logManager.getLogConfig();
    }

    @Override
    public void updateLogConfig(String config) throws RemoteException {
        logManager.updateLogConfig(config);
    }

    @Override
    public String getDatabaseMonitoringHtmlReport() throws RemoteException {
        return dbStorage.getDatabaseMonitoringHtmlReport();
    }

    @Override
    public String getDatabaseMonitoringHtmlDetailReport(String sqlid) throws RemoteException {
        return dbStorage.getDatabaseMonitoringHtmlDetailReport(sqlid);
    }

    @Override
    public String getDatabaseMonitoringRecommendationsReport(String sqlid) throws RemoteException {
        return dbStorage.getRecommendationsReport(sqlid);
    }

    @Override
    public Date getMonitoringDataMinDate() throws RemoteException {
        return monitoringDataAccessQueue.callAndWait(new MonitoringDataAwareCallable<Date>() {
            @Override
            public Date call() throws Exception {
                return monitoringDataAccesor.getMonitoringDataMinDate();
            }
        });
    }

    @Override
    public Date getMonitoringDataMaxDate() throws RemoteException {
        return monitoringDataAccessQueue.callAndWait(new MonitoringDataAwareCallable<Date>() {
            @Override
            public Date call() throws Exception {
                return monitoringDataAccesor.getMonitoringDataMaxDate();
            }
        });
    }

    @Override
    public <T> List<T> getList(final MonitoringDataFilter<T> filter, final Date from, final Date to, final long maxCount) throws RemoteException {
        return monitoringDataAccessQueue.callAndWait(new MonitoringDataAwareCallable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                return monitoringDataAccesor.getList(filter, from, to, maxCount);
            }
        });
    }

    @Override
    public <T, R extends Serializable> List<List<R>> createStatistic(final MonitoringDataFilter<T> filter,
            final List<StatisticCreator<T, R>> statisticCreators, final Date from, final Date to) throws RemoteException {
        return monitoringDataAccessQueue.callAndWait(new MonitoringDataAwareCallable<List<List<R>>>() {
            @Override
            public List<List<R>> call() throws Exception {
                return monitoringDataAccesor.createStatistic(filter, statisticCreators, from, to);
            }
        });
    }

    @Override
    public List<MonitoringDataProviderInfo> getMonitoringDataProviderInfos() {
        return monitoringDataProviderManager.getInfos();
    }

    @Override
    public void startMonitoringDataProvider(String name) throws RemoteException {
        final Optional<MonitoringDataProvider> provider = monitoringDataProviderManager.getProvider(name);
        if (provider.isPresent()) {
            provider.get().startProvider();
        }
    }

    @Override
    public void stopMonitoringDataProvider(String name) throws RemoteException {
        final Optional<MonitoringDataProvider> provider = monitoringDataProviderManager.getProvider(name);
        if (provider.isPresent()) {
            provider.get().stopProvider();
        }
    }

    @Override
    public MonitoringDataStorageInfo getMonitroingDataStorageInfo() {
        return monitoringDataAccessQueue.callAndWait(new MonitoringDataAwareCallable<MonitoringDataStorageInfo>() {
            @Override
            public MonitoringDataStorageInfo call() throws Exception {
                return monitoringDataAccesor.getMonitroingDataStorageInfo();
            }
        });
    }

}
