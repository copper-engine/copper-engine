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
package de.scoopgmbh.copper;

/**
 * Glue interface for adding dependency injection capatibilty to COPPER.
 * Implementations of this interface connect a container, e.g. Spring, with COPPER and enables COPPER
 * to inject dependencies to workflow instances.
 * 
 * @see de.scoopgmbh.copper.spring.SpringDependencyInjector
 * 
 * @author austermann
 *
 */
public interface DependencyInjector {
	public void setEngine(ProcessingEngine e);
	public void inject(Workflow<?> workflow);
	public String getType();
}
