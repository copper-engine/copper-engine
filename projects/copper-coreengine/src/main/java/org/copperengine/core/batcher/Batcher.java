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
 * A batcher is a service that collects homogeneous tasks over a period of time and executes them as a batch.
 * This may increase dramatically the throughput for a kind of tasks.
 * A typical usage for the Batcher is database batching. It collects simple insert, update oder delete statements
 * and executes them as a batch. This increases throughput mostly by a factor of 10 or more.
 * Of course, batching may lead to longer latency times.
 *
 * @author austermann
 */
public interface Batcher {

    public <E extends BatchExecutor<E, T>, T extends BatchCommand<E, T>> void submitBatchCommand(BatchCommand<E, T> cmd);

}
