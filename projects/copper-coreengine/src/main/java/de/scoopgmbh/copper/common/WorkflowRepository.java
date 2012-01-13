/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.common;

import de.scoopgmbh.copper.WorkflowFactory;


/**
 * A WorkflowRepository is a container for COPPER workflows.
 * It encapsulates the handling and storage of workflows and makes the workflow classes accessible to one or more COPPER
 * {@link de.scoopgmbh.copper.ProcessingEngine}s.
 *  
 * @author austermann
 *
 */
public interface WorkflowRepository {

	public <E> WorkflowFactory<E> createWorkflowFactory(final String classname) throws ClassNotFoundException;
	public java.lang.Class<?> resolveClass(	java.io.ObjectStreamClass desc) throws java.io.IOException, ClassNotFoundException;
	public void start();
	public void shutdown();

}