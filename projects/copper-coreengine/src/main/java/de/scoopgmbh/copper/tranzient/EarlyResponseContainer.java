package de.scoopgmbh.copper.tranzient;

import java.util.List;

import de.scoopgmbh.copper.Response;

/**
 * A transient container for early response. In the context of COPPER, a reponse is called early, if an asynchronous  response 
 * is provided to an engine using <code>notify</code> before the workflow has called <code>wait</code>.
 * In this case the engine is unable to correlate the response with a waiting workflow instance and will store the
 * response in this container. Later on, the engine will ask the container for every <code>wait</code> if a response for this
 * wait is already there. In this case the response is removed from the container.
 * The container may implement a strategy to remove reponses for which no wait from a corresponding workflow instance is called.  
 * @author austermann
 *
 */
public interface EarlyResponseContainer {

	/**
	 * Puts an early reponse into the container
	 * @param response 
	 */
	public void put(final Response<?> response);

	/**
	 * Gets and removed a response for the provided correlationId if it exists.
	 * @param correlationId
	 * @return the response or <code>null</code> if there is no response for the provided correlationId.
	 */
	public Response<?> get(final String correlationId);

	/**
	 * Notifies this container about a stale correlationId. Stale in this context means, that a response for
	 * this correlationId  may be ignored by the container.
	 * 
	 * @param correlationId
	 */
	public void putStaleCorrelationId(final String correlationId);

	/**
	 * Notifies this container about some stale correlationIds. Stale in this context means, that a response for
	 * this correlationId  may be ignored by the container.

	 * @param correlationIds
	 */
	public void putStaleCorrelationId(List<String> correlationIds);

	/**
	 * Startup the container
	 */
	public void startup();

	/**
	 * Shutdown the container
	 */
	public void shutdown();
	
}