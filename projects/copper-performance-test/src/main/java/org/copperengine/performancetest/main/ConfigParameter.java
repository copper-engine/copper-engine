package org.copperengine.performancetest.main;

import java.util.ArrayList;
import java.util.List;

import org.copperengine.core.persistent.PersistentPriorityProcessorPool;
import org.copperengine.core.persistent.StandardJavaSerializer;

public enum ConfigParameter {

    // common configuration parameters
    PROC_POOL_NUMB_OF_THREADS("procPool.numberOfThreads", "Number of processor threads per processor pool", Integer.valueOf(Runtime.getRuntime().availableProcessors()), ConfigParameterGroup.common),
    PROC_DEQUEUE_BULK_SIZE("procPool.dequeueBulkSize", "Max. bulk size when fetching workflow instances from the underlying DB", Integer.valueOf(PersistentPriorityProcessorPool.DEFAULT_DEQUEUE_SIZE), ConfigParameterGroup.common),
    MOCK_ADAPTER_NUMB_OF_THREADS("mockAdapter.numberOfThreads", "Number of processor threads in adapter mock", Integer.valueOf(Runtime.getRuntime().availableProcessors()), ConfigParameterGroup.common),
    COMPRESSION("compression", "compress workflow instances in DB?", StandardJavaSerializer.DEFAULT_COMPRESS, ConfigParameterGroup.common),

    // configuration parameters used only for RDBMS, e.g. Oracle
    DS_MAX_POOL_SIZE("ds.maxPoolSize", "maximum size of the connection pool", Integer.valueOf(Runtime.getRuntime().availableProcessors() * 2), ConfigParameterGroup.rdbms),
    DS_MIN_POOL_SIZE("ds.minPoolSize", "minimum size of the connection pool", Integer.valueOf(Runtime.getRuntime().availableProcessors()), ConfigParameterGroup.rdbms),
    DS_DRIVER_CLASS("ds.driverClass", "jdbc driver class", ConfigParameterGroup.rdbms, null, "mandatory when testing RDBMS"),
    DS_PASSWORD("ds.password", "jdbc password", null, ConfigParameterGroup.rdbms),
    DS_USER("ds.user", "jdbc user", null, ConfigParameterGroup.rdbms),
    DS_JDBC_URL("ds.jdbcURL", "jdbc URL", null, ConfigParameterGroup.rdbms, "mandatory when testing RDBMS"),
    BATCHER_NUMB_OF_THREADS("batcher.numberOfThreads", "Number of DB batcher threads", Integer.valueOf(Runtime.getRuntime().availableProcessors()), ConfigParameterGroup.rdbms),

    // configuration parameters used only for Cassandra DB
    CASSANDRA_HOSTS("cassandra.hosts", "comma separated list of initial cassandra nodes", null, ConfigParameterGroup.cassandra, "mandatory when testing with Cassandra DB"),
    CASSANDRA_PORT("cassandra.port", "cassandra port", com.datastax.driver.core.ProtocolOptions.DEFAULT_PORT, ConfigParameterGroup.cassandra),
    CASSANDRA_KEYSPACE("cassandra.keyspace", "cassandra keyspace", "copper", ConfigParameterGroup.cassandra),

    // configuration parameters used only in the throughput performance test
    THROUGHPUTTEST_NUMBER_OF_WORKFLOW_INSTANCES("throughput.numberOfWfI", "Number of workflow instances to process in the test", 20000, ConfigParameterGroup.throughput),
    THROUGHPUTTEST_DATA_SIZE("throughput.dataSize", "Size of the data argument passed to the workflow instances", 50, ConfigParameterGroup.throughput),
    THROUGHPUTTEST_NUMBER_OF_INSERT_THREADS("throughput.numberOfInsertThreads", "Number of concurrent insert threads", 1, ConfigParameterGroup.throughput),
    THROUGHPUTTEST_BATCHS_SIZE("throughput.batchSize", "insert batch size", 100, ConfigParameterGroup.throughput),
    THROUGHPUTTEST_NUMBER_OF_EXTRA_PROC_POOLS("throughput.numberOfExtraProcPools", "number of extra processor pools", 0, ConfigParameterGroup.throughput);

    private ConfigParameter(String key, String description, Object defaultValue, ConfigParameterGroup grp) {
        this.key = key;
        this.description = description;
        this.grp = grp;
        this.mandatory = "optional";
        this.defaultValue = defaultValue;
    }

    private ConfigParameter(String key, String description, Object defaultValue, ConfigParameterGroup grp, String mandatory) {
        this.key = key;
        this.description = description;
        this.grp = grp;
        this.mandatory = mandatory;
        this.defaultValue = defaultValue;
    }

    private final String key;
    private final String description;
    private final ConfigParameterGroup grp;
    private final String mandatory;
    private final Object defaultValue;

    public String getDescription() {
        return description;
    }

    public ConfigParameterGroup getGrp() {
        return grp;
    }

    public String getKey() {
        return key;
    }

    public static List<ConfigParameter> all4group(ConfigParameterGroup grp) {
        List<ConfigParameter> rv = new ArrayList<>();
        for (ConfigParameter x : ConfigParameter.values()) {
            if (x.grp == grp) {
                rv.add(x);
            }
        }
        return rv;
    }

    public String getMandatory() {
        return mandatory;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

}
