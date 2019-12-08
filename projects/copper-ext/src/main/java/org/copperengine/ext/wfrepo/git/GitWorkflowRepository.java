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

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;
import org.copperengine.management.FileBasedWorkflowRepositoryMXBean;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of {@link FileBasedWorkflowRepository}, that adds an observer of a git repository.
 *
 * @author wsluyterman
 */
public class GitWorkflowRepository extends FileBasedWorkflowRepository implements WorkflowRepository, FileBasedWorkflowRepositoryMXBean {

    private static final Logger logger = LoggerFactory.getLogger(GitWorkflowRepository.class);

    private static final String ORIGIN = "origin";
    public static final String DEFAULT_BRANCH = "master";

    private String originUri;
    private String branch = DEFAULT_BRANCH;
    private File gitRepositoryDir;

    private int checkIntervalMSecGit = 5000;
    private AtomicBoolean gitObserverStopped;
    private Thread observerThread;


    public boolean isUp() {
        return gitObserverStopped != null && !gitObserverStopped.get();
    }

    public synchronized void setGitRepositoryDir(File gitRepositoryDir) throws IOException {
        deleteGitRepositoryDir();
        this.gitRepositoryDir = gitRepositoryDir;
    }

    public synchronized void setGitRepositoryDir(String gitRepositoryDir) throws IOException {
        setGitRepositoryDir(new File(gitRepositoryDir));
    }

    public synchronized File getGitRepositoryDir() {
        return gitRepositoryDir;
    }

    public synchronized String getOriginUri() {
        return originUri;
    }

    public synchronized void setOriginURI(String uri) throws IOException, GitAPIException {
        this.originUri = uri;
        if (isUp()) {
            deleteGitRepositoryDir();
            updateLocalGitRepositories();
        }
    }

    public synchronized void setBranch(String branch) throws IOException, GitAPIException {
        this.branch = branch;
        if (isUp()) {
            updateLocalGitRepositories();
        }
    }

    @Override
    public synchronized void setCheckIntervalMSec(int checkIntervalMSec) {
        this.checkIntervalMSecGit = checkIntervalMSec;
        super.setCheckIntervalMSec(checkIntervalMSec);
    }

    @Override
    public synchronized void start() {
        logger.info("Starting git workflow repository.");
        if (gitObserverStopped != null)
            throw new IllegalStateException("Git workflow repository can only be startet once.");
        try {
            updateLocalGitRepositories();
        } catch (Exception e) {
            throw  new GitWorkflowRepositoryException("Exception while initial update of it workflow repository.", e);
        }
        this.gitObserverStopped = new AtomicBoolean(false);
        observerThread = new GitWorkflowRepository.ObserverThread(this);
        observerThread.start();
        super.start();
    }

    @Override
    public synchronized void shutdown() {
        logger.info("Shutting down git workflow repository.");
        this.gitObserverStopped.set(true);
        observerThread.interrupt();
        try {
            observerThread.join();
        } catch (Exception e) {
            /* ignore */
        }
        super.shutdown();
    }

    protected synchronized void updateLocalGitRepositories() throws IOException, GitAPIException {
        logger.debug("Update git repositories.");
        if (!getGitRepositoryDir().exists()) {
            logger.info("No local repository found. Clone a new one. Branch is {}.", branch);
            try (Git git = Git.cloneRepository()
                    .setURI(originUri)
                    .setDirectory(getGitRepositoryDir())
                    .setRemote(ORIGIN)
                    .call();) {
                git.checkout().setName("remotes/" + ORIGIN + "/" + branch).call();
            }
        } else {
            try (Git git = Git.open(getGitRepositoryDir())) {
                Repository repository = git.getRepository();
                logger.debug("Local repository {} found. Checkout branch {} force.", repository, branch);
                logger.trace("Repository {}: bare={}.", repository, repository.isBare());
                git.checkout()
                        .setName(ORIGIN + "/" + branch)
                        .setForced(true)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();
                logger.debug("Pull repository {}", repository);
                git.pull().setRemote(ORIGIN).setRemoteBranchName(branch).call();
                logger.debug("Local repository {} found. Again checkout branch {} force.", repository, branch);
                git.checkout()
                        .setName(ORIGIN + "/" + branch)
                        .setForced(true)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();
            }
        }
    }

    protected synchronized void deleteGitRepositoryDir() throws IOException {
        logger.info("Delete repository dir {}.", getGitRepositoryDir());
        if (getGitRepositoryDir() != null) {
            FileUtils.deleteDirectory(getGitRepositoryDir());
        }
    }

    private static class ObserverThread extends Thread {
        private static final Logger logger = LoggerFactory.getLogger(GitWorkflowRepository.ObserverThread.class);

        final WeakReference<GitWorkflowRepository> gitWorkflowRepository;

        public ObserverThread(GitWorkflowRepository repository) {
            super("WfRepoObserverGit");
            this.gitWorkflowRepository = new WeakReference<>(repository);
            setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Starting git observation");
            GitWorkflowRepository repository = this.gitWorkflowRepository.get();

            while (repository != null && !repository.gitObserverStopped.get()) {
                try {
                    Thread.sleep(repository.checkIntervalMSecGit);
                    repository.updateLocalGitRepositories();
                } catch (Exception e) {
                    logger.info("Exception caught while git oberservation. Continue anyway.", e);
                }
            }
            logger.info("Stopping git observation");
        }
    }

    /**
     * Used for runtime exception in this class.
     */
    public static class GitWorkflowRepositoryException extends RuntimeException {
        public GitWorkflowRepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
