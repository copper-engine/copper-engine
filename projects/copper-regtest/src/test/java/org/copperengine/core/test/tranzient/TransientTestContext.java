package org.copperengine.core.test.tranzient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.core.AbstractDependencyInjector;
import org.copperengine.core.DependencyInjector;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.DefaultTicketPoolManager;
import org.copperengine.core.common.TicketPool;
import org.copperengine.core.test.MockAdapter;
import org.copperengine.core.test.backchannel.BackChannelQueue;
import org.copperengine.core.tranzient.DefaultEarlyResponseContainer;
import org.copperengine.core.tranzient.DefaultTimeoutManager;
import org.copperengine.core.tranzient.TransientPriorityProcessorPool;
import org.copperengine.core.tranzient.TransientProcessorPool;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.copperengine.core.wfrepo.CompilerOptionsProvider;
import org.copperengine.core.wfrepo.ConfigurableStringOptionsProvider;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class TransientTestContext implements AutoCloseable {

    public static final String PPOOL_DEFAULT = "T#DEFAULT";

    protected final Map<String, Supplier<?>> suppliers = new HashMap<>();
    protected final Supplier<MockAdapter> mockAdapter;
    protected final Supplier<TransientScottyEngine> engine;
    protected final Supplier<FileBasedWorkflowRepository> repo;
    protected final Supplier<DependencyInjector> dependencyInjector;
    protected final Supplier<BackChannelQueue> backChannelQueue;
    protected final Supplier<DefaultProcessorPoolManager<TransientProcessorPool>> ppoolManager;

    public TransientTestContext() {
        ppoolManager = Suppliers.memoize(new Supplier<DefaultProcessorPoolManager<TransientProcessorPool>>() {
            @Override
            public DefaultProcessorPoolManager<TransientProcessorPool> get() {
                return createProcessorPoolManager();
            }
        });
        suppliers.put("ppoolManager", ppoolManager);

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

        repo = Suppliers.memoize(new Supplier<FileBasedWorkflowRepository>() {
            @Override
            public FileBasedWorkflowRepository get() {
                return createFileBasedWorkflowRepository();
            }
        });
        suppliers.put("repo", repo);

        engine = Suppliers.memoize(new Supplier<TransientScottyEngine>() {
            @Override
            public TransientScottyEngine get() {
                return createTransientScottyEngine();
            }
        });
        suppliers.put("engine", engine);

        mockAdapter = Suppliers.memoize(new Supplier<MockAdapter>() {
            @Override
            public MockAdapter get() {
                return createMockAdapter();
            }
        });
        suppliers.put("mockAdapter", mockAdapter);
    }

    protected BackChannelQueue createBackChannelQueue() {
        return new BackChannelQueue();
    }

    protected FileBasedWorkflowRepository createFileBasedWorkflowRepository() {
        FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
        repo.setSourceDirs(Arrays.asList(new String[] { "src/workflow/java" }));
        repo.setTargetDir("build/compiled_workflow");
        repo.setLoadNonWorkflowClasses(true);
        List<CompilerOptionsProvider> compilerOptionsProviders = new ArrayList<>();
        ConfigurableStringOptionsProvider x = new ConfigurableStringOptionsProvider();
        x.setOptions(Arrays.asList(new String[] { "-g" }));
        compilerOptionsProviders.add(x);
        repo.setCompilerOptionsProviders(compilerOptionsProviders);
        return repo;
    }

    protected TransientScottyEngine createTransientScottyEngine() {
        DefaultTicketPoolManager ticketPoolManager = new DefaultTicketPoolManager();
        ticketPoolManager.add(new TicketPool("DEFAULT", 20000));
        ticketPoolManager.add(new TicketPool("SMALL", 50));

        TransientScottyEngine engine = new TransientScottyEngine();
        engine.setTicketPoolManager(ticketPoolManager);
        engine.setPoolManager(ppoolManager.get());
        engine.setWfRepository(repo.get());
        engine.setDependencyInjector(dependencyInjector.get());
        engine.setEarlyResponseContainer(new DefaultEarlyResponseContainer());
        engine.setTimeoutManager(new DefaultTimeoutManager());

        return engine;
    }

    private DefaultProcessorPoolManager<TransientProcessorPool> createProcessorPoolManager() {
        DefaultProcessorPoolManager<TransientProcessorPool> processorPoolManager = new DefaultProcessorPoolManager<TransientProcessorPool>();
        processorPoolManager.addProcessorPool(new TransientPriorityProcessorPool(PPOOL_DEFAULT, 4));
        processorPoolManager.addProcessorPool(new TransientPriorityProcessorPool("PS47112", 4));
        return processorPoolManager;
    }

    protected MockAdapter createMockAdapter() {
        MockAdapter x = new MockAdapter();
        x.setEngine(engine.get());
        return x;
    }

    public TransientTestContext startup() {
        mockAdapter.get().startup();
        engine.get().startup();
        return this;
    }

    public void shutdown() {
        engine.get().shutdown();
        mockAdapter.get().shutdown();
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

    @Override
    public void close() {
        shutdown();
    }

    public TransientScottyEngine getEngine() {
        return engine.get();
    }

    public BackChannelQueue getBackChannelQueue() {
        return backChannelQueue.get();
    }

    public Map<String, Supplier<?>> getSuppliers() {
        return suppliers;
    }

    public FileBasedWorkflowRepository getRepo() {
        return repo.get();
    }

    public DefaultProcessorPoolManager<TransientProcessorPool> getPpoolManager() {
        return ppoolManager.get();
    }
}
