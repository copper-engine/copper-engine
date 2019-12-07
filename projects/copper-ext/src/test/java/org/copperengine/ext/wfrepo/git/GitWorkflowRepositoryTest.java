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

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.DependencyInjector;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.copperengine.core.util.Backchannel;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.core.util.PojoDependencyInjector;
import org.copperengine.ext.util.Supplier2Provider;
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
        PojoDependencyInjector injector = new PojoDependencyInjector();
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

                protected DependencyInjector createDependencyInjector() {
                    return injector;
                }
            };
            TransientScottyEngine engine = factory.create();

            Backchannel channel = new BackchannelDefaultImpl();
            injector.register("backChannel", channel);


            engine.run("Workflow1", "foo");
            String result = (String) channel.wait("correlationId",1000, TimeUnit.MILLISECONDS);
            assertEquals("Vmaster", result);

            wfRepo.setVersion("1.0");
            Thread.sleep(3 * 1000); // wait for workflow refresh
            engine.run("Workflow1", "foo");
            result = (String) channel.wait("correlationId",1000, TimeUnit.MILLISECONDS);
            assertEquals("V1.0", result);

            wfRepo.setVersion("2.0");
            Thread.sleep(3 * 1000); // wait for workflow refresh
            engine.run("Workflow1", "foo");
            result = (String) channel.wait("correlationId",1000, TimeUnit.MILLISECONDS);
            assertEquals("V2.0", result);

        } finally {
            wfRepo.shutdown();
        }
    }
}
