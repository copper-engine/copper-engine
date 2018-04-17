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
package org.copperengine.core.persistent.cassandra;

/**
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

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

public class CassandraTest {

    private static final Logger logger = LoggerFactory.getLogger(CassandraTest.class);

    public static final int CASSANDRA_PORT = 9042;

    protected static UnitTestCassandraEngineFactory factory;

    @BeforeClass
    public synchronized static void setUpBeforeClass() throws Exception {
        if (factory == null) {
//            logger.info("Starting embedded cassandra...");
//            EmbeddedCassandraServerHelper.startEmbeddedCassandra("unittest-cassandra.yaml", "./build/cassandra");
//            Thread.sleep(100);
//            logger.info("Successfully started embedded cassandra.");

            final Cluster cluster = new Builder().addContactPoint("localhost").withPort(CASSANDRA_PORT).build();
//            final Session session = cluster.newSession();
//            session.execute("CREATE KEYSPACE copper WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");

            factory = new UnitTestCassandraEngineFactory(false);
            factory.setCassandraPort(CASSANDRA_PORT);
            try {
                factory.getEngine().startup();
            } catch (NoHostAvailableException e) {
                factory = null;
            }
        }
    }

}
