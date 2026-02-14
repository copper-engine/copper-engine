package org.copperengine.regtest.test.persistent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.copperengine.regtest.test.backchannel.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Commons {

    private static final Logger logger = LoggerFactory.getLogger(Commons.class);

    static void assertWorkflowResult(WorkflowResult x, String data) {
        assertNotNull(x);
        Exception unexpectedException = x.getException();
        if (unexpectedException != null) {
            logger.warn("Unexpected exception", unexpectedException);
        }
        assertNull(unexpectedException);
        assertNotNull(x.getResult());
        assertEquals(data.length(), x.getResult().toString().length());
    }

    private Commons() {
    }
}
