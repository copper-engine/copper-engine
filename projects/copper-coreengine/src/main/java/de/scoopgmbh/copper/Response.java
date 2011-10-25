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

/**
 * Container for asynchronous responses.
 * 
 * @author austermann
 *
 * @param <E>
 */
public class Response<E> implements Serializable {

	private static final long serialVersionUID = 7903392981566779966L;
	private final String correlationId;
	private final E response;
	private final Exception exception;
	private final boolean timeout;
	
	/**
	 * Creates a new instance
	 * @param correlationId
	 * @param response
	 * @param exception
	 */
	public Response(String correlationId, E response, Exception exception) {
		super();
		this.correlationId = correlationId;
		this.response = response;
		this.exception = exception;
		this.timeout = false;
	}

	/**
	 * Creates a new instance with timeout set to true. 
	 * @param correlationId
	 */
	public Response(String correlationId) {
		super();
		this.correlationId = correlationId;
		this.response = null;
		this.exception = null;
		this.timeout = true;
	}
	
	/**
	 * 
	 * @return the correlation id of this response
	 */
	public String getCorrelationId() {
		return correlationId;
	}
	
	/**
	 * @return the response object 
	 */
	public E getResponse() {
		return response;
	}
	
	/**
	 * @return the exception or <code>null</code> if no exception occured 
	 */
	public Exception getException() {
		return exception;
	}
	
	/**
	 * 
	 * @return true, if a timeout occured while waiting for the response.
	 */
	public boolean isTimeout() {
		return timeout;
	}

	@Override
	public String toString() {
		return "Response [correlationId=" + correlationId + ", exception="
				+ exception + ", response=" + response + ", timeout=" + timeout
				+ "]";
	}

	
	
}
