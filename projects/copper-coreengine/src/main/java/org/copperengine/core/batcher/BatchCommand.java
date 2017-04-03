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

/**
 * A BatchCommand is one task executed in a Batch.
 * BatchCommands with the same executor are batched by the {@link Batcher}.
 * Usually, a BatchCommand should hold the data unique to each command, where the executor holds the sql-queries and
 * puts the data from all commands into the queries.
 *
 * @param <E> type of the BatchExecutor
 * @param <T> type of the BatchCommand to be executed
 * @author austermann
 */
public interface BatchCommand<E extends BatchExecutorBase<E, T>, T extends BatchCommand<E, T>> {

    /**
     * @return the executor for this BatchCommand.
     */
    E executor();

    /**
     * @return the callback called by the batcher, after this command has been processed. Used for asynchronously
     * notifying the caller.
     */
    CommandCallback<T> callback();

    /**
     * @return the targetTime timestamp in java milliseconds until which this batch command shall be executed.
     * This value influences how long the used batcher is collecting BatchCommands with the same executor.
     */
    long targetTime();

}
