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

/**
 * A BatchCommand is one task executed in a Batch.
 * BatchCommands with the same executor are batched by the {@link Batcher}
 * 
 * @author austermann
 * @param <E>
 * @param <T>
 */
public interface BatchCommand<E extends BatchExecutorBase<E, T>, T extends BatchCommand<E, T>> {

    /**
     * returns the executor for this BatchCommand.
     */
    E executor();

    /**
     * returns the callback called by the batcher, after this command has been processed. Used for asynchronously
     * notifying the caller.
     */
    CommandCallback<T> callback();

    /**
     * returns the targetTime timestamp in java milliseconds until which this batch command shall be executed.
     * This value influences how long the used batcher is collecting BatchCommands with the same executor.
     */
    long targetTime();

}
