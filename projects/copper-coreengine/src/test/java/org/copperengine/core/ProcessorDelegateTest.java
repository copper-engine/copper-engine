package org.copperengine.core;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProcessorDelegateTest {

    @Test
    void executeInVirtualThread() throws Exception {
        execute(new TransientEngineFactory.CreateParam(100), true);
    }

    @Test
    void executeInPlatformThread() throws Exception {
        execute(new TransientEngineFactory.CreateParam(0), false);
    }

    private void execute(final TransientEngineFactory.CreateParam createParam, final boolean expected) throws Exception {
        TransientEngineFactory factory = new TransientEngineFactory() {
            @Override
            protected File getWorkflowSourceDirectory() {
                return new File("./src/test/workflow");
            }
        };
        TransientScottyEngine engine = factory.create(createParam);
        try {
            Assertions.assertEquals("STARTED", engine.getState());
            engine.run("test.ProcessorDelegateWorkflow", expected);
        } finally {
            LockSupport.parkNanos(Duration.ofMillis(500).toNanos());
            Assertions.assertEquals(0, engine.getNumberOfWorkflowInstances());
            engine.shutdown();
        }
    }
}
