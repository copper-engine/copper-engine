/**
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
package org.copperengine.regtest.test.persistent;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.util.Backchannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavepointPerfTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SavepointPerfTestWorkflow.class);

    private transient Backchannel backchannel;

    @AutoWire
    public void setBackchannel(Backchannel backchannel) {
        this.backchannel = backchannel;
    }

    @Override
    public void main() throws Interrupt {
        logger.info("Starting....");
        for (int i = 0; i < 100; i++) {
            logger.info("Savepoint...");
            final long startTS = System.currentTimeMillis();
            savepoint();
            final long et = System.currentTimeMillis() - startTS;
            System.out.println("Savepoint took " + et + " msec");
        }
        logger.info("Finished!");
        backchannel.notify(getId(), "Finished!");
    }
}
