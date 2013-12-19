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
package org.copperengine.core.persistent;

import java.io.Serializable;

import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.common.WorkflowRepository;


/**
 * Service for serializing and deserializing {@link Workflow} instances and {@link Response} instances.
 * The implementation decides how to serialize an instance, e.g. using standard java serialization or XML or...
 * 
 * @author austermann
 *
 */
public interface Serializer {
	
	public SerializedWorkflow serializeWorkflow(final Workflow<?> o) throws Exception;
	public Workflow<?> deserializeWorkflow(SerializedWorkflow serializedWorkflow, final WorkflowRepository wfRepo) throws Exception;
	
	public String serializeResponse(final Response<?> r) throws Exception;
	public Response<?> deserializeResponse(String _data) throws Exception;

	public String serializeObject(final Serializable o) throws Exception;
	public Serializable deserializeObject(String _data) throws Exception;

}
