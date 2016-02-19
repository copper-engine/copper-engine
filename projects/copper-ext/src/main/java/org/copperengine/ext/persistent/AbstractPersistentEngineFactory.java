/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.ext.persistent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.copperengine.core.DependencyInjector;
import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.IdFactory;
import org.copperengine.core.common.JdkRandomUUIDFactory;
import org.copperengine.core.common.ProcessorPoolManager;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.monitoring.LoggingStatisticCollector;
import org.copperengine.core.persistent.PersistentPriorityProcessorPool;
import org.copperengine.core.persistent.PersistentProcessorPool;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.ScottyDBStorageInterface;
import org.copperengine.core.persistent.Serializer;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.copperengine.core.persistent.txn.TransactionController;
import org.copperengine.ext.wfrepo.classpath.ClasspathWorkflowRepository;
import org.slf4j.Logger;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public abstract class AbstractPersistentEngineFactory<T extends DependencyInjector> {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractPersistentEngineFactory.class);

    private int statLoggerIntervalSeconds = 60;
    private String engineId = "default";
    private List<String> wfPackges;

    protected final Supplier<PersistentScottyEngine> engine;
    protected final Supplier<ExecutorService> executorService;
    protected final Supplier<LoggingStatisticCollector> statisticCollector;
    protected final Supplier<WorkflowRepository> workflowRepository;
    protected final Supplier<Serializer> serializer;
    protected final Supplier<T> dependencyInjector;
    protected final Supplier<EngineIdProvider> engineIdProvider;
    protected final Supplier<IdFactory> idFactory;
    protected final Supplier<ProcessorPoolManager<PersistentProcessorPool>> processorPoolManager;
    protected final Supplier<ScottyDBStorageInterface> dbStorage;
    protected final Supplier<TransactionController> transactionController;

    public AbstractPersistentEngineFactory(List<String> wfPackges) {
        this.wfPackges = wfPackges;

        processorPoolManager = Suppliers.memoize(new Supplier<ProcessorPoolManager<PersistentProcessorPool>>() {
            @Override
            public ProcessorPoolManager<PersistentProcessorPool> get() {
                logger.info("Creating ProcessorPoolManager...");
                return createProcessorPoolManager();
            }
        });
        dbStorage = Suppliers.memoize(new Supplier<ScottyDBStorageInterface>() {
            @Override
            public ScottyDBStorageInterface get() {
                logger.info("Creating DBStorage...");
                return createDBStorage();
            }
        });
        executorService = Suppliers.memoize(new Supplier<ExecutorService>() {
            @Override
            public ExecutorService get() {
                logger.info("Creating ExecutorService...");
                return createExecutorService();
            }
        });
        statisticCollector = Suppliers.memoize(new Supplier<LoggingStatisticCollector>() {
            @Override
            public LoggingStatisticCollector get() {
                logger.info("Creating LoggingStatisticCollector...");
                return createStatisticsLogger();
            }
        });
        workflowRepository = Suppliers.memoize(new Supplier<WorkflowRepository>() {
            @Override
            public WorkflowRepository get() {
                logger.info("Creating WorkflowRepository...");
                return createWorkflowRepository();
            }
        });
        serializer = Suppliers.memoize(new Supplier<Serializer>() {
            @Override
            public Serializer get() {
                logger.info("Creating Serializer...");
                return createSerializer();
            }
        });
        engine = Suppliers.memoize(new Supplier<PersistentScottyEngine>() {
            @Override
            public PersistentScottyEngine get() {
                logger.info("Creating PersistentScottyEngine...");
                return createPersistentScottyEngine();
            }
        });
        dependencyInjector = Suppliers.memoize(new Supplier<T>() {
            @Override
            public T get() {
                logger.info("Creating DependencyInjector...");
                return createDependencyInjector();
            }
        });
        engineIdProvider = Suppliers.memoize(new Supplier<EngineIdProvider>() {
            @Override
            public EngineIdProvider get() {
                logger.info("Creating EngineIdProvider...");
                return createEngineIdProvider();
            }
        });
        idFactory = Suppliers.memoize(new Supplier<IdFactory>() {
            @Override
            public IdFactory get() {
                logger.info("Creating IdFactory...");
                return createIdFactory();
            }
        });
        transactionController = Suppliers.memoize(new Supplier<TransactionController>() {
            @Override
            public TransactionController get() {
                logger.info("Creating TransactionController...");
                return createTransactionController();
            }
        });
    }

    protected abstract TransactionController createTransactionController();

    protected abstract ScottyDBStorageInterface createDBStorage();

    protected abstract T createDependencyInjector();

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    public void setStatLoggerIntervalSeconds(int statLoggerIntervalSeconds) {
        this.statLoggerIntervalSeconds = statLoggerIntervalSeconds;
    }

    protected int getStatLoggerIntervalSeconds() {
        return statLoggerIntervalSeconds;
    }

    protected ProcessorPoolManager<PersistentProcessorPool> createProcessorPoolManager() {
        PersistentPriorityProcessorPool ppool = new PersistentPriorityProcessorPool(PersistentProcessorPool.DEFAULT_POOL_ID, transactionController.get(), Runtime.getRuntime().availableProcessors());
        ppool.setEmptyQueueWaitMSec(2);
        ppool.setDequeueBulkSize(50);
        List<PersistentProcessorPool> pools = new ArrayList<PersistentProcessorPool>();
        pools.add(ppool);
        DefaultProcessorPoolManager<PersistentProcessorPool> processorPoolManager = new DefaultProcessorPoolManager<PersistentProcessorPool>();
        processorPoolManager.setProcessorPools(pools);
        return processorPoolManager;
    }

    protected IdFactory createIdFactory() {
        return new JdkRandomUUIDFactory();
    }

    protected EngineIdProvider createEngineIdProvider() {
        return new EngineIdProviderBean(engineId);
    }

    protected PersistentScottyEngine createPersistentScottyEngine() {
        final PersistentScottyEngine engine = new PersistentScottyEngine();
        engine.setDbStorage(dbStorage.get());
        engine.setWfRepository(workflowRepository.get());
        engine.setEngineIdProvider(engineIdProvider.get());
        engine.setIdFactory(idFactory.get());
        engine.setProcessorPoolManager(processorPoolManager.get());
        engine.setDependencyInjector(dependencyInjector.get());
        return engine;
    }

    protected Serializer createSerializer() {
        return new StandardJavaSerializer();
    }

    protected WorkflowRepository createWorkflowRepository() {
        return new ClasspathWorkflowRepository(wfPackges);
    }

    protected ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    protected LoggingStatisticCollector createStatisticsLogger() {
        LoggingStatisticCollector statisticCollector = new LoggingStatisticCollector();
        statisticCollector.setLoggingIntervalSec(statLoggerIntervalSeconds);
        statisticCollector.start();
        return statisticCollector;
    }

    public void destroyEngine() {
        engine.get().shutdown();

        executorService.get().shutdown();

        statisticCollector.get().shutdown();
    }

    public PersistentScottyEngine getEngine() {
        return engine.get();
    }

    public DependencyInjector getDependencyInjector() {
        return dependencyInjector.get();
    }
}
