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
package org.copperengine.core.batcher;

import java.sql.Connection;
import java.util.Collection;

/**
 * Base interface of a BatchExecutor. A batch executor is responsible to process a batch of {@link BatchCommand}s.
 * A batcher is responsible to collect a batch of commands and call their executor to execute the batch.
 *
 * @param <T>
 * @author austermann
 */
public interface BatchExecutorBase<E extends BatchExecutorBase<E, T>, T extends BatchCommand<E, T>> {

    /**
     * Executes a batch of commands
     */
    void doExec(Collection<BatchCommand<E, T>> commands, Connection connection) throws Exception;

    /**
     * Preferred batch size of this executor
     */
    int preferredBatchSize();

    /**
     * @return the maximum batch size of this executor
     */
    int maximumBatchSize();

    boolean prioritize();

    /**
     * Unique ID of this batcher.
     */
    String id();

}
