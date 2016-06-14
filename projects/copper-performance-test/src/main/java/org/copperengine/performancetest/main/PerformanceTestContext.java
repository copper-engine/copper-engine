package org.copperengine.performancetest.main;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.copperengine.core.AbstractDependencyInjector;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.DependencyInjector;
import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.batcher.RetryingTxnBatchRunner;
import org.copperengine.core.batcher.impl.BatcherImpl;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.JdkRandomUUIDFactory;
import org.copperengine.core.common.ProcessorPoolManager;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.monitoring.LoggingStatisticCollector;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.persistent.DatabaseDialect;
import org.copperengine.core.persistent.DerbyDbDialect;
import org.copperengine.core.persistent.H2Dialect;
import org.copperengine.core.persistent.MySqlDialect;
import org.copperengine.core.persistent.OracleDialect;
import org.copperengine.core.persistent.PersistentPriorityProcessorPool;
import org.copperengine.core.persistent.PersistentProcessorPool;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.PostgreSQLDialect;
import org.copperengine.core.persistent.ScottyDBStorage;
import org.copperengine.core.persistent.ScottyDBStorageInterface;
import org.copperengine.core.persistent.Serializer;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.copperengine.core.persistent.cassandra.CassandraSessionManagerImpl;
import org.copperengine.core.persistent.cassandra.CassandraStorage;
import org.copperengine.core.persistent.hybrid.DefaultTimeoutManager;
import org.copperengine.core.persistent.hybrid.HybridDBStorage;
import org.copperengine.core.persistent.hybrid.HybridTransactionController;
import org.copperengine.core.persistent.hybrid.StorageCache;
import org.copperengine.core.persistent.txn.CopperTransactionController;
import org.copperengine.core.persistent.txn.TransactionController;
import org.copperengine.core.util.Backchannel;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.ext.wfrepo.classpath.ClasspathWorkflowRepository;
import org.copperengine.performancetest.impl.MockAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PerformanceTestContext implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestContext.class);

    protected final Map<String, Supplier<?>> suppliers = new HashMap<>();
    protected final Supplier<Properties> props;
    protected final Supplier<MockAdapter> mockAdapter;
    protected final Supplier<DependencyInjector> dependencyInjector;
    protected final Supplier<Backchannel> backchannel;
    protected final Supplier<PersistentProcessingEngine> engine;
    protected final Supplier<WorkflowRepository> repo;
    protected final Supplier<LoggingStatisticCollector> statisticsCollector;
    protected final Supplier<EngineIdProvider> engineIdProvider;
    protected final Supplier<Serializer> serializer;
    protected final Supplier<ProcessorPoolManager<PersistentProcessorPool>> processorPoolManager;
    protected final Supplier<ConfigurationManager> configManager;
    protected TransactionController transactionController = null;
    private final List<Runnable> shutdownHooks = new ArrayList<>();

    public PerformanceTestContext() {
        configManager = Suppliers.memoize(new Supplier<ConfigurationManager>() {
            @Override
            public ConfigurationManager get() {
                return createConfigurationManager();
            }
        });
        suppliers.put("configManager", configManager);

        processorPoolManager = Suppliers.memoize(new Supplier<ProcessorPoolManager<PersistentProcessorPool>>() {
            @Override
            public ProcessorPoolManager<PersistentProcessorPool> get() {
                return createProcessorPoolManager();
            }
        });
        suppliers.put("processorPoolManager", processorPoolManager);

        serializer = Suppliers.memoize(new Supplier<Serializer>() {
            @Override
            public Serializer get() {
                return createSerializer();
            }
        });
        suppliers.put("serializer", serializer);

        engineIdProvider = Suppliers.memoize(new Supplier<EngineIdProvider>() {
            @Override
            public EngineIdProvider get() {
                return createEngineIdProvider();
            }
        });
        suppliers.put("engineIdProvider", engineIdProvider);

        statisticsCollector = Suppliers.memoize(new Supplier<LoggingStatisticCollector>() {
            @Override
            public LoggingStatisticCollector get() {
                return createStatisticsCollector();
            }
        });
        suppliers.put("statisticsCollector", statisticsCollector);

        repo = Suppliers.memoize(new Supplier<WorkflowRepository>() {
            @Override
            public WorkflowRepository get() {
                return createWorkflowRepository();
            }
        });
        suppliers.put("repo", repo);

        engine = Suppliers.memoize(new Supplier<PersistentProcessingEngine>() {
            @Override
            public PersistentProcessingEngine get() {
                return createPersistentProcessingEngine();
            }
        });
        suppliers.put("engine", engine);

        props = Suppliers.memoize(new Supplier<Properties>() {
            @Override
            public Properties get() {
                return createProperties();
            }
        });
        suppliers.put("props", props);

        mockAdapter = Suppliers.memoize(new Supplier<MockAdapter>() {
            @Override
            public MockAdapter get() {
                return createMockAdapter();
            }
        });
        suppliers.put("mockAdapter", mockAdapter);

        backchannel = Suppliers.memoize(new Supplier<Backchannel>() {
            @Override
            public Backchannel get() {
                return createBackchannel();
            }
        });
        suppliers.put("backchannel", backchannel);

        dependencyInjector = Suppliers.memoize(new Supplier<DependencyInjector>() {
            @Override
            public DependencyInjector get() {
                return createDependencyInjector();
            }
        });
        suppliers.put("dependencyInjector", dependencyInjector);

        startup();
    }

    protected ConfigurationManager createConfigurationManager() {
        return new ConfigurationManager(props.get());
    }

    protected ProcessorPoolManager<PersistentProcessorPool> createProcessorPoolManager() {
        return new DefaultProcessorPoolManager<PersistentProcessorPool>();
    }

    protected Serializer createSerializer() {
        StandardJavaSerializer serializer = new StandardJavaSerializer();
        boolean compression = configManager.get().getConfigBoolean(ConfigParameter.COMPRESSION);
        logger.debug("compression={}", compression);
        serializer.setCompress(compression);
        return serializer;
    }

    protected EngineIdProvider createEngineIdProvider() {
        return new EngineIdProviderBean("perftest");
    }

    protected LoggingStatisticCollector createStatisticsCollector() {
        LoggingStatisticCollector statCollector = new LoggingStatisticCollector();
        statCollector.setLoggingIntervalSec(10);
        statCollector.setResetAfterLogging(false);
        return statCollector;
    }

    protected WorkflowRepository createWorkflowRepository() {
        return new ClasspathWorkflowRepository("org.copperengine.performancetest.workflows");
    }

    protected DatabaseDialect createDialect(DataSource ds, WorkflowRepository wfRepository, EngineIdProvider engineIdProvider, RuntimeStatisticsCollector runtimeStatisticsCollector, Serializer serializer) {
        Connection c = null;
        try {
            c = ds.getConnection();
            String name = c.getMetaData().getDatabaseProductName();
            logger.info("Test database type is {}", name);
            if ("oracle".equalsIgnoreCase(name)) {
                OracleDialect dialect = new OracleDialect();
                dialect.setWfRepository(wfRepository);
                dialect.setEngineIdProvider(engineIdProvider);
                dialect.setMultiEngineMode(false);
                dialect.setRuntimeStatisticsCollector(runtimeStatisticsCollector);
                dialect.setSerializer(serializer);
                dialect.startup();
                return dialect;
            }
            if ("Apache Derby".equalsIgnoreCase(name)) {
                DerbyDbDialect dialect = new DerbyDbDialect();
                dialect.setDataSource(ds);
                dialect.setWfRepository(wfRepository);
                dialect.setRuntimeStatisticsCollector(runtimeStatisticsCollector);
                dialect.setSerializer(serializer);
                return dialect;
            }
            if ("H2".equalsIgnoreCase(name)) {
                H2Dialect dialect = new H2Dialect();
                dialect.setDataSource(ds);
                dialect.setWfRepository(wfRepository);
                dialect.setRuntimeStatisticsCollector(runtimeStatisticsCollector);
                dialect.setSerializer(serializer);
                return dialect;
            }
            if ("MySQL".equalsIgnoreCase(name)) {
                MySqlDialect dialect = new MySqlDialect();
                dialect.setWfRepository(wfRepository);
                dialect.setRuntimeStatisticsCollector(runtimeStatisticsCollector);
                dialect.setSerializer(serializer);
                return dialect;
            }
            if ("PostgreSQL".equalsIgnoreCase(name)) {
                PostgreSQLDialect dialect = new PostgreSQLDialect();
                dialect.setWfRepository(wfRepository);
                dialect.setRuntimeStatisticsCollector(runtimeStatisticsCollector);
                dialect.setSerializer(serializer);
                return dialect;
            }
            throw new Error("No dialect available for DBMS " + name);
        } catch (Exception e) {
            throw new CopperRuntimeException("Unable to create dialect", e);
        } finally {
            if (c != null)
                try {
                    c.close();
                } catch (SQLException e) {
                    logger.error("unable to close connection", e);
                }
        }
    }

    protected PersistentProcessingEngine createPersistentProcessingEngine() {
        ScottyDBStorageInterface dbStorageInterface = null;

        if (!isCassandraTest()) {
            final int batcherNumbOfThreads = configManager.get().getConfigInt(ConfigParameter.BATCHER_NUMB_OF_THREADS);
            logger.debug("Starting batcher with {} worker threads", batcherNumbOfThreads);

            final ComboPooledDataSource dataSource = DataSourceFactory.createDataSource(props.get());
            transactionController = new CopperTransactionController(dataSource);

            final BatcherImpl batcher = new BatcherImpl(batcherNumbOfThreads);
            batcher.setBatchRunner(new RetryingTxnBatchRunner<>(dataSource));
            batcher.setStatisticsCollector(statisticsCollector.get());
            batcher.startup();

            ScottyDBStorage dbStorage = new ScottyDBStorage();
            dbStorage.setBatcher(batcher);
            dbStorage.setCheckDbConsistencyAtStartup(false);
            dbStorage.setDialect(createDialect(dataSource, repo.get(), engineIdProvider.get(), statisticsCollector.get(), serializer.get()));
            dbStorage.setTransactionController(transactionController);
            dbStorageInterface = dbStorage;

            shutdownHooks.add(new Runnable() {
                @Override
                public void run() {
                    batcher.shutdown();
                    dataSource.close();
                }
            });

        }
        else {
            transactionController = new HybridTransactionController();

            final String cassandraHosts = props.get().getProperty(ConfigParameter.CASSANDRA_HOSTS.getKey());
            final CassandraSessionManagerImpl sessionManager = new CassandraSessionManagerImpl(Arrays.asList(cassandraHosts.split(",")), configManager.get().getConfigInteger(ConfigParameter.CASSANDRA_PORT), configManager.get().getConfigString(ConfigParameter.CASSANDRA_KEYSPACE));
            sessionManager.startup();

            final DefaultTimeoutManager timeoutManager = new DefaultTimeoutManager();
            timeoutManager.startup();

            final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            CassandraStorage storage = new CassandraStorage(sessionManager, pool, statisticsCollector.get());
            storage.setCreateSchemaOnStartup(true);

            HybridDBStorage dbStorage = new HybridDBStorage(serializer.get(), repo.get(), new StorageCache(storage), timeoutManager, pool);
            dbStorageInterface = dbStorage;

            shutdownHooks.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        timeoutManager.shutdown();
                        sessionManager.shutdown();
                        pool.shutdown();
                        pool.awaitTermination(5, TimeUnit.SECONDS);
                    }
                    catch (Exception e) {
                        logger.error("shutdown failed", e);
                    }
                }
            });
        }

        final int procPoolNumbOfThreads = configManager.get().getConfigInt(ConfigParameter.PROC_POOL_NUMB_OF_THREADS);
        logger.debug("Starting default processor pool with {} worker threads", procPoolNumbOfThreads);
        final List<PersistentProcessorPool> pools = new ArrayList<PersistentProcessorPool>();
        final PersistentPriorityProcessorPool pool = new PersistentPriorityProcessorPool(PersistentProcessorPool.DEFAULT_POOL_ID, transactionController, procPoolNumbOfThreads);
        pool.setDequeueBulkSize(configManager.get().getConfigInt(ConfigParameter.PROC_DEQUEUE_BULK_SIZE));
        pools.add(pool);
        processorPoolManager.get().setProcessorPools(pools);

        PersistentScottyEngine engine = new PersistentScottyEngine();
        engine.setWfRepository(repo.get());
        engine.setStatisticsCollector(statisticsCollector.get());
        engine.setEngineIdProvider(engineIdProvider.get());
        engine.setIdFactory(new JdkRandomUUIDFactory());
        engine.setProcessorPoolManager(processorPoolManager.get());
        engine.setDbStorage(dbStorageInterface);
        engine.setDependencyInjector(dependencyInjector.get());
        return engine;
    }

    protected DependencyInjector createDependencyInjector() {
        AbstractDependencyInjector dependencyInjector = new AbstractDependencyInjector() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            protected Object getBean(String beanId) {
                Supplier<?> supplier = suppliers.get(beanId);
                if (supplier == null) {
                    throw new RuntimeException("No supplier with id '" + beanId + "' found!");
                }
                else {
                    return supplier.get();
                }
            }
        };
        return dependencyInjector;
    }

    protected Properties createProperties() {
        try {
            Properties defaults = new Properties();
            logger.debug("Loading properties from 'performancetest.default.properties'...");
            defaults.load(DataSourceFactory.class.getResourceAsStream("/performancetest.default.properties"));

            Properties specific = new Properties();
            String username = System.getProperty("user.name", "undefined");
            InputStream is = DataSourceFactory.class.getResourceAsStream("/performancetest." + username + ".properties");
            if (is != null) {
                logger.info("Loading properties from 'performancetest." + username + ".properties'...");
                specific.load(is);
            }

            Properties p = new Properties();
            p.putAll(defaults);
            p.putAll(specific);
            p.putAll(System.getProperties());

            List<String> keys = new ArrayList<>();
            for (Object key : p.keySet()) {
                keys.add(key.toString());
            }
            Collections.sort(keys);
            for (String key : keys) {
                logger.debug("Property {}='{}'", key, p.getProperty(key));
            }
            return p;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("failed to load properties", e);
        }
    }

    protected Backchannel createBackchannel() {
        return new BackchannelDefaultImpl();
    }

    protected MockAdapter createMockAdapter() {
        int numberOfThreads = configManager.get().getConfigInt(ConfigParameter.MOCK_ADAPTER_NUMB_OF_THREADS);
        logger.debug("MockAdapter.numberOfThreads={}", numberOfThreads);
        MockAdapter x = new MockAdapter(numberOfThreads);
        x.setEngine(engine.get());
        return x;
    }

    public PersistentProcessingEngine getEngine() {
        return engine.get();
    }

    public void startup() {
        for (Supplier<?> s : suppliers.values()) {
            s.get();
        }
        mockAdapter.get().startup();
        statisticsCollector.get().start();
        engine.get().startup();
    };

    public void shutdown() {
        engine.get().shutdown();
        statisticsCollector.get().shutdown();
        mockAdapter.get().shutdown();
        for (Runnable r : shutdownHooks) {
            r.run();
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    public void registerBean(final String id, final Object bean) {
        suppliers.put(id, new Supplier<Object>() {
            @Override
            public Object get() {
                return bean;
            }
        });
    }

    public LoggingStatisticCollector getStatisticsCollector() {
        return statisticsCollector.get();
    }

    public Backchannel getBackchannel() {
        return backchannel.get();
    }

    public ProcessorPoolManager<PersistentProcessorPool> getProcessorPoolManager() {
        return processorPoolManager.get();
    }

    public TransactionController getTransactionController() {
        return transactionController;
    }

    public ConfigurationManager getConfigManager() {
        return configManager.get();
    }

    public boolean isCassandraTest() {
        final String cassandraHosts = props.get().getProperty(ConfigParameter.CASSANDRA_HOSTS.getKey());
        return cassandraHosts != null && !cassandraHosts.isEmpty();
    }

}
