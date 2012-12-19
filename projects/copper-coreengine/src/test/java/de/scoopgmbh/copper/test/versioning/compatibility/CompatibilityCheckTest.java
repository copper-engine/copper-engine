/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.test.versioning.compatibility;

import java.io.Serializable;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;
import junit.framework.TestCase;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowFactory;
import de.scoopgmbh.copper.persistent.SerializedWorkflow;
import de.scoopgmbh.copper.persistent.Serializer;

public class CompatibilityCheckTest extends TestCase {
	
	private static final Logger logger = LoggerFactory.getLogger(CompatibilityCheckTest.class);

	public void testCheckCompatibility() throws Exception, InterruptException {
		TestWorkflowRepository repo = new TestWorkflowRepository();
		repo.setSourceDirs(Arrays.asList(new String[] {"./src/workflow/java"}));
		repo.setTargetDir("./build/compiled_workflow/"+Long.toHexString(System.currentTimeMillis()));
		repo.start();
		TestEngine engine = new TestEngine();
		try {
			doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_0001",true);
			doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_0002",true);
			doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_0003",true);
			doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_0004",true);
			doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_0005",true);
			
			doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_E001",false);
			doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_E002",false);
			doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_E003",false);
			//doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_E004",false);
			//doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.CompatibilityCheckWorkflow_E005",false);

			doTest(repo, engine, "de.scoopgmbh.copper.test.versioning.compatibility.check2.CompatibilityCheckWorkflow_Base", "de.scoopgmbh.copper.test.versioning.compatibility.check2.CompatibilityCheckWorkflow_E101",false);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			repo.shutdown();
		}

	}

	private void doTest(TestWorkflowRepository repo, TestEngine engine, String baseClass, String compatibleClass, boolean successExpected) throws InterruptException, Exception {
		try {
			TestJavaSerializer serializer = new TestJavaSerializer();
			serializer.setClassNameReplacement(compatibleClass);
			WorkflowFactory<Serializable> factory = repo.createWorkflowFactory(baseClass);
			Workflow<Serializable> wf = factory.newInstance();
			wf.setEngine(engine);
			wf.__beforeProcess();
			try {
				wf.main();
				fail("Expected InterruptException");
			}
			catch(InterruptException e) {
				// ok
			}
			final SerializedWorkflow checkPoint1 = serializer.serializeWorkflow(wf);

			wf.setEngine(engine);
			wf.__beforeProcess();
			try {
				wf.main();
				fail("Expected InterruptException");
			}
			catch(InterruptException e) {
				// ok
			}
			final SerializedWorkflow checkPoint2 = serializer.serializeWorkflow(wf);

			wf.setEngine(engine);
			wf.__beforeProcess();
			wf.main();

			checkCompatibility(checkPoint1, checkPoint2, baseClass, compatibleClass, serializer, engine, repo);

			if (!successExpected) {
				fail("Expected an exception");
			}
			
		}
		catch(Exception e) {
			if (successExpected) {
				throw e;
			}
			logger.info("Caught expected exception "+e.toString(),e);
		}
	}

	private void checkCompatibility(SerializedWorkflow cp1, SerializedWorkflow cp2, String baseClass, String className, Serializer serializer, ProcessingEngine engine, TestWorkflowRepository repo) throws Exception {
		repo.triggerClassname = baseClass;
		repo.overrideClassname = className;
		Workflow<?> wf1 = serializer.deserializeWorkflow(cp1, repo);
		Assert.assertEquals(className, wf1.getClass().getName());
		doRun(engine, wf1,1);
		Workflow<?> wf2 = serializer.deserializeWorkflow(cp2, repo);
		Assert.assertEquals(className, wf1.getClass().getName());
		doRun(engine, wf2,0);

	}

	private void doRun(ProcessingEngine engine, Workflow<?> wf, int numbOfIterations) {
		for (int i=0; i<numbOfIterations; i++) {
			wf.setEngine(engine);
			wf.__beforeProcess();
			try {
				wf.main();
				fail("expected InterruptException");
			} 
			catch (InterruptException e) {
				// ok
			}
		}
		wf.setEngine(engine);
		wf.__beforeProcess();
		try {
			wf.main();
		} 
		catch (InterruptException e) {
			fail("unexpected exception");
		}
	}

}
