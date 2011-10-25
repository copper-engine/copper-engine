/*
 * Copyright 2002-2011 SCOOP Software GmbH
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

import java.util.List;

import de.scoopgmbh.copper.ProcessingEngine;

public interface ProcessorPoolManager<T extends ProcessorPool> {
	public void setEngine(ProcessingEngine engine);
	public T getProcessorPool(String poolId); 
	public List<String> getProcessorPoolIds();
	public void setProcessorPools(List<T> processorPools);
	public void addProcessorPool(T pool);
	public void removeProcessorPool(String poolId);
	public void startup();
	public void shutdown();
}
