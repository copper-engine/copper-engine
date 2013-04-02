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
package de.scoopgmbh.copper.test.versioning.compatibility;

import java.io.IOException;
import java.io.ObjectStreamClass;

import de.scoopgmbh.copper.wfrepo.FileBasedWorkflowRepository;

public class TestWorkflowRepository extends FileBasedWorkflowRepository {

	String triggerClassname = null; 
	String overrideClassname = null;
	
	@Override
	public Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		if (triggerClassname == null || overrideClassname == null || !desc.getName().equals(triggerClassname)) {
			return super.resolveClass(desc);
		}
		else {
			return Class.forName(overrideClassname,false,super.getClassLoader());
		}
	}

}
