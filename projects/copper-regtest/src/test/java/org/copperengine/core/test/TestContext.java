package org.copperengine.core.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.copperengine.core.AbstractDependencyInjector;
import org.copperengine.core.DependencyInjector;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.test.backchannel.BackChannelQueue;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public abstract class TestContext implements AutoCloseable {

    protected final Map<String, Supplier<?>> suppliers = new HashMap<>();
    protected final Supplier<Properties> properties;
    protected final Supplier<MockAdapter> mockAdapter;
    protected final Supplier<DependencyInjector> dependencyInjector;
    protected final Supplier<BackChannelQueue> backChannelQueue;

    public TestContext() {
        properties = Suppliers.memoize(new Supplier<Properties>() {
            @Override
            public Properties get() {
                return createProperties();
            }
        });
        suppliers.put("properties", properties);

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

    protected Properties createProperties() {
        try {
            Properties p = new Properties();
            p.load(getClass().getResourceAsStream("/regtest.properties"));
            return p;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("failed to load properties", e);
        }
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
