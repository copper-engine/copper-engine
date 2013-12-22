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
package org.copperengine.core.test.tranzient.simple;

import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTestChildWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SimpleTestChildWorkflow.class);

    @Override
    public void main() throws Interrupt {
        logger.info("starting...");

        // process the response
        String data = getData();
        StringBuilder responseSB = new StringBuilder(data.length());
        for (int i = data.length() - 1; i >= 0; i--) {
            responseSB.append(data.charAt(i));
        }

        logger.info("sending response to caller...");
        // send back response to caller
        Response<String> response = new Response<String>(this.getId(), responseSB.toString(), null);
        notify(response);

        logger.info("finished");
    }

}
