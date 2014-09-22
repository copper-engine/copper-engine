/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.core.tranzient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.copperengine.core.DependencyInjector;
import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.DefaultTicketPoolManager;
import org.copperengine.core.common.IdFactory;
import org.copperengine.core.common.JdkRandomUUIDFactory;
import org.copperengine.core.common.ProcessorPoolManager;
import org.copperengine.core.common.TicketPoolManager;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.monitoring.NullRuntimeStatisticsCollector;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.util.PojoDependencyInjector;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;

/**
 * Convenience class for easy creation of a transient {@link ProcessingEngine}. Override the corresponding create
 * methods
 * to adapt the created engine.
 *
 */
public abstract class TransientEngineFactory {

    protected EarlyResponseContainer createEarlyResponseContainer() {
        return new DefaultEarlyResponseContainer();
    }

    protected EngineIdProvider createEngineIdProvider() {
        return new EngineIdProviderBean("TransientEngine#" + System.nanoTime());
    }

    protected IdFactory createIdFactory() {
        return new JdkRandomUUIDFactory();
    }

    protected RuntimeStatisticsCollector createRuntimeStatisticsCollector() {
        return new NullRuntimeStatisticsCollector();
    }

    protected TimeoutManager createTimeoutManager() {
        return new DefaultTimeoutManager();
    }

    protected TicketPoolManager createTicketPoolManager() {
        return new DefaultTicketPoolManager();
    }

    protected abstract File getWorkflowSourceDirectory();

    protected WorkflowRepository createWorkflowRepository() {
        FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
        List<String> sourceDirs = new ArrayList<String>();
        sourceDirs.add(getWorkflowSourceDirectory().getAbsolutePath());
        repo.setSourceDirs(sourceDirs);
        repo.setTargetDir(System.getProperty("java.io.tmpdir") + "/copper/");
        return repo;
    }

    protected DependencyInjector createDependencyInjector() {
        return new PojoDependencyInjector();
    }

    protected ProcessorPoolManager<TransientProcessorPool> createProcessorPoolManager() {
        TransientPriorityProcessorPool defaultPP = new TransientPriorityProcessorPool(TransientProcessorPool.DEFAULT_POOL_ID);
        DefaultProcessorPoolManager<TransientProcessorPool> ppm = new DefaultProcessorPoolManager<TransientProcessorPool>();
        ppm.addProcessorPool(defaultPP);
        return ppm;
    }

    public TransientScottyEngine create() {
        TransientScottyEngine engine = new TransientScottyEngine();
        engine.setDependencyInjector(createDependencyInjector());
        engine.setEarlyResponseContainer(createEarlyResponseContainer());
        engine.setEngineIdProvider(createEngineIdProvider());
        engine.setIdFactory(createIdFactory());
        engine.setPoolManager(createProcessorPoolManager());
        engine.setStatisticsCollector(createRuntimeStatisticsCollector());
        engine.setTicketPoolManager(createTicketPoolManager());
        engine.setTimeoutManager(createTimeoutManager());
        engine.setWfRepository(createWorkflowRepository());
        engine.startup();
        return engine;
    }

}
