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
package org.copperengine.regtest.test.versioning.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Arrays;

import org.copperengine.core.Interrupt;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowFactory;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.persistent.SerializedWorkflow;
import org.copperengine.core.persistent.Serializer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompatibilityCheckTest {

    private static final Logger logger = LoggerFactory.getLogger(CompatibilityCheckTest.class);

    @Test
    public void testCheckCompatibility() throws Exception, Interrupt {
        TestWorkflowRepository repo = new TestWorkflowRepository();
        repo.setSourceDirs(Arrays.asList(new String[] { "./src/workflow/java" }));
        repo.setTargetDir("./build/compiled_workflow/" + Long.toHexString(System.currentTimeMillis()));
        repo.start();
        TestEngine engine = new TestEngine();
        try {
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_0001", true, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_0002", true, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_0003", true, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_0004", true, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_0005", true, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_0006", true, false);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_0007", true, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_0008", true, true);

            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_E001", false, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_E002", false, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_E003", false, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_E004", false, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_E005", false, true);
            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.CompatibilityCheckWorkflow_E006", false, true);

            doTest(repo, engine, "org.copperengine.regtest.test.versioning.compatibility.check2.CompatibilityCheckWorkflow_Base", "org.copperengine.regtest.test.versioning.compatibility.check2.CompatibilityCheckWorkflow_E101", false, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            repo.shutdown();
        }

    }

    private void doTest(TestWorkflowRepository repo, TestEngine engine, String baseClass, String compatibleClass, boolean successExpected, boolean checkIterations) throws Interrupt, Exception {
        try {
            TestJavaSerializer serializer = new TestJavaSerializer();
            serializer.setClassNameReplacement(compatibleClass);
            WorkflowFactory<Serializable> factory = repo.createWorkflowFactory(baseClass);
            Workflow<Serializable> wf = factory.newInstance();
            wf.setEngine(engine);
            wf.__beforeProcess();
            try {
                wf.main();
                fail("Expected Interrupt");
            } catch (Interrupt e) {
                // ok
            }
            final SerializedWorkflow checkPoint1 = serializer.serializeWorkflow(wf);

            wf.setEngine(engine);
            wf.__beforeProcess();
            try {
                wf.main();
                fail("Expected Interrupt");
            } catch (Interrupt e) {
                // ok
            }
            final SerializedWorkflow checkPoint2 = serializer.serializeWorkflow(wf);

            wf.setEngine(engine);
            wf.__beforeProcess();
            wf.main();

            checkCompatibility(checkPoint1, checkPoint2, baseClass, compatibleClass, serializer, engine, repo, checkIterations);

            if (!successExpected) {
                fail("Expected an exception");
            }

        } catch (Exception e) {
            if (successExpected) {
                throw e;
            }
            logger.info("Caught expected exception " + e.toString(), e);
        }
    }

    private void checkCompatibility(SerializedWorkflow cp1, SerializedWorkflow cp2, String baseClass, String className, Serializer serializer, ProcessingEngine engine, TestWorkflowRepository repo, boolean checkIterations) throws Exception {
        repo.triggerClassname = baseClass;
        repo.overrideClassname = className;
        Workflow<?> wf1 = serializer.deserializeWorkflow(cp1, repo);
        assertEquals(className, wf1.getClass().getName());
        if (checkIterations)
            doRun(repo, serializer, engine, wf1, 1);
        else
            doRun(repo, serializer, engine, wf1);
        Workflow<?> wf2 = serializer.deserializeWorkflow(cp2, repo);
        assertEquals(className, wf1.getClass().getName());
        if (checkIterations)
            doRun(repo, serializer, engine, wf2, 0);
        else
            doRun(repo, serializer, engine, wf2);

    }

    private void doRun(WorkflowRepository wfRepo, Serializer serializer, ProcessingEngine engine, Workflow<?> wf, int numbOfIterations) throws Exception {
        for (int i = 0; i < numbOfIterations; i++) {
            wf.setEngine(engine);
            wf.__beforeProcess();
            try {
                wf.main();
                fail("expected Interrupt");
            } catch (Interrupt e) {
                // ok
                // check that we can still serialize and deserialize the workflow instance
                SerializedWorkflow swf = serializer.serializeWorkflow(wf);
                wf = serializer.deserializeWorkflow(swf, wfRepo);
            }
        }
        wf.setEngine(engine);
        wf.__beforeProcess();
        try {
            wf.main();
        } catch (Interrupt e) {
            fail("unexpected exception");
        }
    }

    private void doRun(WorkflowRepository wfRepo, Serializer serializer, ProcessingEngine engine, Workflow<?> wf) throws Exception {
        for (;;) {
            wf.setEngine(engine);
            wf.__beforeProcess();
            try {
                wf.main();
                break;
            } catch (Interrupt e) {
                // ok
                // check that we can still serialize and deserialize the workflow instance
                SerializedWorkflow swf = serializer.serializeWorkflow(wf);
                wf = serializer.deserializeWorkflow(swf, wfRepo);
            }
        }
    }
}
