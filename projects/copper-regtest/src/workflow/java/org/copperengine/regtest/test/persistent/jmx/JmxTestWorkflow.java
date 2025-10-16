/*
 * Copyright 2002-2017 SCOOP Software GmbH
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

package org.copperengine.regtest.test.persistent.jmx;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(JmxTestWorkflow.class);
    
    private transient JmxTestAdapter jmxTestAdapter;
    
    @AutoWire
    public void setJmxTestAdapter(JmxTestAdapter jmxTestAdapter) {
        this.jmxTestAdapter = jmxTestAdapter;
    }

    @Override
    public void main() throws Interrupt {
        if ("ERROR".equalsIgnoreCase(getData())) {
            throw new RuntimeException("Test!!!");
        }
        String cid = getEngine().createUUID();
        logger.info("Calling foo...");
        jmxTestAdapter.foo(cid);
        logger.info("Waiting...");
        wait(WaitMode.ALL,60000,cid);
        logger.info("Waking up again...");

        cid = getEngine().createUUID();
        logger.info("Calling foo...");
        jmxTestAdapter.foo(cid);
        logger.info("Waiting...");
        wait(WaitMode.ALL,60000,cid);
        logger.info("Waking up again...");
        
        logger.info("Finished!");
    }

}
