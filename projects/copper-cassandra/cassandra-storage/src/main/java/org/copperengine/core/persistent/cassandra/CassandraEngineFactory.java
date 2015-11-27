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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.DependencyInjector;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.hybrid.HybridEngineFactory;
import org.copperengine.core.persistent.hybrid.Storage;
import org.copperengine.core.persistent.hybrid.StorageCache;
import org.slf4j.Logger;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Utility class to create a {@link PersistentScottyEngine} using a cassandra cluster as underlying storage.
 * <p>
 * Usage is quite simple, e.g. using a SupplierDependencyInjector:
 * 
 * <pre>
 * CassandraEngineFactory&lt;SupplierDependencyInjector&gt; engineFactory = new CassandraEngineFactory&lt;SupplierDependencyInjector&gt;(Arrays.asList(&quot;package.of.copper.workflow.classes&quot;)) {
 *     &#064;Override
 *     protected SupplierDependencyInjector createDependencyInjector() {
 *         return new SupplierDependencyInjector();
 *     }
 * };
 * engineFactory.getEngine().startup();
 * </pre>
 * 
 * @author austermann
 *
 * @param <T>
 */
public abstract class CassandraEngineFactory<T extends DependencyInjector> extends HybridEngineFactory<T> {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CassandraEngineFactory.class);

    private String keyspace = "copper";
    private List<String> cassandraHosts = Collections.singletonList("localhost");
    private Integer cassandraPort = null;
    private boolean withCache = false;

    protected final Supplier<CassandraSessionManager> cassandraSessionManager;
    protected final Supplier<ScheduledExecutorService> scheduledExecutorService;

    public CassandraEngineFactory(List<String> wfPackges) {
        super(wfPackges);

        cassandraSessionManager = Suppliers.memoize(new Supplier<CassandraSessionManager>() {
            @Override
            public CassandraSessionManager get() {
                logger.info("Creating CassandraSessionManager...");
                return createCassandraSessionManager();
            }
        });

        scheduledExecutorService = Suppliers.memoize(new Supplier<ScheduledExecutorService>() {
            @Override
            public ScheduledExecutorService get() {
                logger.info("Creating ScheduledExecutorService...");
                return createScheduledExecutorService();
            }
        });
    }

    public void setCassandraHosts(List<String> cassandraHosts) {
        this.cassandraHosts = cassandraHosts;
    }

    public void setCassandraPort(Integer cassandraPort) {
        this.cassandraPort = cassandraPort;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public void setWithCache(boolean withCache) {
        this.withCache = withCache;
    }

    protected ScheduledExecutorService createScheduledExecutorService() {
        return Executors.newScheduledThreadPool(2);
    }

    protected Storage createStorage() {
        final CassandraStorage cs = new CassandraStorage(cassandraSessionManager.get(), executorService.get(), statisticCollector.get());
        if (withCache) {
            final StorageCache storageCache = new StorageCache(cs);
            scheduledExecutorService.get().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    storageCache.logCacheStats();
                }
            }, getStatLoggerIntervalSeconds(), getStatLoggerIntervalSeconds(), TimeUnit.SECONDS);
            return storageCache;
        }
        else {
            return cs;
        }
    }

    protected CassandraSessionManager createCassandraSessionManager() {
        CassandraSessionManagerImpl x = new CassandraSessionManagerImpl(cassandraHosts, cassandraPort, keyspace);
        x.startup();
        return x;
    }

    public void destroyEngine() {
        super.destroyEngine();

        cassandraSessionManager.get().shutdown();

        scheduledExecutorService.get().shutdown();
    }

    public CassandraSessionManager getCassandraSessionManager() {
        return cassandraSessionManager.get();
    }
}
