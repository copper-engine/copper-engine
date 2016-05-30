package org.copperengine.core.test;

import java.util.HashMap;
import java.util.Map;

import org.copperengine.core.AbstractDependencyInjector;
import org.copperengine.core.DependencyInjector;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.test.backchannel.BackChannelQueue;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

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
