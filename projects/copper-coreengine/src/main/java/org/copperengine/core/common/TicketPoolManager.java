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
package org.copperengine.core.common;

import java.util.List;
import java.util.Map;

import org.copperengine.core.Workflow;

/**
 * Container for a set of named (by their <code>id</code>) {@link TicketPool}s.
 * 
 * @author austermann
 */
public interface TicketPoolManager {

    /**
     * Returns the ticket pool with the corresponding ticket pool id.
     * 
     * @param id
     *            ticket pool id
     * @return the ticket pool or null if non-existent
     */
    TicketPool getTicketPool(final String id);

    /**
     * Checks if the ticket pool with the corresponding id exists.
     * 
     * @param id
     *            ticket pool id
     * @return true is the ticket pool with the corresponding id exists.
     */
    boolean exists(String id);

    void add(TicketPool ticketPool);

    void remove(TicketPool ticketPool);

    void setTicketPools(List<TicketPool> ticketPools);

    void startup();

    void shutdown();

    void obtain(Workflow<?> wf);

    void release(Workflow<?> wf);

    void obtain(String workflowClass);

    void release(String workflowClass);

    void addMapping(String workflowClass, String ticketPoolId);

    void removeMapping(String workflowClass);

    void setMapping(Map<String, String> mapping);

}
