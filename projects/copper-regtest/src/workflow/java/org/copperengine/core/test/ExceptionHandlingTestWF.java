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

import java.io.Serializable;

import org.copperengine.core.InterruptException;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandlingTestWF extends PersistentWorkflow<Serializable> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingTestWF.class);

    @Override
    public void main() throws InterruptException {
        // System.err.println("main "+this.__stack);
        main_1();
    }

    public void main_1() throws InterruptException {
        // System.err.println("main_1 "+this.__stack);
        try {
            red("");
        } catch (RuntimeException e) {
            logger.debug(e.toString());
            // throw e;
        }
    }

    public void red(String prefix) throws InterruptException {
        // System.err.println("red "+this.__stack);
        String red = new String(prefix + ":red");
        blue(red);
    }

    public void blue(String prefix) throws InterruptException {
        // System.err.println("blue "+this.__stack);
        String blue = new String(prefix + ":blue");
        green(blue);
    }

    public void green(String prefix) throws InterruptException {
        // System.err.println("gree "+this.__stack);
        throw new RuntimeException("out of colour");
    }

}
