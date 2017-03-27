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
package org.copperengine.core;

/**
 * Callback interface for asynchronous responses.
 * There are two ways how the receiver of a response may pass it to a copper engine.
 * First, the receiver knows the engine and uses <code>engine.notify</code>.
 * Second, the receiver puts the response into a callback object, created and passed to it by the caller.
 * Callback objects are created using the <code>Workflow.createCallback()</code>.
 *
 * @param <E>
 *        type of data held in the response.
 * @author austermann
 */
public interface Callback<E> {

    public String getCorrelationId();

    public void notify(E response, Acknowledge ack);

    public void notify(Exception exception, Acknowledge ack);

    public Response<E> getResponse(Workflow<?> wf);

}
