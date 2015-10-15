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

package de.scoopgmbh.copper.wfrepo.classpath;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.junit.Assert;
import org.junit.Test;

public class ClasspathWorkflowRepositoryTest {

    private static final String TESTWORKFLOWS_PACKAGE = "org.copperengine.core.wfrepo.testworkflows";

    @Test
    public void testFindWorkflowClasses() throws Exception {
        Set<Class<?>> set = ClasspathWorkflowRepository.findWorkflowClasses(Collections.singletonList(TESTWORKFLOWS_PACKAGE), Thread.currentThread().getContextClassLoader());
        Assert.assertEquals(4, set.size());
        Assert.assertNotNull(set.contains(Class.forName("org.copperengine.core.wfrepo.testworkflows.TestWorkflowThree")));
    }

    @Test
    public void testExec() throws Exception {
        final ClasspathWorkflowRepository wfRepo = new ClasspathWorkflowRepository(TESTWORKFLOWS_PACKAGE);
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
            engine.run("org.copperengine.core.wfrepo.testworkflows.TestWorkflowThree", "foo");
            Thread.sleep(1000);
        } finally {
            // wfRepo.shutdown();
        }
    }
}
