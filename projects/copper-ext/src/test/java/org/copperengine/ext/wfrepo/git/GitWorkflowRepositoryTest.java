/*
 * Copyright 2002-2019 SCOOP Software GmbH
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.copperengine.core.CopperException;
import org.copperengine.core.DependencyInjector;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.copperengine.core.util.Backchannel;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.core.util.PojoDependencyInjector;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GitWorkflowRepositoryTest {

    public static final String WF_WORK = "wf-work";
    public static final String WORK_DIR = "./" + WF_WORK;
    public static final int CHECK_INTERVAL_M_SEC = 1000;

    private GitWorkflowRepository wfRepo;
    private TransientScottyEngine engine;
    private Backchannel channel;

    @BeforeEach
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(WORK_DIR));
        final boolean ignored = new File(WORK_DIR).mkdirs();
        unzip(this.getClass().getClassLoader().getResource("git-wf.zip").openStream(), WORK_DIR);

        wfRepo = new GitWorkflowRepository();
        wfRepo.setGitRepositoryDir(WORK_DIR + "/wf-source");
        wfRepo.addSourceDir(WORK_DIR + "/wf-source");
        wfRepo.setTargetDir(WORK_DIR + "/wf-target");
        wfRepo.setOriginURI("file://" + new File(WORK_DIR + "/git-wf").getAbsolutePath()); // http/s to be verified is system test
        setUpEngine();
    }

    private void setUpEngine() {
        wfRepo.setCheckIntervalMSec(CHECK_INTERVAL_M_SEC);
        PojoDependencyInjector injector = new PojoDependencyInjector();

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
        engine = factory.create();

        channel = new BackchannelDefaultImpl();
        injector.register("backChannel", channel);
    }


    @Test
    public void defaultBranchTest() throws Exception {
        engine.run("Workflow1", "foo");
        String result = (String) channel.wait("correlationId", 3000, TimeUnit.MILLISECONDS);
        assertEquals("Vmaster", result);
        assertEquals("master", wfRepo.getBranch());
    }

    @Test
    public void change2BranchesTest() throws CopperException, InterruptedException, IOException, GitAPIException {
        wfRepo.setBranch("1.0");
        LockSupport.parkNanos(3_000_000_000L + CHECK_INTERVAL_M_SEC * 1_000_000); // wait for workflow refresh
        engine.run("Workflow1", "foo");
        String result1 = (String) channel.wait("correlationId", 1000, TimeUnit.MILLISECONDS);
        assertEquals("V1.0", result1);

        wfRepo.setBranch("2.0");
        LockSupport.parkNanos(3_000_000_000L + CHECK_INTERVAL_M_SEC * 1_000_000); // wait for workflow refresh
        engine.run("Workflow1", "foo");
        String result2 = (String) channel.wait("correlationId", 1000, TimeUnit.MILLISECONDS);
        assertEquals("V2.0", result2);
    }

    @Test
    public void shutdownTest() {
        assertTrue(wfRepo.isUp(), "wfRepos should be up.");
        wfRepo.shutdown();
        assertFalse(wfRepo.isUp(), "wfRepos should be down.");
    }

    @Test
    public void shutdownStartTest() {
        assertTrue(wfRepo.isUp(), "wfRepos should be up.");
        wfRepo.shutdown();
        assertFalse(wfRepo.isUp(), "wfRepos should be down.");
        assertThrows(IllegalStateException.class, () -> wfRepo.start());
    }

    @Test
    public void shutdownDoubleStartTest() {
        assertTrue(wfRepo.isUp(), "wfRepos should be up.");
        assertThrows(IllegalStateException.class, () -> wfRepo.start());
    }

    @Test
    public void changeToTagTest() {
        assertThrows(RefNotAdvertisedException.class, () -> wfRepo.setBranch("2.0.0"));
    }


    @Test
    public void sameGitRepositoryDirTest() throws Exception {
        wfRepo.setGitRepositoryDir(WORK_DIR + "/wf-source");
        defaultBranchTest();
    }


    @Test
    public void changeGitRepositoryRobustDirTest() throws Exception {
        wfRepo.setGitRepositoryDir(WORK_DIR + "/wf-source2");
        LockSupport.parkNanos(1_000_000_000 + CHECK_INTERVAL_M_SEC * 1_000_000); // wait for workflow refresh
        defaultBranchTest(); // should run, because working classes are not overwritten (with empty configuration) by copper
    }

    @Test
    public void sameGitRepositoryFakeCredentialTest() throws Exception {
        wfRepo.setCredentials("test", "s3cret".toCharArray()); // will be ignored, credential feature to be verified is system test
        wfRepo.setGitRepositoryDir(WORK_DIR + "/wf-source");
        defaultBranchTest();
    }

    @Test
    public void changeGitRepositoryDirTest() throws Exception {
        wfRepo.setGitRepositoryDir(WORK_DIR + "/wf-source2");
        List<String> sourceDirs = new ArrayList<String>(1);
        sourceDirs.add(0, WORK_DIR + "/wf-source2");
        wfRepo.setSourceDirs(sourceDirs);
        LockSupport.parkNanos(3_000_000_000L + CHECK_INTERVAL_M_SEC * 1_000_000); // wait for workflow refresh
        change2BranchesTest(); // should run, because working classes are not overwritten (with empty configuration) by copper
    }

    @Test
    public void changeGitRepositoryFailureDirTest() throws Exception {
        wfRepo.setGitRepositoryDir(WORK_DIR + "/wf-source2");
        LockSupport.parkNanos(1_000_000_000 + CHECK_INTERVAL_M_SEC * 1_000_000); // wait for workflow refresh
        defaultBranchTest();
        wfRepo.setBranch("1.0");
        LockSupport.parkNanos(1_000_000_000 + CHECK_INTERVAL_M_SEC * 1_000_000); // wait for workflow refresh
        engine.run("Workflow1", "foo");
        String result1 = (String) channel.wait("correlationId", 1000, TimeUnit.MILLISECONDS);
        assertEquals("Vmaster", result1, "new branch not loaded, so expect Vmaster");
    }

    @Test
    public void sameURITest() throws Exception {
        wfRepo.setOriginURI(wfRepo.getOriginUri());
        defaultBranchTest();
    }


    @Test
    public void changeURITest() throws Exception {
        assertThrows(InvalidRemoteException.class, ()-> wfRepo.setOriginURI(wfRepo.getOriginUri() + "ERROR_TEST"));
    }

    @Test
    public void startFailureEmptyRepoTest() throws Exception {
        GitWorkflowRepository wfRepo2 = new GitWorkflowRepository();
        assertThrows(GitWorkflowRepository.GitWorkflowRepositoryException.class, wfRepo2::start);

    }

    @Test
    public void startFailureTargetDirTest() throws Exception {
        GitWorkflowRepository wfRepo2 = new GitWorkflowRepository();
        wfRepo2.setTargetDir(WORK_DIR + "/wf-target2");
        assertThrows(GitWorkflowRepository.GitWorkflowRepositoryException.class, wfRepo2::start);
    }

    @AfterEach
    public void setDown() throws IOException {
        engine.shutdown();
        FileUtils.deleteDirectory(new File(WF_WORK));
    }

    private static void unzip(InputStream inputStream, String out) throws Exception {

        byte[] buffer = new byte[2048];

        Path outDir = Paths.get(out);

        try (
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                ZipInputStream stream = new ZipInputStream(bis)) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                Path filePath = outDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    filePath.toFile().mkdirs();
                } else {
                    try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
                         BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {
                        int len;
                        while ((len = stream.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }
}
