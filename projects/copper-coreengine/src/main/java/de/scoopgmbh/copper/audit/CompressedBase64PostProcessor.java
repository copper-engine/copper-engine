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
package de.scoopgmbh.copper.audit;

import de.scoopgmbh.copper.persistent.StandardJavaSerializer;

public class CompressedBase64PostProcessor implements MessagePostProcessor {

	private StandardJavaSerializer serializer = new StandardJavaSerializer();
	
	@Override
	public String serialize(String msg) {
		try {
			return serializer.serializeObject(msg);
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String deserialize(String msg) {
		try {
			return (String) serializer.deserializeObject(msg);
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
