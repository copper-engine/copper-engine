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
package de.scoopgmbh.copper.internal;

import java.lang.reflect.Method;

import de.scoopgmbh.copper.ProcessingState;
import de.scoopgmbh.copper.Workflow;

public class ProcessingStateAccessor {
	private static final Method m;
	static {
		try {
			m = Workflow.class.getDeclaredMethod("setProcessingState", ProcessingState.class);
			m.setAccessible(true);
		}
		catch(Exception e) {
			throw new Error(e);
		}
	}
	public static void setProcessingState(Workflow<?> w, ProcessingState s) {
		try {
			m.invoke(w, s);
		} 
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
