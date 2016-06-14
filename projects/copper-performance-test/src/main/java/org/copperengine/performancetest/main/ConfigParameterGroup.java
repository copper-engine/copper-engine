package org.copperengine.performancetest.main;

public enum ConfigParameterGroup {
    common("common configuration parameters"),
    rdbms("configuration parameters used only for RDBMS, e.g. Oracle, MySQL"),
    cassandra("configuration parameters used only for Apache Cassandra DB"),
    latency("configuration parameters used only in the latency performance test"),
    throughput("configuration parameters used only in the throughput performance test");

    private final String description;

    private ConfigParameterGroup(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
