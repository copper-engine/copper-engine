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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.shiro.realm.SimpleAccountRealm;

import de.scoopgmbh.copper.CopperException;
import de.scoopgmbh.copper.audit.BatchingAuditTrail;
import de.scoopgmbh.copper.audit.CompressedBase64PostProcessor;
import de.scoopgmbh.copper.batcher.RetryingTxnBatchRunner;
import de.scoopgmbh.copper.batcher.impl.BatcherImpl;
import de.scoopgmbh.copper.common.DefaultProcessorPoolManager;
import de.scoopgmbh.copper.common.JdkRandomUUIDFactory;
import de.scoopgmbh.copper.management.ProcessingEngineMXBean;
import de.scoopgmbh.copper.monitoring.LoggingStatisticCollector;
import de.scoopgmbh.copper.monitoring.core.CopperMonitoringService;
import de.scoopgmbh.copper.monitoring.example.adapter.BillAdapterImpl;
import de.scoopgmbh.copper.monitoring.example.util.SingleProzessInstanceUtil;
import de.scoopgmbh.copper.monitoring.server.CopperMonitorServiceSecurityProxy;
import de.scoopgmbh.copper.monitoring.server.DefaultCopperMonitoringService;
import de.scoopgmbh.copper.monitoring.server.DefaultLoginService;
import de.scoopgmbh.copper.monitoring.server.SpringRemotingServer;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataAccessQueue;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;
import de.scoopgmbh.copper.monitoring.server.persistent.DerbyMonitoringDbDialect;
import de.scoopgmbh.copper.monitoring.server.persistent.MonitoringDbStorage;
import de.scoopgmbh.copper.monitoring.server.util.DerbyCleanDbUtil;
import de.scoopgmbh.copper.monitoring.server.wrapper.MonitoringAdapterProcessingEngine;
import de.scoopgmbh.copper.monitoring.server.wrapper.MonitoringDependencyInjector;
import de.scoopgmbh.copper.persistent.DerbyDbDialect;
import de.scoopgmbh.copper.persistent.PersistentPriorityProcessorPool;
import de.scoopgmbh.copper.persistent.PersistentScottyEngine;
import de.scoopgmbh.copper.persistent.ScottyDBStorage;
import de.scoopgmbh.copper.persistent.StandardJavaSerializer;
import de.scoopgmbh.copper.persistent.txn.CopperTransactionController;
import de.scoopgmbh.copper.util.PojoDependencyInjector;
import de.scoopgmbh.copper.wfrepo.FileBasedWorkflowRepository;

public class MonitoringExampleMain {
	
	
	public MonitoringExampleMain start(){
		LogManager.getRootLogger().setLevel(Level.INFO);
		
		EmbeddedConnectionPoolDataSource40 datasource_default = new EmbeddedConnectionPoolDataSource40();
		datasource_default.setDatabaseName("./build/copperExampleDB;create=true");
		
		try {
			cleanDB(datasource_default);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
	
		FileBasedWorkflowRepository wfRepository = new FileBasedWorkflowRepository();
		wfRepository.setTargetDir("build/classes/test");
		wfRepository.setSourceDirs(Arrays.asList("src/main/java/de/scoopgmbh/copper/monitoring/example/workflow"));
		wfRepository.start();
		//wfRepository.shutdown
		

		LoggingStatisticCollector runtimeStatisticsCollector = new LoggingStatisticCollector();
		runtimeStatisticsCollector.start();
		//statisticsCollector.shutdown();
		
		final DerbyDbDialect dbDialect = new DerbyDbDialect();
		dbDialect.setWfRepository(wfRepository);
		dbDialect.setDataSource(datasource_default);
		dbDialect.startup();
		dbDialect.setRuntimeStatisticsCollector(runtimeStatisticsCollector);
		dbDialect.setDbBatchingLatencyMSec(0);
		
		BatcherImpl batcher = new BatcherImpl(3);
		batcher.setStatisticsCollector(runtimeStatisticsCollector);
		
		@SuppressWarnings("rawtypes")
		RetryingTxnBatchRunner batchRunner = new RetryingTxnBatchRunner();
		batchRunner.setDataSource(datasource_default);
		batcher.setBatchRunner(batchRunner);
		batcher.startup();
		//batcherImpl.shutdown();
		
		CopperTransactionController txnController = new CopperTransactionController();
		txnController.setDataSource(datasource_default);
		
		ScottyDBStorage persistentdbStorage = new ScottyDBStorage();
		persistentdbStorage.setTransactionController(txnController);
		persistentdbStorage.setDialect(dbDialect);
		persistentdbStorage.setBatcher(batcher);
		persistentdbStorage.setCheckDbConsistencyAtStartup(true);
	
		
		PersistentPriorityProcessorPool persistentPriorityProcessorPool = new PersistentPriorityProcessorPool("P#DEFAULT",txnController);
		
		PersistentScottyEngine persistentengine = new PersistentScottyEngine();
		persistentengine.setIdFactory(new JdkRandomUUIDFactory());
		persistentengine.setDbStorage(persistentdbStorage);
		persistentengine.setWfRepository(wfRepository);
		persistentengine.setStatisticsCollector(runtimeStatisticsCollector);
		
		DefaultProcessorPoolManager<PersistentPriorityProcessorPool> defaultProcessorPoolManager = new DefaultProcessorPoolManager<PersistentPriorityProcessorPool>();
		defaultProcessorPoolManager.setProcessorPools(Arrays.asList(persistentPriorityProcessorPool));
		defaultProcessorPoolManager.setEngine(persistentengine);
		
		persistentengine.setProcessorPoolManager(defaultProcessorPoolManager);
		//persistentengine.shutdown();
		
		
		BatchingAuditTrail auditTrail = new BatchingAuditTrail();
		auditTrail.setBatcher(batcher);
		auditTrail.setDataSource(datasource_default);
		auditTrail.setMessagePostProcessor(new CompressedBase64PostProcessor());
		try {
			auditTrail.startup();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
		PojoDependencyInjector dependyInjector = new PojoDependencyInjector();
		final MonitoringDataAccessQueue monitoringQueue = new MonitoringDataAccessQueue();
		final MonitoringDataCollector monitoringDataCollector = new MonitoringDataCollector(monitoringQueue);
		MonitoringDependencyInjector monitoringDependencyInjector= new MonitoringDependencyInjector(dependyInjector, monitoringDataCollector);
		BillAdapterImpl billAdapterImpl = new BillAdapterImpl();
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
		
		
		CopperMonitoringService copperMonitoringService = new DefaultCopperMonitoringService(
				new MonitoringDbStorage(txnController,new DerbyMonitoringDbDialect(new StandardJavaSerializer())),
				runtimeStatisticsCollector,
				engines,
				monitoringQueue, 
				true,
				new CompressedBase64PostProcessor());
	

		final SimpleAccountRealm realm = new SimpleAccountRealm();
		realm.addAccount("user1", "pass1");
		new SpringRemotingServer(CopperMonitorServiceSecurityProxy.secure(copperMonitoringService)  ,8080,"localhost", new DefaultLoginService(realm)).start();
		
		return this;
		
	}
	
	public static void main(String[] args) {
		SingleProzessInstanceUtil.enforceSingleProzessInstance();
		new MonitoringExampleMain().start();
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
