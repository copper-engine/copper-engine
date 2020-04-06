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
package org.copperengine.core.batcher;

import java.sql.Connection;
import java.util.Collection;

/**
 * Base interface of a BatchExecutor. A batch executor is responsible to process a batch of {@link BatchCommand}s.
 * A batcher is responsible to collect a batch of commands and call their executor to execute the batch.
 *
 * @param <E> type of the BatchExecutorBase
 * @param <T> type of the BatchCommand to be executed by the executor implementation
 * @author austermann
 */
public interface BatchExecutorBase<E extends BatchExecutorBase<E, T>, T extends BatchCommand<E, T>> {

    /**
     * Executes a batch of commands
     * @param commands  the batch of commands is collected by the batcher and passed in to this method which can then
     *                  execute them all in one batch.
     * @param connection the connection to work on
     * @throws Exception any kind of uncaught exception with the implementation of the executor
     */
    void doExec(Collection<BatchCommand<E, T>> commands, Connection connection) throws Exception;

    /**
     * @return Preferred batch size of this executor
     */
    int preferredBatchSize();

    /**
     * @return the maximum batch size of this executor
     */
    int maximumBatchSize();

    boolean prioritize();

    /**
     * @return unique ID of this batcher.
     */
    String id();

}
