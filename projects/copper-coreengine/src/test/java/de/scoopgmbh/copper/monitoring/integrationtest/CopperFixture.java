package de.scoopgmbh.copper.monitoring.integrationtest;

import java.util.Arrays;
import java.util.Date;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40;

import de.scoopgmbh.copper.audit.BatchingAuditTrail;
import de.scoopgmbh.copper.audit.CompressedBase64PostProcessor;
import de.scoopgmbh.copper.batcher.RetryingTxnBatchRunner;
import de.scoopgmbh.copper.batcher.impl.BatcherImpl;
import de.scoopgmbh.copper.common.DefaultProcessorPoolManager;
import de.scoopgmbh.copper.common.JdkRandomUUIDFactory;
import de.scoopgmbh.copper.monitoring.DefaultMonitoringDataCollector;
import de.scoopgmbh.copper.monitoring.LoggingMonitoringView;
import de.scoopgmbh.copper.monitoring.MonitoringEventQueue;
import de.scoopgmbh.copper.monitoring.RmiMonitoringView;
import de.scoopgmbh.copper.persistent.DerbyDbDialect;
import de.scoopgmbh.copper.persistent.PersistentPriorityProcessorPool;
import de.scoopgmbh.copper.persistent.PersistentScottyEngine;
import de.scoopgmbh.copper.persistent.ScottyDBStorage;
import de.scoopgmbh.copper.persistent.txn.CopperTransactionController;
import de.scoopgmbh.copper.spring.SpringDependencyInjector;
import de.scoopgmbh.copper.wfrepo.FileBasedWorkflowRepository;

public class CopperFixture {
	
	
	public CopperFixture start(){
		
		EmbeddedConnectionPoolDataSource40 datasource_default = new EmbeddedConnectionPoolDataSource40();
		datasource_default.setDatabaseName("./build/copperExampleDB;create=true");
		
		MonitoringEventQueue monitoringQueue = new MonitoringEventQueue();
		DefaultMonitoringDataCollector monitoringDataCollector = new DefaultMonitoringDataCollector(monitoringQueue);
		
		FileBasedWorkflowRepository wfRepository = new FileBasedWorkflowRepository();
		wfRepository.setTargetDir("build/classes/test");
		wfRepository.setSourceDirs(Arrays.asList("src/test/java/de/scoopgmbh/copper/monitoring/integrationtest/workflow"));
		wfRepository.start();
		//wfRepository.shutdown
		

		//statisticsCollector.shutdown();
		
		final DerbyDbDialect dbDialect = new DerbyDbDialect();
		dbDialect.setWfRepository(wfRepository);
		dbDialect.setMonitoringDataCollector(monitoringDataCollector);
		dbDialect.setDataSource(datasource_default);
		dbDialect.startup();
		
		BatcherImpl batcher = new BatcherImpl(3);
		batcher.setMonitoringDataCollector(monitoringDataCollector);
		
		RetryingTxnBatchRunner<?,?> batchRunner = new RetryingTxnBatchRunner<>();
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
		persistentPriorityProcessorPool.setMonitoringDataCollector(monitoringDataCollector);
		
		PersistentScottyEngine persistentengine = new PersistentScottyEngine();
		persistentengine.setDependencyInjector(new SpringDependencyInjector());		
		persistentengine.setIdFactory(new JdkRandomUUIDFactory());
		persistentengine.setDbStorage(persistentdbStorage);
		persistentengine.setWfRepository(wfRepository);
		persistentengine.setMonitoringDataCollector(monitoringDataCollector);
		
		DefaultProcessorPoolManager<PersistentPriorityProcessorPool> defaultProcessorPoolManager = new DefaultProcessorPoolManager<PersistentPriorityProcessorPool>();
		defaultProcessorPoolManager.setProcessorPools(Arrays.asList(persistentPriorityProcessorPool));
		defaultProcessorPoolManager.setEngine(persistentengine);
		
		persistentengine.setProcessorPoolManager(defaultProcessorPoolManager);
		persistentengine.startup();
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
		auditTrail.asynchLog(1, new Date(), "", "", "", "", "", "detail", "Text");
		
		new LoggingMonitoringView(monitoringQueue, 5);
		RmiMonitoringView rmiMonitoringView = new RmiMonitoringView(monitoringQueue,persistentdbStorage);
		rmiMonitoringView.setAuditrailMessagePostProcessor(new CompressedBase64PostProcessor());
		
	
		
		return this;
		
	}
	
	public static void main(String[] args) {
		new CopperFixture().start();
	}

}
