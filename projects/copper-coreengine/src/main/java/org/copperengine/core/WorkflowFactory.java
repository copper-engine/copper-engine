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
package org.copperengine.core;

/**
 * Factory for workflow instances.
 * A WorkflowFactory is requested a the corresponding engine.
 *
 * @param <D>
 *        type of data passed in to the workflow on creation.
 * @author austermann
 */
public interface WorkflowFactory<D> {
    /**
     * @return
     *         new instance of the workflow
     * @throws InstantiationException
     *         {@link Class#newInstance()}
     *         "if this Class represents an abstract class, an interface, an array class, a primitive type, or void;
     *         or if the class has no nullary constructor; or if the instantiation fails for some other reason."
     * @throws IllegalAccessException
     *         {@link Class#newInstance()}
     *         "if the class or its nullary constructor is not accessible."
     */
    public Workflow<D> newInstance() throws InstantiationException, IllegalAccessException;
}
