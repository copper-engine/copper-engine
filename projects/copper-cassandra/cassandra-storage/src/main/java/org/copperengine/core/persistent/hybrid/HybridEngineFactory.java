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
package org.copperengine.core.persistent.hybrid;

import java.util.List;

import org.copperengine.core.DependencyInjector;
import org.copperengine.core.persistent.ScottyDBStorageInterface;
import org.copperengine.core.persistent.txn.TransactionController;
import org.copperengine.ext.persistent.AbstractPersistentEngineFactory;
import org.slf4j.Logger;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public abstract class HybridEngineFactory<T extends DependencyInjector> extends AbstractPersistentEngineFactory<T> {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(HybridEngineFactory.class);

    protected final Supplier<TimeoutManager> timeoutManager;
    protected final Supplier<Storage> storage;

    public HybridEngineFactory(List<String> wfPackges) {
        super(wfPackges);

        timeoutManager = Suppliers.memoize(new Supplier<TimeoutManager>() {
            @Override
            public TimeoutManager get() {
                logger.info("Creating TimeoutManager...");
                return createTimeoutManager();
            }
        });
        storage = Suppliers.memoize(new Supplier<Storage>() {
            @Override
            public Storage get() {
                logger.info("Creating Storage...");
                return createStorage();
            }
        });
    }

    protected abstract Storage createStorage();

    @Override
    protected ScottyDBStorageInterface createDBStorage() {
        return new HybridDBStorage(serializer.get(), workflowRepository.get(), storage.get(), timeoutManager.get(), executorService.get());
    }

    @Override
    protected TransactionController createTransactionController() {
        return new HybridTransactionController();
    }

    protected TimeoutManager createTimeoutManager() {
        return new DefaultTimeoutManager().startup();
    }

    public void destroyEngine() {
        super.destroyEngine();

        timeoutManager.get().shutdown();
    }

}
