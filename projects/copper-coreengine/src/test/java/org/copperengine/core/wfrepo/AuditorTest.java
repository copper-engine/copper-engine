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
package org.copperengine.core.wfrepo;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AuditorTest {

    @Test
    void execute() throws Exception {
        TransientEngineFactory factory = new TransientEngineFactory() {
            @Override
            protected File getWorkflowSourceDirectory() {
                return new File("./src/test/workflow");
            }
        };
        TransientScottyEngine engine = factory.create();
        try {
            Assertions.assertEquals("STARTED", engine.getState());
            engine.run("test.AuditorWorkflow", null);
        } finally {
            LockSupport.parkNanos(Duration.ofMillis(500).toNanos());
            Assertions.assertEquals(0, engine.getNumberOfWorkflowInstances());
            engine.shutdown();
        }
    }
}
