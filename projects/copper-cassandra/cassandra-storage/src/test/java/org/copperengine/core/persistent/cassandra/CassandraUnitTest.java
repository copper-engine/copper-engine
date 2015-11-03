package org.copperengine.core.persistent.cassandra;

public class CassandraUnitTest {

    protected static boolean skipTests() {
        return Boolean.getBoolean(Constants.SKIP_EXTERNAL_DB_TESTS_KEY);
    }

}
