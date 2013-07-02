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
package de.scoopgmbh.copper.monitoring.core.debug;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public abstract class Data implements Serializable, DisplayableNode {
	
	private static final long serialVersionUID = 1L;

	final String type;
	public int objectId;

	public Data(String type, int objectId) {
		this.type = type.intern();
		this.objectId = objectId;
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public abstract String getDisplayValue(); 
	
	@Override
	public Collection<? extends DisplayableNode> getChildren() {
		return Collections.emptyList();
	}
	
	@Override
	public NodeTyp getTyp() {
		return null;
	}
	
	private static final class NULLDATA_CLASS extends Data {
		public NULLDATA_CLASS() {
			super("",-1);
		}
		
		private static final long serialVersionUID = 1L;

		public String getDisplayValue() {
			return "<null>";
		};
		
		/* the compiler does not know about this special method. It is a fake method that alters the behaviour of ObjectInputStream.readObject */ 
		@SuppressWarnings("unused")
		public Data readResolve() throws ObjectStreamException {
			return NULL;
		}

	}
	
	public static final Data NULL = new NULLDATA_CLASS();		
}
