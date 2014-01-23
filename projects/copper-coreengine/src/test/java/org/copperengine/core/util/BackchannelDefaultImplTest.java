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
