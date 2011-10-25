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
import de.scoopgmbh.copper.Workflow;

public abstract class Spock4GTestWF extends Workflow<String> {

	private static final Logger logger = Logger.getLogger(Spock4GTestWF.class);
	private static final long serialVersionUID = 1816644971610832089L;

	protected abstract void abstractPartnersystemCall() throws InterruptException;

	protected void doSomething01() throws InterruptException {
		try {
			logger.debug("> doSomething01");
			wait(WaitMode.ALL, 100, getEngine().createUUID());
			logger.debug("< doSomething01");
		} catch (NullPointerException ex) {
			logger.debug("should never happen");
		}
	}

	protected void doSomething02() throws InterruptException {
		try {
			logger.debug("> doSomething02");
			wait(WaitMode.ALL, 50, getEngine().createUUID());
			doSomething01();
			wait(WaitMode.ALL, 50, getEngine().createUUID());
			logger.debug("< doSomething02");
		} catch (NullPointerException ex) {
			logger.debug("should never happen");
		}
	}

	protected void doSomething03() throws InterruptException {
		try {
			logger.debug("> doSomething03");
			wait(WaitMode.ALL, 50, getEngine().createUUID());
			doSomething02();
			wait(WaitMode.ALL, 50, getEngine().createUUID());
			logger.debug("< doSomething03");
		} catch (NullPointerException ex) {
			logger.debug("should never happen");
		}
	}

	protected void doSomething04() throws InterruptException {
		try {
			logger.debug("> doSomething04");
			wait(WaitMode.ALL, 50, getEngine().createUUID());
			doSomething03();
			wait(WaitMode.ALL, 50, getEngine().createUUID());
			logger.debug("< doSomething04");
		} catch (NullPointerException ex) {
			logger.debug("should never happen");
		}
	}
}
