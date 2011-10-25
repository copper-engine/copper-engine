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
package de.scoopgmbh.copper;

import java.io.Serializable;

class DefaultCallback<E> implements Callback<E>, Serializable {

	private static final long serialVersionUID = -2978285750814814436L;
	private final String correlationId;
	private transient ProcessingEngine engine;
	private Response<E> response;
	
	public DefaultCallback(ProcessingEngine engine) {
		super();
		this.engine = engine;
		this.correlationId = engine.createUUID();
	}
	
	public void setEngine(ProcessingEngine engine) {
		this.engine = engine;
	}

	@Override
	public String getCorrelationId() {
		return correlationId;
	}

	@Override
	public void notify(E response) {
		engine.notify(new Response<E>(correlationId, response, null));
	}

	@Override
	public void notify(Exception exception) {
		engine.notify(new Response<E>(correlationId, null, exception));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response<E> getResponse(final Workflow wf) {
		if (response == null) {
			response = (Response<E>) wf.getAndRemoveResponse(correlationId);
		}
		return response;
	}


}
