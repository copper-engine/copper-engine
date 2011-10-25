/*
 * Copyright 2002-2011 SCOOP Software GmbH
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
package de.scoopgmbh.copper.persistent;

import java.sql.Connection;
import java.util.List;

import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;

public interface ScottyDBStorageInterface {

	public void insert(final Workflow<?> wf) throws Exception;

	public void insert(final List<Workflow<?>> wfs) throws Exception;

	public void insert(final Workflow<?> wf, Connection con) throws Exception;

	public void insert(final List<Workflow<?>> wfs, Connection con) throws Exception;

	public void finish(final Workflow<?> w);

	public List<Workflow<?>> dequeue(final String ppoolId, final int max)
			throws Exception;

	public void notify(final Response<?> response, final Object callback)
			throws Exception;

	public void notify(final List<Response<?>> response) throws Exception;

	public void registerCallback(final RegisterCall rc) throws Exception;

	public void startup();

	public void shutdown();
	
	public void error(final Workflow<?> w, Throwable t);
	
	public void restart(final String workflowInstanceId) throws Exception;

}