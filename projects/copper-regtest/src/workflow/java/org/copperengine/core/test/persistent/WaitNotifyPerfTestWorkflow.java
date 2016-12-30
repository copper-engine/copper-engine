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
package org.copperengine.core.test.persistent;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.test.MockAdapter;
import org.copperengine.core.util.Backchannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitNotifyPerfTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(WaitNotifyPerfTestWorkflow.class);

    private transient Backchannel backchannel;
    private transient MockAdapter mockAdapter;

    @AutoWire
    public void setBackchannel(Backchannel backchannel) {
        this.backchannel = backchannel;
    }

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @Override
    public void main() throws Interrupt {
        logger.info("Starting....");
        for (int i = 0; i < 100; i++) {
            logger.info("Wait/notify...");
            final String cid = getEngine().createUUID();
            mockAdapter.foo("foo", cid, 0);
            final long startTS = System.currentTimeMillis();
            wait(WaitMode.ALL, 1000, cid);
            final long et = System.currentTimeMillis() - startTS;
            System.out.println("Wait took " + et + " msec");
        }
        logger.info("Finished!");
        backchannel.notify(getId(), "Finished!");
    }
}
