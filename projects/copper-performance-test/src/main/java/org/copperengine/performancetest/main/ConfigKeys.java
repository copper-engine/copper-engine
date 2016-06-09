package org.copperengine.performancetest.main;

public interface ConfigKeys {

    // common configuration parameters
    public static final String PROC_POOL_NUMB_OF_THREADS = "procPool.numberOfThreads";
    public static final String MOCK_ADAPTER_NUMB_OF_THREADS = "mockAdapter.numberOfThreads";
    public static final String COMPRESSION = "compression";

    // configuration parameters used only for RDBMS, e.g. Oracle
    public static final String DS_MAX_POOL_SIZE = "ds.maxPoolSize";
    public static final String DS_MIN_POOL_SIZE = "ds.minPoolSize";
    public static final String DS_PREFERRED_TEST_QUERY = "ds.preferredTestQuery";
    public static final String DS_DRIVER_CLASS = "ds.driverClass";
    public static final String DS_PASSWORD = "ds.password";
    public static final String DS_USER = "ds.user";
    public static final String DS_JDBC_URL = "ds.jdbcURL";
    public static final String BATCHER_NUMB_OF_THREADS = "batcher.numberOfThreads";

    // configuration parameters used only for Cassandra DB
    public static final String CASSANDRA_HOSTS = "cassandra.hosts";
    public static final String CASSANDRA_PORT = "cassandra.port";
    public static final String CASSANDRA_KEYSPACE = "cassandra.keyspace";

    // configuration parameters used only in the throughput performance test
    public static final String THROUGHPUTTEST_NUMBER_OF_WORKFLOW_INSTANCES = "throughput.numberOfWfI";
    public static final String THROUGHPUTTEST_DATA_SIZE = "throughput.dataSize";
    public static final String THROUGHPUTTEST_NUMBER_OF_INSERT_THREADS = "throughput.numberOfInsertThreads";
    public static final String THROUGHPUTTEST_BATCHS_SIZE = "throughput.batchSize";
    public static final String THROUGHPUTTEST_NUMBER_OF_EXTRA_PROC_POOLS = "throughput.numberOfExtraProcPools";

}
