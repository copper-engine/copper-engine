/*
 * Copyright 2002-2014 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.monitoring.server.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.copperengine.management.BatcherMXBean;
import org.copperengine.management.DBStorageMXBean;
import org.copperengine.management.FileBasedWorkflowRepositoryMXBean;
import org.copperengine.management.PersistentPriorityProcessorPoolMXBean;
import org.copperengine.management.PersistentProcessingEngineMXBean;
import org.copperengine.management.ProcessingEngineMXBean;
import org.copperengine.management.ProcessorPoolMXBean;
import org.copperengine.management.ScottyDBStorageMXBean;
import org.copperengine.management.WorkflowRepositoryMXBean;
import org.copperengine.management.model.EngineType;
import org.copperengine.monitoring.core.data.MonitoringDataStorage;
import org.copperengine.monitoring.core.model.BatcherInfo;
import org.copperengine.monitoring.core.model.DependencyInjectorInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.ProcessorPoolInfo;
import org.copperengine.monitoring.core.model.StorageInfo;
import org.copperengine.monitoring.core.model.WorkflowRepositoryInfo;
import org.copperengine.monitoring.server.monitoring.MonitoringDataCollector;

public class ConfigurationDataProvider extends RepeatingMonitoringDataProviderBase {

    private final  List<ProcessingEngineMXBean> engines;
    private MonitoringDataProviderManager monitoringDataProviderManager;
    private final MonitoringDataStorage monitoringDataStorage;

    public ConfigurationDataProvider(MonitoringDataCollector monitoringDataCollector, List<ProcessingEngineMXBean> engines, MonitoringDataStorage monitoringDataStorage) {
        super(monitoringDataCollector);
        this.engines = engines;
        this.monitoringDataStorage = monitoringDataStorage;
    }

    @Override
    protected void provideData() {
        if (monitoringDataProviderManager!=null){
            monitoringDataCollector.submitConfiguration(createProcessingEngineInfos(),monitoringDataProviderManager.getInfos(),monitoringDataStorage.getMonitoringDataStorageInfo());
        }
    }

    private List<ProcessingEngineInfo> createProcessingEngineInfos() {
        ArrayList<ProcessingEngineInfo> result = new ArrayList<ProcessingEngineInfo>();
        for (ProcessingEngineMXBean engine : engines) {
            WorkflowRepositoryInfo workflowRepositoryInfo = new WorkflowRepositoryInfo();
            WorkflowRepositoryMXBean workflowRepository = engine.getWorkflowRepository();
            workflowRepositoryInfo.setName(workflowRepository.getDescription());
            workflowRepositoryInfo.setSrcPaths(Collections.<String>emptyList());
            workflowRepositoryInfo.setWorkflowRepositorTyp(WorkflowRepositoryInfo.WorkflowRepositorTyp.UNKOWN);
            if (workflowRepository instanceof FileBasedWorkflowRepositoryMXBean) {
                workflowRepositoryInfo.setWorkflowRepositorTyp(WorkflowRepositoryInfo.WorkflowRepositorTyp.FILE);
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
                        isPersistent ? ProcessorPoolInfo.ProcessorPoolTyp.PERSISTENT : ProcessorPoolInfo.ProcessorPoolTyp.TRANSIENT,
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
                    engine.getEngineType() == EngineType.persistent ? ProcessingEngineInfo.EngineTyp.PERSISTENT : ProcessingEngineInfo.EngineTyp.TRANSIENT,
                    engine.getEngineId(), workflowRepositoryInfo, dependencyInjectorInfo,
                    engine.getStatisticsCollectorType(), storageInfo, enginepools.toArray(new ProcessorPoolInfo[0]));
            result.add(engineInfo);

        }
        return result;
    }

    public void setMonitoringDataProviderManager(MonitoringDataProviderManager monitoringDataProviderManager){
        this.monitoringDataProviderManager = monitoringDataProviderManager;
    }

}
