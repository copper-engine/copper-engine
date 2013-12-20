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
package org.copperengine.monitoring.example;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.spring.remoting.SecureRemoteInvocationExecutor;
import org.copperengine.core.CopperException;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.audit.CompressedBase64PostProcessor;
import org.copperengine.core.batcher.RetryingTxnBatchRunner;
import org.copperengine.core.batcher.impl.BatcherImpl;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.JdkRandomUUIDFactory;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.monitoring.LoggingStatisticCollector;
import org.copperengine.core.persistent.DatabaseDialect;
import org.copperengine.core.persistent.DerbyDbDialect;
import org.copperengine.core.persistent.OracleDialect;
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
import org.copperengine.monitoring.example.util.SingleProzessInstanceUtil;
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
import org.copperengine.monitoring.server.util.DerbyCleanDbUtil;
import org.copperengine.monitoring.server.wrapper.MonitoringAdapterProcessingEngine;
import org.copperengine.monitoring.server.wrapper.MonitoringDependencyInjector;
import org.springframework.remoting.support.DefaultRemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocationExecutor;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MonitoringExampleMain {

    private static final EngineIdProviderBean ENGINE_ID_PROVIDER = new EngineIdProviderBean("default");

    private class DatabaseData {
        public final DatabaseDialect databaseDialect;
        public final DataSource dataSource;

        public DatabaseData(DatabaseDialect databaseDialect, DataSource dataSource) {
            super();
            this.databaseDialect = databaseDialect;
            this.dataSource = dataSource;
        }
    }

    DatabaseData setupDerbyDatabase(WorkflowRepository workflowRepository, LoggingStatisticCollector runtimeStatisticsCollector) {

        EmbeddedConnectionPoolDataSource40 datasource_default = new EmbeddedConnectionPoolDataSource40();
        datasource_default.setDatabaseName("./build/copperExampleDB;create=true");

        try {
            cleanDB(datasource_default);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final DerbyDbDialect dbDialect = new DerbyDbDialect();
        dbDialect.setWfRepository(workflowRepository);
        dbDialect.setDataSource(datasource_default);
        dbDialect.startup();
        dbDialect.setRuntimeStatisticsCollector(runtimeStatisticsCollector);
        dbDialect.setDbBatchingLatencyMSec(0);

        return new DatabaseData(dbDialect, datasource_default);
    }

    DatabaseData setupOracleDatabase(WorkflowRepository workflowRepository, LoggingStatisticCollector runtimeStatisticsCollector) {

        ComboPooledDataSource datasource_oracle = new ComboPooledDataSource();
        try {
            datasource_oracle.setDriverClass("oracle.jdbc.OracleDriver");
            datasource_oracle.setJdbcUrl("jdbc:oracle:thin:COPPER2/COPPER2@localhost:1521:HM");
            datasource_oracle.setMinPoolSize(1);
            datasource_oracle.setMaxPoolSize(8);
            datasource_oracle.setConnectionTesterClassName("org.copperengine.core.db.utility.oracle.c3p0.OracleConnectionTester");
            datasource_oracle.setConnectionCustomizerClassName("org.copperengine.core.db.utility.oracle.c3p0.OracleConnectionCustomizer");
            datasource_oracle.setIdleConnectionTestPeriod(15);
        } catch (PropertyVetoException e1) {
            throw new RuntimeException(e1);
        }

        final OracleDialect oracleDialect = new OracleDialect();
        oracleDialect.setWfRepository(workflowRepository);
        oracleDialect.setRuntimeStatisticsCollector(runtimeStatisticsCollector);
        oracleDialect.setDbBatchingLatencyMSec(0);
        oracleDialect.setEngineIdProvider(ENGINE_ID_PROVIDER);
        oracleDialect.startup();
        return new DatabaseData(oracleDialect, datasource_oracle);
    }

    public MonitoringExampleMain start(String[] args) {
        boolean unsecure = Boolean.getBoolean("unsecureCopperMonitoring");
        LogManager.getRootLogger().setLevel(Level.INFO);
        System.out.println("Copper monitoring using " + (unsecure ? "un" : "") + "secure remote invocation.");

        FileBasedWorkflowRepository wfRepository = new FileBasedWorkflowRepository();
        wfRepository.setTargetDir("build/classes/test");

        System.out.println(new File(".").getAbsolutePath());
        File srcDir = new File("src/workflow/java"); // eclipse
        if (!srcDir.exists()) {
            srcDir = new File("./projects/copper-monitoring/copper-monitoring-example/src/workflow/java");// idea
        }
        System.out.println(srcDir.getAbsolutePath());
        wfRepository.setSourceDirs(Arrays.asList(srcDir.getAbsolutePath()));
        wfRepository.start();
        // wfRepository.shutdown

        LoggingStatisticCollector runtimeStatisticsCollector = new LoggingStatisticCollector();
        runtimeStatisticsCollector.start();
        // statisticsCollector.shutdown();

        DatabaseData databaseData = setupDerbyDatabase(wfRepository, runtimeStatisticsCollector);
        // DatabaseData databaseData = setupOracleDatabase(wfRepository,runtimeStatisticsCollector);

        BatcherImpl batcher = new BatcherImpl(3);
        batcher.setStatisticsCollector(runtimeStatisticsCollector);

        @SuppressWarnings("rawtypes")
        RetryingTxnBatchRunner batchRunner = new RetryingTxnBatchRunner();
        batchRunner.setDataSource(databaseData.dataSource);
        batcher.setBatchRunner(batchRunner);
        batcher.startup();
        // batcherImpl.shutdown();

        CopperTransactionController txnController = new CopperTransactionController();
        txnController.setDataSource(databaseData.dataSource);

        ScottyDBStorage persistentdbStorage = new ScottyDBStorage();
        persistentdbStorage.setTransactionController(txnController);
        persistentdbStorage.setDialect(databaseData.databaseDialect);
        persistentdbStorage.setBatcher(batcher);
        persistentdbStorage.setCheckDbConsistencyAtStartup(true);

        PersistentPriorityProcessorPool persistentPriorityProcessorPool = new PersistentPriorityProcessorPool(PersistentProcessorPool.DEFAULT_POOL_ID, txnController);

        PersistentScottyEngine persistentengine = new PersistentScottyEngine();
        persistentengine.setIdFactory(new JdkRandomUUIDFactory());
        persistentengine.setDbStorage(persistentdbStorage);
        persistentengine.setWfRepository(wfRepository);
        persistentengine.setEngineIdProvider(ENGINE_ID_PROVIDER);
        persistentengine.setStatisticsCollector(runtimeStatisticsCollector);

        DefaultProcessorPoolManager<PersistentPriorityProcessorPool> defaultProcessorPoolManager = new DefaultProcessorPoolManager<PersistentPriorityProcessorPool>();
        defaultProcessorPoolManager.setProcessorPools(Arrays.asList(persistentPriorityProcessorPool));
        defaultProcessorPoolManager.setEngine(persistentengine);

        persistentengine.setProcessorPoolManager(defaultProcessorPoolManager);
        // persistentengine.shutdown();

        BatchingAuditTrail auditTrail = new BatchingAuditTrail();
        auditTrail.setBatcher(batcher);
        auditTrail.setDataSource(databaseData.dataSource);
        auditTrail.setMessagePostProcessor(new CompressedBase64PostProcessor());
        try {
            auditTrail.startup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PojoDependencyInjector dependyInjector = new PojoDependencyInjector();
        MonitoringDataStorage monitoringDataStorage;
        try {
            monitoringDataStorage = new MonitoringDataStorage(File.createTempFile("test", ".tmp").getParentFile(), "copperMonitorLog");
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        final MonitoringDataAccessQueue monitoringQueue = new MonitoringDataAccessQueue(new MonitoringDataAccesor(monitoringDataStorage), new MonitoringDataAdder(monitoringDataStorage));

        final MonitoringDataCollector monitoringDataCollector = new MonitoringDataCollector(monitoringQueue);
        MonitoringDependencyInjector monitoringDependencyInjector = new MonitoringDependencyInjector(dependyInjector, monitoringDataCollector);
        BillAdapterImpl billAdapterImpl = new BillAdapterImpl(monitoringDataCollector);
        billAdapterImpl.initWithEngine(new MonitoringAdapterProcessingEngine(billAdapterImpl, persistentengine, monitoringDataCollector));
        dependyInjector.register("billAdapter", billAdapterImpl);
        dependyInjector.register("auditTrail", auditTrail);

        persistentengine.setDependencyInjector(monitoringDependencyInjector);
        persistentengine.startup();

        try {
            persistentengine.run("BillWorkflow", "");
        } catch (CopperException e) {
            throw new RuntimeException(e);
        }

        List<ProcessingEngineMXBean> engines = new ArrayList<ProcessingEngineMXBean>();
        engines.add(persistentengine);

        WorkflowInstanceIntrospector introspector = new WorkflowInstanceIntrospector(persistentdbStorage, wfRepository);

        final MonitoringLogbackDataProvider monitoringLogbackDataProvider = new MonitoringLogbackDataProvider(monitoringDataCollector);
        final MonitoringDataProviderManager monitoringDataProviderManager = new MonitoringDataProviderManager(new SystemRessourceDataProvider(monitoringDataCollector), monitoringLogbackDataProvider, new GcDataProvider(monitoringDataCollector));
        monitoringDataProviderManager.startAll();
        CopperMonitoringService copperMonitoringService = new DefaultCopperMonitoringService(
                new MonitoringDbStorage(txnController, new DerbyMonitoringDbDialect(new StandardJavaSerializer(), new CompressedBase64PostProcessor(), auditTrail)),
                runtimeStatisticsCollector,
                engines,
                monitoringQueue,
                true,
                introspector,
                new LogbackConfigManager(monitoringLogbackDataProvider),
                monitoringDataProviderManager);

        String host = (args.length > 0) ? args[0] : "localhost";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 8080;

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
        springRemotingServer.start();

        return this;
    }

    public static void main(String[] args) {
        SingleProzessInstanceUtil.enforceSingleProzessInstance();
        new MonitoringExampleMain().start(args);
    }

    void cleanDB(DataSource ds) throws Exception {
        Connection connection = null;
        try {
            connection = ds.getConnection();
            connection.setAutoCommit(false);
            DerbyCleanDbUtil.dropSchema(connection.getMetaData(), "APP");
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

}
