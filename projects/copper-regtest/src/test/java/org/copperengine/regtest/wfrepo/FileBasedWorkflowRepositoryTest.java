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
package org.copperengine.regtest.wfrepo;

import org.copperengine.core.WorkflowFactory;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;
import org.copperengine.management.model.WorkflowClassInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileBasedWorkflowRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedWorkflowRepositoryTest.class);

    @Test
    public void testCreateWorkflowFactory_ClassNotFound() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
        repo.addSourceDir("src/workflow/java");
        repo.setTargetDir("build/compiled_workflow");
        repo.start();
        try {
            assertThrows(ClassNotFoundException.class, () -> {
                WorkflowFactory<Object> factory = repo.createWorkflowFactory("foo");
                factory.newInstance();
            });
        } finally {
            repo.shutdown();
        }

    }

    @Test()
    public void testCreateWorkflowFactory() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
        repo.addSourceDir("src/workflow/java");
        repo.setTargetDir("build/compiled_workflow");
        repo.start();
        try {
            List<WorkflowClassInfo> wfClassInfos = repo.getWorkflows();
            logger.info("wfClassInfos.size={}", wfClassInfos.size());
            Assertions.assertTrue(wfClassInfos.size() >= 54);
            boolean checked = false;
            for (WorkflowClassInfo wfi : wfClassInfos) {
                if (wfi.getClassname().equals("org.copperengine.regtest.test.versioning.VersionTestWorkflow_9_1_1")) {
                    Assertions.assertEquals("VersionTestWorkflow", wfi.getAlias());
                    Assertions.assertEquals(9L, wfi.getMajorVersion().longValue());
                    Assertions.assertEquals(1L, wfi.getMinorVersion().longValue());
                    Assertions.assertEquals(1L, wfi.getPatchLevel().longValue());
                    Assertions.assertNotNull(wfi.getSourceCode());
                    checked = true;
                    break;
                }
            }
            Assertions.assertTrue(checked);
        } finally {
            repo.shutdown();
        }

    }
}
