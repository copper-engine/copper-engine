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
package org.copperengine.regtest.test.tranzient;

import java.util.Map;

import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.DefaultTicketPoolManager;
import org.copperengine.core.common.TicketPool;
import org.copperengine.core.tranzient.DefaultEarlyResponseContainer;
import org.copperengine.core.tranzient.DefaultTimeoutManager;
import org.copperengine.core.tranzient.TransientPriorityProcessorPool;
import org.copperengine.core.tranzient.TransientProcessorPool;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.copperengine.regtest.test.TestContext;
import org.copperengine.regtest.test.backchannel.BackChannelQueue;

public class TransientEngineTestContext extends TestContext {

    public static final String PPOOL_DEFAULT = "T#DEFAULT";

    protected final Supplier<TransientScottyEngine> engine;
    protected final Supplier<FileBasedWorkflowRepository> repo;
    protected final Supplier<DefaultProcessorPoolManager<TransientProcessorPool>> ppoolManager;

    public TransientEngineTestContext() {
        ppoolManager = Suppliers.memoize(new Supplier<DefaultProcessorPoolManager<TransientProcessorPool>>() {
            @Override
            public DefaultProcessorPoolManager<TransientProcessorPool> get() {
                return createProcessorPoolManager();
            }
        });
        suppliers.put("ppoolManager", ppoolManager);

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
    }

    protected FileBasedWorkflowRepository createFileBasedWorkflowRepository() {
        FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
        repo.setSourceDirs("src/workflow/java");
        repo.setTargetDir("build/compiled_workflow");
        repo.setLoadNonWorkflowClasses(true);
        repo.setCompilerOptions("-g");
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

    @Override
    public void startup() {
        super.startup();
        mockAdapter.get().startup();
        engine.get().startup();
    }

    @Override
    public void shutdown() {
        engine.get().shutdown();
        mockAdapter.get().shutdown();
        super.shutdown();
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

    @Override
    protected ProcessingEngine getProcessingEngine() {
        return getEngine();
    }
}
