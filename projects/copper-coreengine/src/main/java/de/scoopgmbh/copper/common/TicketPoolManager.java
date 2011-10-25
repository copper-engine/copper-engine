 package de.scoopgmbh.copper.common;

import java.util.List;
import java.util.Map;

import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.util.TicketPool;

public interface TicketPoolManager {
	
	/**
	 * Returns the ticket pool with the corresponding ticket pool id.
	 * @param id ticket pool id
	 * @return the ticket pool or null if non-existent
	 */
	TicketPool getTicketPool(final String id);
	

	/**
	 * Checks if the ticket pool with the corresponding id exists.
	 * @param poolId ticket pool id
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
	 
	 void addMapping(Class<?> workflowClass, String ticketPoolId);
	 void removeMapping(Class<?> workflowClass);
	 void setMapping(Map<Class<?>, String> mapping);

}
