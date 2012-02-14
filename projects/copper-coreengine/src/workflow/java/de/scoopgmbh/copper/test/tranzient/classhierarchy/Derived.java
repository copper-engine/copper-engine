/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.test.tranzient.classhierarchy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.InterruptException;

public abstract class Derived extends Base {
	
	private static final Logger logger = LoggerFactory.getLogger(Derived.class);

	protected abstract void callMock() throws InterruptException;
	
	@Override
	public void main() throws InterruptException {
		callMock();
		
		for (i=0; i<5; i++) {
			
			mockSync();

			resubmit(); // just for fun...
			
			mockAsync();
			
		}
		
		simulateTimeout();
		
		doubleWait();
		
		logger.info("setting response");
		getData().setResponse(counter);
		
	}

}
