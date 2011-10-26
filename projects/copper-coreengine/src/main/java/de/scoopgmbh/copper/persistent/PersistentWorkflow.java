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
package de.scoopgmbh.copper.persistent;

import java.io.Serializable;
import java.util.List;

import de.scoopgmbh.copper.Workflow;


/**
 * Abstract base class for persistent workflows.
 * 
 * It is safe to run a PersistentWorkflow in a transient engine. So if your want to keep it open to decide later wether your
 * workflow needs persistence or not, it is OK to inherit from PersistentWorkflow.
 * 
 * @author austermann
 *
 * @param <E>
 */
public abstract class PersistentWorkflow<E extends Serializable> extends Workflow<E> implements Serializable {

	private static final long serialVersionUID = 3232137844188440549L;
	
	transient RegisterCall registerCall;
	transient List<String> cidList;
	transient String rowid;
	transient String oldProcessorPoolId;
	transient int oldPrio;
	
}
