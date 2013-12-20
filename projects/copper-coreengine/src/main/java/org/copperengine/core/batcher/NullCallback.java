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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation of the {@link CommandCallback} interface.
 * Successful command executions are ignored, exceptions are logged.
 *
 * @param <T>
 * @author austermann
 */
public class NullCallback<T extends BatchCommand<?, T>> implements CommandCallback<T> {

    static final Logger logger = LoggerFactory.getLogger(NullCallback.class);

    @SuppressWarnings("rawtypes")
    public static final NullCallback instance = new NullCallback();

    @Override
    public void commandCompleted() {
    }

    @SuppressWarnings("unchecked")
    public static <X extends BatchCommand<?, X>> CommandCallback<X> get() {
        return (CommandCallback<X>) instance;
    }

    @Override
    public void unhandledException(Exception e) {
        logger.error("Unhandled exception occurred", e);

    }

}
