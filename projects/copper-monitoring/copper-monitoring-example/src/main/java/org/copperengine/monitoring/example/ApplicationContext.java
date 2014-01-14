package org.copperengine.monitoring.example;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.spring.remoting.SecureRemoteInvocationExecutor;
import org.copperengine.core.CopperException;
import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.audit.CompressedBase64PostProcessor;
import org.copperengine.core.batcher.RetryingTxnBatchRunner;
import org.copperengine.core.batcher.impl.BatcherImpl;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.JdkRandomUUIDFactory;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.monitoring.LoggingStatisticCollector;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.persistent.DerbyDbDialect;
import org.copperengine.core.persistent.PersistentPriorityProcessorPool;
import org.copperengine.core.persistent.PersistentProcessorPool;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.ScottyDBStorage;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.copperengine.core.persistent.txn.CopperTransactionController;
import org.copperengine.core.util.PojoDependencyInjector;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;
import org.copperengine.management.ProcessingEngineMXBean;
import org.copperengine.monitoring.core.CopperMonitoringService;
import org.copperengine.monitoring.core.LoginService;
import org.copperengine.monitoring.core.data.MonitoringDataAccesor;
import org.copperengine.monitoring.core.data.MonitoringDataAdder;
import org.copperengine.monitoring.core.data.MonitoringDataStorage;
import org.copperengine.monitoring.example.adapter.BillAdapterImpl;
import org.copperengine.monitoring.example.monitoringprovider.GcDataProvider;
import org.copperengine.monitoring.server.CopperMonitorServiceDefaultProxy;
import org.copperengine.monitoring.server.CopperMonitorServiceSecurityProxy;
import org.copperengine.monitoring.server.DefaultCopperMonitoringService;
import org.copperengine.monitoring.server.DefaultLoginService;
import org.copperengine.monitoring.server.SecureLoginService;
import org.copperengine.monitoring.server.SpringRemotingServer;
import org.copperengine.monitoring.server.debug.WorkflowInstanceIntrospector;
import org.copperengine.monitoring.server.logging.LogbackConfigManager;
import org.copperengine.monitoring.server.monitoring.MonitoringDataAccessQueue;
import org.copperengine.monitoring.server.monitoring.MonitoringDataCollector;
import org.copperengine.monitoring.server.persistent.DerbyMonitoringDbDialect;
import org.copperengine.monitoring.server.persistent.MonitoringDbStorage;
import org.copperengine.monitoring.server.provider.MonitoringDataProviderManager;
import org.copperengine.monitoring.server.provider.MonitoringLogbackDataProvider;
import org.copperengine.monitoring.server.provider.SystemRessourceDataProvider;
import org.copperengine.monitoring.server.statisticcollector.MonitoringStatisticCollector;
import org.copperengine.monitoring.server.statisticcollector.MultipleStatistikCollector;
import org.copperengine.monitoring.server.wrapper.MonitoringAdapterProcessingEngine;
import org.copperengine.monitoring.server.wrapper.MonitoringDependencyInjector;
import org.springframework.remoting.support.DefaultRemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocationExecutor;

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
public class ApplicationContext {

    private MultipleStatistikCollector statistikCollector;
    private PojoDependencyInjector dependyInjector;
    private PersistentScottyEngine persistentengine;
    private BatchingAuditTrail auditTrail;
    private ScottyDBStorage persistentdbStorage;
    private FileBasedWorkflowRepository wfRepository;
    private CopperTransactionController txnController;
    private LoggingStatisticCollector loggingStatisticsCollector;
    private WorkflowInstanceIntrospector workflowInstanceIntrospector;

    private MonitoringLogbackDataProvider monitoringLogbackDataProvider;
    private MonitoringDataProviderManager monitoringDataProviderManager;
    private MonitoringDataAccessQueue monitoringQueue;

    private void createCopperCore(){
        wfRepository = new FileBasedWorkflowRepository();
        wfRepository.setTargetDir("build/classes/test");

        File srcDir = new File("src/workflow/java"); // eclipse
        if (!srcDir.exists()) {
            srcDir = new File("./projects/copper-monitoring/copper-monitoring-example/src/workflow/java");// idea
        }
        wfRepository.setSourceDirs(Arrays.asList(srcDir.getAbsolutePath()));
        wfRepository.start();

        loggingStatisticsCollector = new LoggingStatisticCollector();
        loggingStatisticsCollector.start();
        statistikCollector = new MultipleStatistikCollector(loggingStatisticsCollector);


        DatabaseUtil databaseData = setupDatabase(wfRepository, statistikCollector);

        BatcherImpl batcher = new BatcherImpl(3);
        batcher.setStatisticsCollector(statistikCollector);

        @SuppressWarnings("rawtypes")
        RetryingTxnBatchRunner batchRunner = new RetryingTxnBatchRunner();
        batchRunner.setDataSource(databaseData.dataSource);
        batcher.setBatchRunner(batchRunner);
        batcher.startup();

        txnController = new CopperTransactionController();
        txnController.setDataSource(databaseData.dataSource);

        persistentdbStorage = new ScottyDBStorage();
        persistentdbStorage.setTransactionController(txnController);
        persistentdbStorage.setDialect(databaseData.databaseDialect);
        persistentdbStorage.setBatcher(batcher);
        persistentdbStorage.setCheckDbConsistencyAtStartup(true);

        PersistentPriorityProcessorPool persistentPriorityProcessorPool = new PersistentPriorityProcessorPool(PersistentProcessorPool.DEFAULT_POOL_ID, txnController);

        persistentengine = new PersistentScottyEngine();
        persistentengine.setIdFactory(new JdkRandomUUIDFactory());
        persistentengine.setDbStorage(persistentdbStorage);
        persistentengine.setWfRepository(wfRepository);
        persistentengine.setEngineIdProvider(getEngineIdProvider());
        persistentengine.setStatisticsCollector(statistikCollector);

        DefaultProcessorPoolManager<PersistentPriorityProcessorPool> defaultProcessorPoolManager = new DefaultProcessorPoolManager<PersistentPriorityProcessorPool>();
        defaultProcessorPoolManager.setProcessorPools(Arrays.asList(persistentPriorityProcessorPool));
        defaultProcessorPoolManager.setEngine(persistentengine);

        persistentengine.setProcessorPoolManager(defaultProcessorPoolManager);

        auditTrail = new BatchingAuditTrail();
        auditTrail.setBatcher(batcher);
        auditTrail.setDataSource(databaseData.dataSource);
        auditTrail.setMessagePostProcessor(new CompressedBase64PostProcessor());
        try {
            auditTrail.startup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        dependyInjector = new PojoDependencyInjector();
    }

    private void createMonitoring(){

        MonitoringDataStorage monitoringDataStorage;
        try {
            monitoringDataStorage = new MonitoringDataStorage(File.createTempFile("test", ".tmp").getParentFile(), "copperMonitorLog");
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        monitoringQueue = new MonitoringDataAccessQueue(new MonitoringDataAccesor(monitoringDataStorage), new MonitoringDataAdder(monitoringDataStorage));

        final MonitoringDataCollector monitoringDataCollector = new MonitoringDataCollector(monitoringQueue);
        MonitoringDependencyInjector monitoringDependencyInjector = new MonitoringDependencyInjector(dependyInjector, monitoringDataCollector);
        BillAdapterImpl billAdapterImpl = new BillAdapterImpl(monitoringDataCollector);
        billAdapterImpl.initWithEngine(new MonitoringAdapterProcessingEngine(billAdapterImpl, persistentengine, monitoringDataCollector));
        dependyInjector.register("billAdapter", billAdapterImpl);
        dependyInjector.register("auditTrail", auditTrail);
        statistikCollector.addStatisticsCollector(new MonitoringStatisticCollector(monitoringDataCollector));

        persistentengine.setDependencyInjector(monitoringDependencyInjector);
        persistentengine.startup();

        try {
            persistentengine.run("BillWorkflow", "");
        } catch (CopperException e) {
            throw new RuntimeException(e);
        }

        workflowInstanceIntrospector = new WorkflowInstanceIntrospector(persistentdbStorage, wfRepository);

        monitoringLogbackDataProvider = new MonitoringLogbackDataProvider(monitoringDataCollector);
        monitoringDataProviderManager = new MonitoringDataProviderManager(new SystemRessourceDataProvider(monitoringDataCollector), monitoringLogbackDataProvider, new GcDataProvider(monitoringDataCollector));
        monitoringDataProviderManager.startAll();
    }

    public SpringRemotingServer createServer(String host, int port, boolean unsecure){
        createCopperCore();
        createMonitoring();

        CopperMonitoringService copperMonitoringService = new DefaultCopperMonitoringService(
                new MonitoringDbStorage(txnController, new DerbyMonitoringDbDialect(new StandardJavaSerializer(), new CompressedBase64PostProcessor(), auditTrail)),
                /*loggingStatisticsCollector*/null,
                Arrays.<ProcessingEngineMXBean>asList(persistentengine),
                monitoringQueue,
                true,
                workflowInstanceIntrospector,
                new LogbackConfigManager(monitoringLogbackDataProvider),
                monitoringDataProviderManager);

        CopperMonitoringService monitoringService;
        LoginService loginService;
        RemoteInvocationExecutor remoteInvocationExecutor;
        if (unsecure) {
            monitoringService = CopperMonitorServiceDefaultProxy.getServiceProxy(copperMonitoringService);
            loginService = new DefaultLoginService();
            remoteInvocationExecutor = new DefaultRemoteInvocationExecutor();
        } else {
            monitoringService = CopperMonitorServiceSecurityProxy.secure(copperMonitoringService);
            final SimpleAccountRealm realm = new SimpleAccountRealm();
            realm.addAccount("user1", "pass1");
            loginService = new SecureLoginService(realm);
            remoteInvocationExecutor = new SecureRemoteInvocationExecutor();
        }
        SpringRemotingServer springRemotingServer = new SpringRemotingServer(monitoringService, port, host, loginService);
        springRemotingServer.setRemoteInvocationExecutor(remoteInvocationExecutor);
        return springRemotingServer;
    }

    EngineIdProvider engineIdProvider = new EngineIdProviderBean("default");
    protected EngineIdProvider getEngineIdProvider(){
        return engineIdProvider;
    }

    protected DatabaseUtil setupDatabase(WorkflowRepository workflowRepository, RuntimeStatisticsCollector runtimeStatisticsCollector) {
        EmbeddedConnectionPoolDataSource40 datasource_default = new EmbeddedConnectionPoolDataSource40();
        datasource_default.setDatabaseName("./build/copperExampleDB;create=true");

        final DerbyDbDialect dbDialect = new DerbyDbDialect();
        final DatabaseUtil databaseData = new DatabaseUtil(dbDialect, datasource_default);
        databaseData.cleanDearbyDB();

        dbDialect.setWfRepository(workflowRepository);
        dbDialect.setDataSource(datasource_default);
        dbDialect.startup();
        dbDialect.setRuntimeStatisticsCollector(runtimeStatisticsCollector);
        dbDialect.setDbBatchingLatencyMSec(0);

        return databaseData;
    }

}
