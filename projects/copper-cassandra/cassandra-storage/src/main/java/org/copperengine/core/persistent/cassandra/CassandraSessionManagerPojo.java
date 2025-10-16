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


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CassandraSessionManagerPojo implements CassandraSessionManager {

    private final Session session;
    private final Cluster cluster;

    public CassandraSessionManagerPojo(final Session session, final Cluster cluster) {
        if (session == null)
            throw new IllegalArgumentException("session");
        if (cluster == null)
            throw new IllegalArgumentException("cluster");
        this.session = session;
        this.cluster = cluster;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public Cluster getCluster() {
        return cluster;
    }

}
