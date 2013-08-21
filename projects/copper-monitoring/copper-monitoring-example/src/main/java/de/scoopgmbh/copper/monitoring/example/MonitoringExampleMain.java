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
package de.scoopgmbh.copper.monitoring.example;

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

import com.mchange.v2.c3p0.ComboPooledDataSource;

import de.scoopgmbh.copper.CopperException;
import de.scoopgmbh.copper.EngineIdProviderBean;
import de.scoopgmbh.copper.audit.BatchingAuditTrail;
import de.scoopgmbh.copper.audit.CompressedBase64PostProcessor;
import de.scoopgmbh.copper.batcher.RetryingTxnBatchRunner;
import de.scoopgmbh.copper.batcher.impl.BatcherImpl;
import de.scoopgmbh.copper.common.DefaultProcessorPoolManager;
import de.scoopgmbh.copper.common.JdkRandomUUIDFactory;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.management.ProcessingEngineMXBean;
import de.scoopgmbh.copper.monitoring.LoggingStatisticCollector;
import de.scoopgmbh.copper.monitoring.core.CopperMonitoringService;
import de.scoopgmbh.copper.monitoring.core.data.MonitoringDataAccesor;
import de.scoopgmbh.copper.monitoring.core.data.MonitoringDataAdder;
import de.scoopgmbh.copper.monitoring.core.data.MonitoringDataStorage;
import de.scoopgmbh.copper.monitoring.example.adapter.BillAdapterImpl;
import de.scoopgmbh.copper.monitoring.example.monitoringprovider.GcDataProvider;
import de.scoopgmbh.copper.monitoring.example.util.SingleProzessInstanceUtil;
import de.scoopgmbh.copper.monitoring.server.CopperMonitorServiceSecurityProxy;
import de.scoopgmbh.copper.monitoring.server.DefaultCopperMonitoringService;
import de.scoopgmbh.copper.monitoring.server.DefaultLoginService;
import de.scoopgmbh.copper.monitoring.server.SpringRemotingServer;
import de.scoopgmbh.copper.monitoring.server.debug.WorkflowInstanceIntrospector;
import de.scoopgmbh.copper.monitoring.server.logging.LogbackConfigManager;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataAccessQueue;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;
import de.scoopgmbh.copper.monitoring.server.persistent.DerbyMonitoringDbDialect;
import de.scoopgmbh.copper.monitoring.server.persistent.MonitoringDbStorage;
import de.scoopgmbh.copper.monitoring.server.provider.MonitoringDataProviderManager;
import de.scoopgmbh.copper.monitoring.server.provider.MonitoringLogbackDataProvider;
import de.scoopgmbh.copper.monitoring.server.provider.SystemRessourceDataProvider;
import de.scoopgmbh.copper.monitoring.server.util.DerbyCleanDbUtil;
import de.scoopgmbh.copper.monitoring.server.wrapper.MonitoringAdapterProcessingEngine;
import de.scoopgmbh.copper.monitoring.server.wrapper.MonitoringDependencyInjector;
import de.scoopgmbh.copper.persistent.DatabaseDialect;
import de.scoopgmbh.copper.persistent.DerbyDbDialect;
import de.scoopgmbh.copper.persistent.OracleDialect;
import de.scoopgmbh.copper.persistent.PersistentPriorityProcessorPool;
import de.scoopgmbh.copper.persistent.PersistentProcessorPool;
import de.scoopgmbh.copper.persistent.PersistentScottyEngine;
import de.scoopgmbh.copper.persistent.ScottyDBStorage;
import de.scoopgmbh.copper.persistent.StandardJavaSerializer;
import de.scoopgmbh.copper.persistent.txn.CopperTransactionController;
import de.scoopgmbh.copper.util.PojoDependencyInjector;
import de.scoopgmbh.copper.wfrepo.FileBasedWorkflowRepository;

public class MonitoringExampleMain {
	
	private static final EngineIdProviderBean ENGINE_ID_PROVIDER = new EngineIdProviderBean("default");

	private class DatabaseData{
		public final DatabaseDialect databaseDialect;
		public final DataSource  dataSource ;
		public DatabaseData(DatabaseDialect databaseDialect, DataSource dataSource) {
			super();
			this.databaseDialect = databaseDialect;
			this.dataSource = dataSource;
		}
	}
	
	DatabaseData setupDerbyDatabase(WorkflowRepository workflowRepository, LoggingStatisticCollector runtimeStatisticsCollector){

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
	
	DatabaseData setupOracleDatabase(WorkflowRepository workflowRepository, LoggingStatisticCollector runtimeStatisticsCollector){

		ComboPooledDataSource datasource_oracle = new ComboPooledDataSource();
		try {
			datasource_oracle.setDriverClass("oracle.jdbc.OracleDriver");
			datasource_oracle.setJdbcUrl("jdbc:oracle:thin:COPPER2/COPPER2@localhost:1521:HM");
			datasource_oracle.setMinPoolSize(1);
			datasource_oracle.setMaxPoolSize(8);
			datasource_oracle.setConnectionTesterClassName("de.scoopgmbh.copper.db.utility.oracle.c3p0.OracleConnectionTester");
			datasource_oracle.setConnectionCustomizerClassName("de.scoopgmbh.copper.db.utility.oracle.c3p0.OracleConnectionCustomizer");
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
	
	
	public MonitoringExampleMain start(String[] args){
		LogManager.getRootLogger().setLevel(Level.INFO);
		
		FileBasedWorkflowRepository wfRepository = new FileBasedWorkflowRepository();
		wfRepository.setTargetDir("build/classes/test");

        System.out.println(new File(".").getAbsolutePath());
        File srcDir = new File("src/main/java"); //eclipse
        if (!srcDir.exists()){
            srcDir = new File("./projects/copper-monitoring/copper-monitoring-example/src/main/java");//idea
        }
        System.out.println(srcDir.getAbsolutePath());
		wfRepository.setSourceDirs(Arrays.asList(srcDir.getAbsolutePath()));
		wfRepository.start();
		//wfRepository.shutdown

		
		LoggingStatisticCollector runtimeStatisticsCollector = new LoggingStatisticCollector();
		runtimeStatisticsCollector.start();
		//statisticsCollector.shutdown();
		
		DatabaseData databaseData = setupDerbyDatabase(wfRepository,runtimeStatisticsCollector);
//		DatabaseData databaseData = setupOracleDatabase(wfRepository,runtimeStatisticsCollector);

		
		BatcherImpl batcher = new BatcherImpl(3);
		batcher.setStatisticsCollector(runtimeStatisticsCollector);
		
		@SuppressWarnings("rawtypes")
		RetryingTxnBatchRunner batchRunner = new RetryingTxnBatchRunner();
		batchRunner.setDataSource(databaseData.dataSource);
		batcher.setBatchRunner(batchRunner);
		batcher.startup();
		//batcherImpl.shutdown();
		
		CopperTransactionController txnController = new CopperTransactionController();
		txnController.setDataSource(databaseData.dataSource);
		
		ScottyDBStorage persistentdbStorage = new ScottyDBStorage();
		persistentdbStorage.setTransactionController(txnController);
		persistentdbStorage.setDialect(databaseData.databaseDialect);
		persistentdbStorage.setBatcher(batcher);
		persistentdbStorage.setCheckDbConsistencyAtStartup(true);
	
		
		PersistentPriorityProcessorPool persistentPriorityProcessorPool = new PersistentPriorityProcessorPool(PersistentProcessorPool.DEFAULT_POOL_ID,txnController);
		
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
		//persistentengine.shutdown();
		
		
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
		final MonitoringDataAccessQueue monitoringQueue = new MonitoringDataAccessQueue(new MonitoringDataAccesor(monitoringDataStorage),new MonitoringDataAdder(monitoringDataStorage));
		
		final MonitoringDataCollector monitoringDataCollector = new MonitoringDataCollector(monitoringQueue);
		MonitoringDependencyInjector monitoringDependencyInjector= new MonitoringDependencyInjector(dependyInjector, monitoringDataCollector);
		BillAdapterImpl billAdapterImpl = new BillAdapterImpl(monitoringDataCollector);
		billAdapterImpl.initWithEngine(new MonitoringAdapterProcessingEngine(billAdapterImpl,persistentengine,monitoringDataCollector));
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
		

		final SimpleAccountRealm realm = new SimpleAccountRealm();
		realm.addAccount("user1", "pass1");
		
		WorkflowInstanceIntrospector introspector = new WorkflowInstanceIntrospector(persistentdbStorage, wfRepository); 
		
		final MonitoringLogbackDataProvider monitoringLogbackDataProvider = new MonitoringLogbackDataProvider(monitoringDataCollector);
		final MonitoringDataProviderManager monitoringDataProviderManager = new MonitoringDataProviderManager(new SystemRessourceDataProvider(monitoringDataCollector),monitoringLogbackDataProvider,new GcDataProvider(monitoringDataCollector));
		monitoringDataProviderManager.startAll();
		CopperMonitoringService copperMonitoringService = new DefaultCopperMonitoringService(
				new MonitoringDbStorage(txnController,new DerbyMonitoringDbDialect(new StandardJavaSerializer(),new CompressedBase64PostProcessor(),auditTrail)),
				runtimeStatisticsCollector,
				engines,
				monitoringQueue, 
				true,
				introspector,
				new LogbackConfigManager(monitoringLogbackDataProvider),
				monitoringDataProviderManager);

		String host = (args.length > 0) ? args[0] : "localhost";
		int port = (args.length > 1) ? Integer.parseInt(args[1]) : 8080;
		new SpringRemotingServer(CopperMonitorServiceSecurityProxy.secure(copperMonitoringService)  ,port, host, new DefaultLoginService(realm)).start();
		
		return this;
	}
	
	public static void main(String[] args) {
		SingleProzessInstanceUtil.enforceSingleProzessInstance();
		new MonitoringExampleMain().start(args);
	}
	
	void cleanDB(DataSource ds) throws Exception {
		Connection connection=null;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			DerbyCleanDbUtil.dropSchema(connection.getMetaData(), "APP");
		} finally {
			if (connection!=null){
				connection.close();
			}
		}
	}

}
