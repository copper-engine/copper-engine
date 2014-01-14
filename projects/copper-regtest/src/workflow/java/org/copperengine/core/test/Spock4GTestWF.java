/*
 * Copyright 2002-2014 SCOOP Software GmbH
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

import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Spock4GTestWF extends Workflow<String> {

    private static final Logger logger = LoggerFactory.getLogger(Spock4GTestWF.class);
    private static final long serialVersionUID = 1816644971610832089L;

    protected abstract void abstractPartnersystemCall() throws Interrupt;

    protected void doSomething01() throws Interrupt {
        try {
            logger.debug("> doSomething01");
            wait(WaitMode.ALL, 100, getEngine().createUUID());
            logger.debug("< doSomething01");
        } catch (NullPointerException ex) {
            logger.debug("should never happen");
        }
    }

    protected void doSomething02() throws Interrupt {
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

    protected void doSomething03() throws Interrupt {
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

    protected void doSomething04() throws Interrupt {
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
