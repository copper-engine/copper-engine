package org.copperengine.core.persistent.cassandra;

import com.datastax.driver.core.Session;

public interface CassandraSessionManager {
    public Session getSession();
}
