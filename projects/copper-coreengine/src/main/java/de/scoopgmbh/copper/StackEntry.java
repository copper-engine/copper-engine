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

import java.io.IOException;
import java.io.Serializable;

/**
 * For internal use only.
 * 
 * @author austermann
 *
 */
public class StackEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	public transient int jumpNo;
	public transient Object[] locals;
	public transient Object[] stack;
	
	public StackEntry(Object[] stack, int jumpNo, Object[] locals) {
		this.jumpNo = jumpNo;
		this.locals = locals;
		this.stack = stack;
	}

	public StackEntry(int jumpNo) {
		this.jumpNo = jumpNo;
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		jumpNo = stream.readInt();
		int numLocals = stream.readInt();
		int numStack = stream.readInt();
		if (numLocals > 0)
			locals = new Object[numLocals];
		if (numStack > 0)
			stack = new Object[numStack];
		for (int i = 0; i < numLocals; ++i)
			locals[i] = stream.readObject();
		for (int i = 0; i < numStack; ++i)
			stack[i] = stream.readObject();
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeInt(jumpNo);
		stream.writeInt(locals == null ? 0 : locals.length);
		stream.writeInt(stack == null ? 0 : stack.length);
		if (locals != null) {
			for (int i = 0; i < locals.length; ++i)
				stream.writeObject(locals[i]);
		}
		if (stack != null) {
			for (int i = 0; i < stack.length; ++i)
				stream.writeObject(stack[i]);
		}
	}

	@Override
	public String toString() {
//		return "StackEntry [jumpNo=" + jumpNo + ", locals="
//		+ Arrays.toString(locals) + ", stack=" + Arrays.toString(stack)
//		+ "]";
		return "StackEntry@"+hashCode()+" [jumpNo=" + jumpNo + "]";
	}
	
	
	
}
