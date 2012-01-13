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

import java.util.List;

import de.scoopgmbh.copper.ProcessingEngine;

/**
 * public interface for a timeout manager used in a transient processing engine.
 * The timeout manager is responsible to wake up waiting workflow instances in case of a timeout.
 * 
 * @author austermann
 *
 */
public interface TimeoutManager {
	public void setEngine(ProcessingEngine engine);
	public void registerTimeout(long timeoutTS, String correlationId);
	public void registerTimeout(long timeoutTS, List<String> correlationIds);
	public void unregisterTimeout(long timeoutTS, String correlationId);
	public void unregisterTimeout(long timeoutTS, List<String> correlationIds);
	public void startup();
	public void shutdown();
}
