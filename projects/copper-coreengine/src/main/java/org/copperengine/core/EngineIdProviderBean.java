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
package org.copperengine.core;

/**
 * Simple java bean implementation if the {@link EngineIdProvider} interface.
 *  
 * @author austermann
 *
 */
public class EngineIdProviderBean implements EngineIdProvider {

	private String engineId;
	
	public EngineIdProviderBean() {
		
	}
	
	public EngineIdProviderBean(String engineId) {
		if (engineId == null) throw new NullPointerException();
		this.engineId = engineId;
	}
	
	public void setEngineId(String engineId) {
		if (engineId == null) throw new NullPointerException();
		this.engineId = engineId;
	}
	
	@Override
	public String getEngineId() {
		return engineId;
	}

}
