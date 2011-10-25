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
package de.scoopgmbh.copper.test;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.WaitMode;

public class ExtendedSpock2GTestWF extends Spock2GTestWF {

	private static final Logger logger = Logger.getLogger(ExtendedSpock2GTestWF.class);
	private static final long serialVersionUID = 1L;
	private String correlationId;
	
	public ExtendedSpock2GTestWF() {
	}

	@Override
	protected void abstractPartnersystemCall() throws InterruptException {
		correlationId = "ThisIsACustomCorrelationId"+System.nanoTime();
		mockAdapter.foo("foo", correlationId);
		logger.debug("Request sent, waiting...");
		wait(WaitMode.ALL, 250, correlationId);
		logger.debug("Waking up again, response="+super.getAndRemoveResponse(correlationId));
	}
	
}
