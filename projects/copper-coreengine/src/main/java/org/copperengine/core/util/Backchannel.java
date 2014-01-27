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
package org.copperengine.core.util;

import java.util.concurrent.TimeUnit;

/**
 * Utility that offers functionality to signal a response from within a persistent workflow to its originator.
 * Intended usage as follows:
 * <p>
 * The originator (e.g. a webservice input adapter) and its created workflow instance share a dependency to the same
 * Backchannel. Furthermore, the originator creates a unique correlationId and passes this correlationId to the workflow
 * instance.
 * <p>
 * The originator will then call {@link Backchannel#wait(String, long, TimeUnit)} to wait for a response for this
 * correlationId}.
 * <p>
 * The workflow instance will signal its response by calling {@link Backchannel#notify(String, Object)}.
 */
public interface Backchannel {

    /**
     * Waits for a response for the specified correlationId.
     * 
     * @param correlationId
     *        unique correlation id to wait for
     * @param timeout
     *        timeout
     * @param timeunit
     *        timeunit of timeout
     * @return the response or <code>null</code> in case of a timeout.
     * @throws InterruptedException
     */
    public Object wait(String correlationId, long timeout, TimeUnit timeunit) throws InterruptedException;

    /**
     * Passes a response for the specified correlationId to the bachchannel.
     * 
     * @param correlationId
     *        unique correlation id
     * @param response
     *        the response object - <code>null</code> value is allowed
     */
    public void notify(String correlationId, Object response);

}
