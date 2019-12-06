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

package org.copperengine.ext.wfrepo.git;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.copperengine.ext.wfrepo.classpath.ClasspathWorkflowRepository;
import org.junit.Assert;
import org.junit.Test;

public class GitWorkflowRepositoryTest {

    @Test
    public void testExec() throws Exception {
        final GitWorkflowRepository wfRepo = new GitWorkflowRepository();
        wfRepo.addSourceDir("./ttt");
        wfRepo.setTargetDir(".tttarget");
        wfRepo.setCheckIntervalMSec(1000);
        wfRepo.setURI("file://C:/DEV/src/git-wf");
        wfRepo.setVersion("master");
        try {
            final TransientEngineFactory factory = new TransientEngineFactory() {
                @Override
                protected WorkflowRepository createWorkflowRepository() {
                    return wfRepo;
                }

                @Override
                protected File getWorkflowSourceDirectory() {
                    return null;
                }
            };
            TransientScottyEngine engine = factory.create();
            engine.run("Workflow1", "foo");
            Thread.sleep(10000);
            wfRepo.setVersion("1.0");
            Thread.sleep(10000);
            engine.run("Workflow1", "foo");
            Thread.sleep(10000);
            wfRepo.setVersion("2.0");
            Thread.sleep(10000);
            engine.run("Workflow1", "foo");
        } finally {
            wfRepo.shutdown();
        }
    }
}
