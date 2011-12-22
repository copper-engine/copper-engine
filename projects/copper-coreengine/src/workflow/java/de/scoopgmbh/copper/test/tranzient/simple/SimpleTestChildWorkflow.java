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
package de.scoopgmbh.copper.test.tranzient.simple;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;

public class SimpleTestChildWorkflow extends PersistentWorkflow<String> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SimpleTestChildWorkflow.class);

	@Override
	public void main() throws InterruptException {
		logger.info("starting...");
		
		// process the response
		String data = getData();
		StringBuilder responseSB = new StringBuilder(data.length());
		for (int i=data.length()-1; i>=0; i--) {
			responseSB.append(data.charAt(i));
		}

		logger.info("sending response to caller...");
		// send back response to caller
		Response<String> response = new Response<String>(this.getId(), responseSB.toString(), null); 
		getEngine().notify(response);

		logger.info("finished");
	}

}
