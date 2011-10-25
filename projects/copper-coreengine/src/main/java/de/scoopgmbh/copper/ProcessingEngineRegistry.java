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
package de.scoopgmbh.copper;

import java.util.Map;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

@Deprecated
class ProcessingEngineRegistry implements InitializingBean {
	
	private Map<String,ProcessingEngine> engines;
	
	public void setEngines (Map<String,ProcessingEngine> engines) {
		this.engines = engines;
	}
	
	public ProcessingEngine getEngine (String name) {
		if ( name!=null ) {
			return engines.get(name);
		} else {
			return engines.values().iterator().next();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if ( this.engines ==null ) throw new BeanInitializationException("missing process engines map");
	}

}
