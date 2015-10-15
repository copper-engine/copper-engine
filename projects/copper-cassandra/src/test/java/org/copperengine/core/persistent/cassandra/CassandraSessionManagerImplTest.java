package org.copperengine.core.persistent.cassandra;

import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CassandraSessionManagerImplTest {

    @Test()
    public void test() {
        CassandraSessionManagerImpl cassandraSessionManagerImpl = new CassandraSessionManagerImpl(Collections.singletonList("localhost"), null, "copper");
        cassandraSessionManagerImpl.startup();
        cassandraSessionManagerImpl.shutdown();
    }

}
