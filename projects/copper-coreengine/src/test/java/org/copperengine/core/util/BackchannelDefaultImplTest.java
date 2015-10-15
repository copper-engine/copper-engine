/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.core.util;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackchannelDefaultImplTest {

    private static final Logger logger = LoggerFactory.getLogger(BackchannelDefaultImpl.class);

    final String correlationId = "4711";
    final Object payload = new Object();

    @Test
    public void testEarlyResponse() throws Exception {
        BackchannelDefaultImpl channel = new BackchannelDefaultImpl();
        channel.notify(correlationId, payload);
        final Object response = channel.wait(correlationId, 1000, TimeUnit.MILLISECONDS);
        Assert.assertEquals(payload, response);
    }

    @Test
    public void testReponse() throws Exception {
        final BackchannelDefaultImpl channel = new BackchannelDefaultImpl();
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(50L);
                    channel.notify(correlationId, payload);
                } catch (Exception e) {
                    logger.error("unexpected exception", e);
                }
            };
        }.start();
        final Object response = channel.wait(correlationId, 1000, TimeUnit.MILLISECONDS);
        Assert.assertEquals(payload, response);
    }
}
