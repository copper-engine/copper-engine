package org.copperengine.core.test.persistent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.copperengine.core.AbstractDependencyInjector;
import org.copperengine.core.DependencyInjector;
import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.db.utility.RetryingTransaction;
import org.copperengine.core.persistent.DataSourceFactory;
import org.copperengine.core.persistent.DatabaseDialect;
import org.copperengine.core.persistent.OracleDialect;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.test.DBMockAdapter;
import org.copperengine.core.test.DataHolder;
import org.copperengine.core.test.MockAdapter;
import org.copperengine.core.test.backchannel.BackChannelQueue;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;
import org.copperengine.ext.persistent.RdbmsEngineFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class TestContext implements AutoCloseable {

    protected final Map<String, Supplier<?>> suppliers = new HashMap<>();
    protected final Supplier<Properties> properties;
    protected final Supplier<MockAdapter> mockAdapter;
    protected final Supplier<RdbmsEngineFactory<DependencyInjector>> engineFactoryRed;
    protected final Supplier<DependencyInjector> dependencyInjector;
    protected final Supplier<ComboPooledDataSource> dataSource;
    protected final Supplier<BackChannelQueue> backChannelQueue;
    protected final Supplier<DataHolder> dataHolder;
    protected final Supplier<BatchingAuditTrail> auditTrail;
    protected final Supplier<DBMockAdapter> dbMockAdapter;

    public TestContext(final DataSourceType dataSourceType, final boolean cleanDB) {
        this(dataSourceType, cleanDB, "default", false);
    }

    public TestContext(final DataSourceType dataSourceType, final boolean cleanDB, final String engineId, final boolean multiEngineMode) {
        dbMockAdapter = Suppliers.memoize(new Supplier<DBMockAdapter>() {
            @Override
            public DBMockAdapter get() {
                return createDBMockAdapter();
            }
        });
        suppliers.put("dbMockAdapter", dbMockAdapter);

        auditTrail = Suppliers.memoize(new Supplier<BatchingAuditTrail>() {
            @Override
            public BatchingAuditTrail get() {
                return createBatchingAuditTrail();
            }
        });
        suppliers.put("auditTrail", auditTrail);

        properties = Suppliers.memoize(new Supplier<Properties>() {
            @Override
            public Properties get() {
                return createProperties();
            }
        });
        suppliers.put("properties", properties);

        dataHolder = Suppliers.memoize(new Supplier<DataHolder>() {
            @Override
            public DataHolder get() {
                return createDataHolder();
            }
        });
        suppliers.put("dataHolder", dataHolder);

        backChannelQueue = Suppliers.memoize(new Supplier<BackChannelQueue>() {
            @Override
            public BackChannelQueue get() {
                return createBackChannelQueue();
            }
        });
        suppliers.put("backChannelQueue", backChannelQueue);

        dataSource = Suppliers.memoize(new Supplier<ComboPooledDataSource>() {
            @Override
            public ComboPooledDataSource get() {
                ComboPooledDataSource ds = createDataSource(dataSourceType);
                if (cleanDB) {
                    cleanDB(ds);
                }
                return ds;
            }
        });
        suppliers.put("dataSource", dataSource);

        dependencyInjector = Suppliers.memoize(new Supplier<DependencyInjector>() {
            @Override
            public DependencyInjector get() {
                return createDependencyInjector();
            }
        });
        suppliers.put("dependencyInjector", dependencyInjector);

        mockAdapter = Suppliers.memoize(new Supplier<MockAdapter>() {
            @Override
            public MockAdapter get() {
                return createMockAdapter();
            }
        });
        suppliers.put("mockAdapter", mockAdapter);

        engineFactoryRed = Suppliers.memoize(new Supplier<RdbmsEngineFactory<DependencyInjector>>() {
            @Override
            public RdbmsEngineFactory<DependencyInjector> get() {
                return createRdbmsEngineFactory(engineId, multiEngineMode);
            }
        });
        suppliers.put("engineFactoryRed", engineFactoryRed);
    }

    protected DBMockAdapter createDBMockAdapter() {
        DBMockAdapter dbMockAdapter = new DBMockAdapter();
        dbMockAdapter.setDataSource(dataSource.get());
        dbMockAdapter.setEngine(getEngine());
        return dbMockAdapter;
    }

    protected BatchingAuditTrail createBatchingAuditTrail() {
        BatchingAuditTrail batchingAuditTrail = new BatchingAuditTrail();
        batchingAuditTrail.setDataSource(dataSource.get());
        batchingAuditTrail.setBatcher(engineFactoryRed.get().getBatcher());
        return batchingAuditTrail;
    }

    protected void cleanDB(DataSource ds) {
        try {
            new RetryingTransaction<Void>(ds) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_AUDIT_TRAIL_EVENT");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_WAIT");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_RESPONSE");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_QUEUE");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_WORKFLOW_INSTANCE");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_WORKFLOW_INSTANCE_ERROR");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_LOCK");
                    stmt.close();
                    return null;
                }
            }.run();
        } catch (Exception e) {
            throw new RuntimeException("cleanDB failed", e);
        }
    }

    private Statement createStatement(Connection con) throws SQLException {
        return con.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT);
    }

    protected Properties createProperties() {
        try {
            Properties p = new Properties();
            p.load(getClass().getResourceAsStream("/regtest.properties"));
            return p;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("failed to load properties", e);
        }
    }

    protected ComboPooledDataSource createDataSource(final DataSourceType dataSourceType) {
        ComboPooledDataSource ds;
        if (dataSourceType == DataSourceType.H2) {
            ds = DataSourceFactory.createH2Datasource();
        }
        else if (dataSourceType == DataSourceType.DerbyDB) {
            ds = DataSourceFactory.createDerbyDbDatasource();
        }
        else if (dataSourceType == DataSourceType.Oracle) {
            ds = DataSourceFactory.createOracleDatasource();
        }
        else if (dataSourceType == DataSourceType.MySQL) {
            ds = DataSourceFactory.createMySqlDatasource();
        }
        else if (dataSourceType == DataSourceType.Postgres) {
            ds = DataSourceFactory.createPostgresDatasource();
        }
        else {
            throw new IllegalArgumentException(dataSourceType.name());
        }
        return ds;
    }

    protected BackChannelQueue createBackChannelQueue() {
        return new BackChannelQueue();
    }

    protected DataHolder createDataHolder() {
        return new DataHolder();
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

    protected RdbmsEngineFactory<DependencyInjector> createRdbmsEngineFactory(final String engineId, final boolean multiEngineMode) {
        RdbmsEngineFactory<DependencyInjector> x = new RdbmsEngineFactory<DependencyInjector>(Collections.<String>emptyList()) {

            @Override
            protected DataSource createDataSource() {
                return TestContext.this.dataSource.get();
            }

            @Override
            protected DependencyInjector createDependencyInjector() {
                return TestContext.this.dependencyInjector.get();
            }

            @Override
            protected WorkflowRepository createWorkflowRepository() {
                FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
                repo.setSourceDirs(Arrays.asList(new String[] { "src/workflow/java" }));
                repo.setTargetDir("build/compiled_workflow_" + engineId);
                repo.setLoadNonWorkflowClasses(true);
                return repo;
            }

            @Override
            protected DatabaseDialect createDatabaseDialect() {
                DatabaseDialect x = super.createDatabaseDialect();
                if (multiEngineMode) {
                    if (x instanceof OracleDialect) {
                        ((OracleDialect) x).setMultiEngineMode(true);
                    }
                    else {
                        throw new IllegalArgumentException("MultiEngineMode only supported for Oracle");
                    }
                }
                return x;
            }

        };
        x.setEngineId(engineId);
        return x;
    }

    protected MockAdapter createMockAdapter() {
        MockAdapter x = new MockAdapter();
        x.setEngine(engineFactoryRed.get().getEngine());
        return x;
    }

    public void startup() {
        try {
            for (Supplier<?> s : suppliers.values()) {
                s.get();
            }
            mockAdapter.get().startup();
            dbMockAdapter.get().startup();
            engineFactoryRed.get().getEngine().startup();
            auditTrail.get().startup();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("startup failed", e);
        }
    }

    public void shutdown() {
        engineFactoryRed.get().destroyEngine();
        mockAdapter.get().shutdown();
        dbMockAdapter.get().shutdown();
        dataSource.get().close();
    }

    public MockAdapter getMockAdapter() {
        return mockAdapter.get();
    }

    public DataSource getDataSource() {
        return dataSource.get();
    }

    public PersistentScottyEngine getEngine() {
        return engineFactoryRed.get().getEngine();
    }

    public BackChannelQueue getBackChannelQueue() {
        return backChannelQueue.get();
    }

    public BatchingAuditTrail getAuditTrail() {
        return auditTrail.get();
    }

    public boolean isDbmsAvailable() {
        try {
            DataSource ds = getDataSource();
            ds.setLoginTimeout(10);
            ds.getConnection();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    public DataHolder getDataHolder() {
        return dataHolder.get();
    }

}
