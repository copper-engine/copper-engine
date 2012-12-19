/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.test.versioning.compatibility;

import java.sql.Connection;
import java.util.List;

import de.scoopgmbh.copper.CopperException;
import de.scoopgmbh.copper.CopperRuntimeException;
import de.scoopgmbh.copper.EngineState;
import de.scoopgmbh.copper.PersistentProcessingEngine;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitHook;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowInstanceDescr;

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
	public void run(String wfname, Object data) throws CopperException {
	}

	@Override
	public void run(WorkflowInstanceDescr<?> wfInstanceDescr)
			throws CopperException {
	}

	@Override
	public void runBatch(List<WorkflowInstanceDescr<?>> wfInstanceDescr)
			throws CopperException {
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
	public void run(WorkflowInstanceDescr<?> wfInstanceDescr, Connection con)
			throws CopperException {
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

}
