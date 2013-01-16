/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.test.sourcearchive;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.wfrepo.FileBasedWorkflowRepository;

import static org.junit.Assert.assertNotNull;

public class SourceArchiveTest {
	
	private Logger logger = LoggerFactory.getLogger(SourceArchiveTest.class);

	@Test
	public void testSourceArchive() throws Exception {
		FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
		String url = new File("src/workflow_archive/workflow_archive.jar").toURI().toURL().toString();
		logger.info("URL="+url);
		repo.addSourceArchiveUrl(url);
		repo.addSourceArchiveUrl(url);
		repo.addSourceArchiveUrl(url);
		repo.setTargetDir("build/compiled_workflow");
		repo.start();
		assertNotNull(repo.createWorkflowFactory("de.scoopgmbh.copper.archivetest.ArchiveTestWorkflow"));
		repo.shutdown();
	}
}
