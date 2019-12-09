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
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of {@link FileBasedWorkflowRepository}, that adds an observer of a git repository.
 *
 * The {@link #setBranch(String)} branch in {@link #setOriginURI(String)} is pulled for changes. The
 * changes are applyed online. The interval {@link #setCheckIntervalMSec(int)} is also used in the FileBasedWorkflowRepository.
 *
 * This class enables a lightweight CI/CD pipeline using git repositories only for changes in workflows. Changes
 * outside the workflows are not in scope of this pipeline.
 *
 * @author wsluyterman
 */
public class GitWorkflowRepository extends FileBasedWorkflowRepository implements WorkflowRepository, FileBasedWorkflowRepositoryMXBean {


    /**
     * Used as remote name: "origin". Not changed.
     */
    public static final String ORIGIN = "origin";

    /**
     * Default name "master". Changeable with {@link #setBranch(String)}
     */
    public static final String DEFAULT_BRANCH = "master";

    private static final Logger logger = LoggerFactory.getLogger(GitWorkflowRepository.class);

    /**
     * Used for runtime exception in this class.
     */
    public static class GitWorkflowRepositoryException extends RuntimeException {
        public GitWorkflowRepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    private String originUri;
    private String branch = DEFAULT_BRANCH;
    private File gitRepositoryDir;
    private CredentialsProvider credentialsProvider;

    private int checkIntervalMSecGit = 5000;
    private AtomicBoolean gitObserverStopped;
    private Thread observerThread;


    /**
     * Define the directory, where the local git clone is created expected to exists.
     * <p>
     * If the GitWorkflowRepository {@link #isUp()} isUp the a refesh incl. directory deletion is performed.
     *
     * @param gitRepositoryDir the new or same location of local git clone
     * @throws GitAPIException on exception in refresh
     * @throws IOException     on exception on directory deletion
     */
    public synchronized void setGitRepositoryDir(File gitRepositoryDir) throws IOException, GitAPIException {
        if (isUp()) {
            deleteGitRepositoryDir();
        }
        this.gitRepositoryDir = gitRepositoryDir;
        if (isUp()) {
            updateLocalGitRepositories();
        }
    }

    /**
     * Delegates to {@link #setGitRepositoryDir}.
     *
     * @param gitRepositoryDir see {@link #setGitRepositoryDir}.
     * @throws IOException see {@link #setGitRepositoryDir}.
     * @throws GitAPIException see {@link #setGitRepositoryDir}.
     */
    public synchronized void setGitRepositoryDir(String gitRepositoryDir) throws IOException, GitAPIException {
        setGitRepositoryDir(new File(gitRepositoryDir));
    }

    /**
     * @return a new file object for the current directory of the local repository clone
     */
    public synchronized File getGitRepositoryDir() {
        return new File (gitRepositoryDir.getAbsolutePath());
    }

    /**
     * @return the current originURI
     */
    public synchronized String getOriginUri() {
        return originUri;
    }

    /**
     * The current originUri is changed.
     *
     * * If the GitWorkflowRepository {@link #isUp()} isUp the a refesh incl. directory deletion is performed.
     *
     * @param originUri new origin URI
     * @throws GitAPIException on exception in refresh
     * @throws IOException     on exception on directory deletion
     */
    public synchronized void setOriginURI(String originUri) throws IOException, GitAPIException {
        this.originUri = originUri;
        if (isUp()) {
            deleteGitRepositoryDir();
            updateLocalGitRepositories();
        }
    }


    /**
     * Sets the credentials used in next clone
     *
     * @param username selfexplaining
     * @param password selfexplaining
     */
    public synchronized void setCredentials(String username, char[] password) {
        this.credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
    }

    /**
     * Changes the used branch and refreshes the the local repository clone.
     *
     * @param branch new branch
     *
     * @throws GitAPIException on exception in refresh
     * @throws IOException     on exception in refresh
    */
    public synchronized void setBranch(String branch) throws IOException, GitAPIException {
        this.branch = branch;
        if (isUp()) {
            updateLocalGitRepositories();
        }
    }

    /**
     * Defines the poll interval for changes in a branch.
     *
     * @param checkIntervalMSec also given to {@link FileBasedWorkflowRepository} as the underlying file pol
     */
    @Override
    public synchronized void setCheckIntervalMSec(int checkIntervalMSec) {
        this.checkIntervalMSecGit = checkIntervalMSec;
        super.setCheckIntervalMSec(checkIntervalMSec);
    }

    /**
     * Starts the repository poll activities and refreshes the the local repository clone.
     *
     * @throws IllegalStateException if another start already was executed
     * @throws  GitWorkflowRepositoryException on exception in refresh
     */
    @Override
    public synchronized void start() {
        logger.info("Starting git workflow repository.");
        if (gitObserverStopped != null)
            throw new IllegalStateException("Git workflow repository can only be startet once.");
        try {
            updateLocalGitRepositories();
        } catch (Exception e) {
            throw new GitWorkflowRepositoryException("Exception while initial update of it workflow repository.", e);
        }
        this.gitObserverStopped = new AtomicBoolean(false);
        observerThread = new GitWorkflowRepository.ObserverThread(this);
        observerThread.start();
        super.start();
    }

    /**
     * Shuts down  the repository poll activities on git repository and files.
     */
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

    /**
     * @return true, if repository start was sucessful and no shutdown was requested.
     */
    public synchronized boolean isUp() {
        return gitObserverStopped != null && !gitObserverStopped.get();
    }


    /**
     * Updates the used local repository, either with gut-pull or with git-checkout force with git pull.
     *
     * @throws IOException     in case of git open exception
     * @throws GitAPIException in case of exception in git calls
     */
    protected synchronized void updateLocalGitRepositories() throws IOException, GitAPIException {
        logger.debug("Update git repositories.");
        if (!getGitRepositoryDir().exists()) {
            logger.info("No local repository found. Clone a new one. Branch is {}.", branch);
            try (Git git = Git.cloneRepository()
                    .setURI(originUri)
                    .setDirectory(getGitRepositoryDir())
                    .setRemote(ORIGIN)
                    .setCredentialsProvider(credentialsProvider)
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

    /**
     * Deletes the whole tree in {@link #getGitRepositoryDir()}.
     *
     * @throws IOException if exception in deleting is thrown.
     */
    protected synchronized void deleteGitRepositoryDir() throws IOException {
        logger.info("Delete repository dir {}.", getGitRepositoryDir());
        if (getGitRepositoryDir() != null) {
            FileUtils.deleteDirectory(getGitRepositoryDir());
        }
    }

    private static class ObserverThread extends Thread {
        private static final Logger logger = LoggerFactory.getLogger(GitWorkflowRepository.ObserverThread.class);

        private final WeakReference<GitWorkflowRepository> gitWorkflowRepository;

        private ObserverThread(GitWorkflowRepository repository) {
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
}
