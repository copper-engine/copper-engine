/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package org.copperengine.monitoring.server.wrapper;

import java.util.List;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.CopperException;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.EngineState;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.Response;
import org.copperengine.core.WaitHook;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.monitoring.server.monitoring.MonitoringDataCollector;

/**
 * Add Monitoring for ProcessingEngine for a specific adapter
 *
 * @author hbrackmann
 */
public class MonitoringAdapterProcessingEngine implements ProcessingEngine {

    private final ProcessingEngine processingEngine;
    private final MonitoringDataCollector monitoringDataCollector;
    private Object adapter;

    public MonitoringAdapterProcessingEngine(ProcessingEngine processingEngine, MonitoringDataCollector monitoringDataCollector) {
        this(new Object(), processingEngine, monitoringDataCollector);
    }

    public MonitoringAdapterProcessingEngine(Object adapter, ProcessingEngine processingEngine, MonitoringDataCollector monitoringDataCollector) {
        super();
        this.adapter = adapter;
        this.processingEngine = processingEngine;
        this.monitoringDataCollector = monitoringDataCollector;
    }

    public void setAdapter(Object adapter) {
        this.adapter = adapter;
    }

    @Override
    public void startup() throws CopperRuntimeException {
        processingEngine.startup();
    }

    @Override
    public void shutdown() throws CopperRuntimeException {
        processingEngine.shutdown();
    }

    @Override
    public void addShutdownObserver(Runnable observer) {
        processingEngine.addShutdownObserver(observer);
    }

    @Override
    public void registerCallbacks(Workflow<?> w, WaitMode mode, long timeoutMsec, String... correlationIds) throws CopperRuntimeException {
        processingEngine.registerCallbacks(w, mode, timeoutMsec, correlationIds);
    }

    @Override
    @Deprecated
    public void notify(Response<?> response) throws CopperRuntimeException {
        monitoringDataCollector.submitAdapterWfNotify(response.getCorrelationId(), response.getResponse(), adapter);
        processingEngine.notify(response);
    }

    @Override
    public String createUUID() {
        return processingEngine.createUUID();
    }

    @Override
    public void run(String wfname, Object data) throws CopperException {
        monitoringDataCollector.submitAdapterWfLaunch(wfname, adapter);
        processingEngine.run(wfname, data);
    }

    @Override
    public void run(WorkflowInstanceDescr<?> wfInstanceDescr) throws CopperException {
        monitoringDataCollector.submitAdapterWfLaunch(wfInstanceDescr.getWfName(), adapter);
        processingEngine.run(wfInstanceDescr);
    }

    @Override
    public void runBatch(List<WorkflowInstanceDescr<?>> wfInstanceDescr) throws CopperException {
        for (WorkflowInstanceDescr<?> wfInstanceDesc : wfInstanceDescr) {
            monitoringDataCollector.submitAdapterWfLaunch(wfInstanceDesc.getWfName(), adapter);
        }
        processingEngine.runBatch(wfInstanceDescr);
    }

    @Override
    public EngineState getEngineState() {
        return processingEngine.getEngineState();
    }

    @Override
    public String getEngineId() {
        return processingEngine.getEngineId();
    }

    @Override
    public void addWaitHook(String wfInstanceId, WaitHook waitHook) {
        processingEngine.addWaitHook(wfInstanceId, waitHook);
    }

    @Override
    public void notify(Response<?> response, Acknowledge ack)
            throws CopperRuntimeException {
        monitoringDataCollector.submitAdapterWfNotify(response.getCorrelationId(), response.getResponse(), adapter);
        processingEngine.notify(response, ack);
    }

}
