/**
 * Copyright 2002-2017 SCOOP Software GmbH
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
package org.copperengine.regtest.test;

import java.util.HashMap;
import java.util.Map;

import org.copperengine.core.AbstractDependencyInjector;
import org.copperengine.core.DependencyInjector;
import org.copperengine.core.ProcessingEngine;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.copperengine.regtest.test.backchannel.BackChannelQueue;

public abstract class TestContext implements AutoCloseable {

    protected final Map<String, Supplier<?>> suppliers = new HashMap<>();
    protected final Supplier<MockAdapter> mockAdapter;
    protected final Supplier<DependencyInjector> dependencyInjector;
    protected final Supplier<BackChannelQueue> backChannelQueue;

    public TestContext() {
        mockAdapter = Suppliers.memoize(new Supplier<MockAdapter>() {
            @Override
            public MockAdapter get() {
                return createMockAdapter();
            }
        });
        suppliers.put("mockAdapter", mockAdapter);

        backChannelQueue = Suppliers.memoize(new Supplier<BackChannelQueue>() {
            @Override
            public BackChannelQueue get() {
                return createBackChannelQueue();
            }
        });
        suppliers.put("backChannelQueue", backChannelQueue);

        dependencyInjector = Suppliers.memoize(new Supplier<DependencyInjector>() {
            @Override
            public DependencyInjector get() {
                return createDependencyInjector();
            }
        });
        suppliers.put("dependencyInjector", dependencyInjector);
    }

    protected BackChannelQueue createBackChannelQueue() {
        return new BackChannelQueue();
    }

    protected DependencyInjector createDependencyInjector() {
        AbstractDependencyInjector dependencyInjector = new AbstractDependencyInjector() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            protected Object getBean(String beanId) {
                Supplier<?> supplier = suppliers.get(beanId);
                if (supplier == null) {
                    throw new RuntimeException("No supplier with id '" + beanId + "' found!");
                }
                else {
                    return supplier.get();
                }
            }
        };
        return dependencyInjector;
    }

    protected MockAdapter createMockAdapter() {
        MockAdapter x = new MockAdapter();
        x.setEngine(getProcessingEngine());
        return x;
    }

    protected abstract ProcessingEngine getProcessingEngine();

    protected void startup() {
        for (Supplier<?> s : suppliers.values()) {
            s.get();
        }
    };

    protected void shutdown() {

    }

    @Override
    public void close() {
        shutdown();
    }

}
