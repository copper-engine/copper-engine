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
package org.copperengine.core.common;

import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowFactory;
import org.copperengine.core.WorkflowVersion;
import org.copperengine.core.instrument.ClassInfo;

/**
 * A WorkflowRepository is a container for COPPER workflows.
 * It encapsulates the handling and storage of workflows and makes the workflow classes accessible to one or more COPPER
 * {@link org.copperengine.core.ProcessingEngine}s.
 *
 * @author austermann
 */
public interface WorkflowRepository {

    public <E> WorkflowFactory<E> createWorkflowFactory(final String wfName) throws ClassNotFoundException;

    public <E> WorkflowFactory<E> createWorkflowFactory(final String wfName, final WorkflowVersion version) throws ClassNotFoundException;

    public WorkflowVersion findLatestMajorVersion(final String wfName, long majorVersion);

    public WorkflowVersion findLatestMinorVersion(final String wfName, long majorVersion, long minorVersion);

    public java.lang.Class<?> resolveClass(String classname) throws java.io.IOException, ClassNotFoundException;

    ClassInfo getClassInfo(@SuppressWarnings("rawtypes") final Class<? extends Workflow> wfClazz) throws java.io.IOException, ClassNotFoundException;

    public void start();

    public void shutdown();

}