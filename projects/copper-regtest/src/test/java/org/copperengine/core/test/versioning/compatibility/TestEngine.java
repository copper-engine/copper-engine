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
package org.copperengine.core.test.versioning.compatibility;

import java.sql.Connection;
import java.util.List;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.CopperException;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.EngineState;
import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.Response;
import org.copperengine.core.WaitHook;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowInstanceDescr;

public class TestEngine implements PersistentProcessingEngine {

    @Override
    public void startup() throws CopperRuntimeException {
    }

    @Override
    public void shutdown() throws CopperRuntimeException {
    }

    @Override
    public void addShutdownObserver(Runnable observer) {
    }

    @Override
    public void registerCallbacks(Workflow<?> w, WaitMode mode,
            long timeoutMsec, String... correlationIds)
            throws CopperRuntimeException {
    }

    @Override
    public void notify(Response<?> response) throws CopperRuntimeException {
    }

    @Override
    public String createUUID() {
        return Long.toHexString(System.currentTimeMillis());
    }

    @Override
    public String run(String wfname, Object data) throws CopperException {
        return null;
    }

    @Override
    public String run(WorkflowInstanceDescr<?> wfInstanceDescr) throws CopperException {
        return null;
    }

    @Override
    public void runBatch(List<WorkflowInstanceDescr<?>> wfInstanceDescr) throws CopperException {
    }

    @Override
    public EngineState getEngineState() {
        return null;
    }

    @Override
    public String getEngineId() {
        return null;
    }

    @Override
    public void addWaitHook(String wfInstanceId, WaitHook waitHook) {
    }

    @Override
    public String run(WorkflowInstanceDescr<?> wfInstanceDescr, Connection con) throws CopperException {
        return null;
    }

    @Override
    public void runBatch(List<WorkflowInstanceDescr<?>> wfInstanceDescr,
            Connection con) throws CopperException {
    }

    @Override
    public void restart(String workflowInstanceId) throws Exception {
    }

    @Override
    public void restartAll() throws Exception {
    }

    @Override
    public void notify(Response<?> response, Connection c)
            throws CopperRuntimeException {
    }

    @Override
    public void notify(List<Response<?>> responses, Connection c)
            throws CopperRuntimeException {
    }

    @Override
    public void notify(Response<?> response, Acknowledge ack)
            throws CopperRuntimeException {
    }

    @Override
    public void notifyProcessorPools() {

    }

}
