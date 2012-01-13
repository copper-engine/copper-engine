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
package de.scoopgmbh.copper.tranzient;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.ProcessorPool;

/**
 * Base interface for a {@link ProcessorPool} used in a transient {@link ProcessingEngine}
 * 
 * @author austermann
 *
 */
public interface TransientProcessorPool extends ProcessorPool {
	public static final String DEFAULT_POOL_ID = "T#DEFAULT";
	public void enqueue(Workflow<?> wf);
}
