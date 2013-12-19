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
