/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package org.copperengine.core.test;

import org.copperengine.core.InterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExtendedSpock4GTestWF extends Spock4GTestWF {
	private static final Logger logger = LoggerFactory.getLogger(ExtendedSpock4GTestWF.class);

	private static final long serialVersionUID = 1L;

	@Override
	public void main() throws InterruptException {
		logger.debug("started");

		doSomething01();

		doSomething02();

		doSomething03();

		doSomething04();

		logger.debug("finished");
	}

	public ExtendedSpock4GTestWF() {
	}

	@Override
	protected void abstractPartnersystemCall() throws InterruptException {
		//resubmitt();
		//changeProcessPool("PARNERSYSTEM_MOCK");



		//changeProcessPool("DEFAULT");
	}

}
