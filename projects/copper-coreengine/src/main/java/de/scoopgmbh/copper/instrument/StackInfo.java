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
package de.scoopgmbh.copper.instrument;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class StackInfo {
	
	class DummyClass {}
	public static final Type AconstNullType = Type.getType(DummyClass.class);

	enum ComputationalCategory {
		CAT_1, CAT_2;
	}

	public StackInfo() {
		locals = new Vector<Type>();
		stack = new Stack<Type>();
	}
	
	@SuppressWarnings("unchecked")
	public StackInfo(StackInfo orig) {
		locals = (Vector<Type>)orig.locals.clone();
		stack = new Stack<Type>();
		stack.addAll(orig.stack);
		//this.numLocals = orig.numLocals;
	}
	
	//int numLocals;
	Vector<Type> locals;
	Stack<Type> stack;
	
	public void setLocal(int pos, Type t) {
		ComputationalCategory cat = t!=null?getCategory(t):null;
		int upperBound = pos;
		if (cat == ComputationalCategory.CAT_2)
			++upperBound;
		if (locals.size() <= upperBound) {
			locals.setSize(upperBound+1);
		}
		locals.set(pos,t);
		if (cat == ComputationalCategory.CAT_2)
			locals.set(pos+1,null);
	}
	
	public String localsToString() {
		if (locals.size() == 0)
			return "[]";
		StringBuilder sb = new StringBuilder("[");
		for (Type t : locals) {
			if (t == null)
				sb.append("null, ");
			else
				sb.append(t.getDescriptor()).append(", ");
		}
		sb.setLength(sb.length()-2);
		sb.append(']');
		return sb.toString();
	}
	
	public Type getLocal(int pos) {
		return locals.get(pos);
	}

	public Type getStack(int pos) {
		return stack.get(pos);
	}

	public void pushStack(Type t) {
		if (t != Type.VOID_TYPE)
			stack.add(t);
	}


	public Type popStack() {
		Type t = stack.pop();
		if (getCategory(t) != ComputationalCategory.CAT_1)
				throw new BuildStackFrameException("Cannot pop cat 2 values");
		return t;
	}
	
	public Type popStackUnchecked() {
		Type t = stack.pop();
		return t;
	}
	
	public Type pop2Stack() {
		Type t = stack.pop();
		switch (getCategory(t)) {
		case CAT_1:
			if (getCategory(stack.pop()) != ComputationalCategory.CAT_1)
				throw new BuildStackFrameException("Cannot pop2 cat 1 and cat 2 values");
			break;
		case CAT_2: break;
		}
		return t;
	}
	
	public void popStackChecked(Type t) {
		Type ref = stack.pop();
		if (!compatible(t,ref))
			throw new BuildStackFrameException("Unexpected thing on stack. Expected "+t+" but got "+ref);
	}
	
	public void popStackBySignature(String signature) {
		List<Type> types = Arrays.asList(Type.getArgumentTypes(signature));
		Collections.reverse(types);
		for (Type t : types) {
			popStackChecked(t);
		}
		
	}
	
	public void clearStack() {
		stack.clear();
	}
	
	
	public Type replaceStackChecked(Type oldType, Type newType) {
		Type rt = stack.pop();
		if (!compatible(rt,oldType))
			throw new BuildStackFrameException("Expected "+oldType+" but got "+rt);
		if (newType != Type.VOID_TYPE)
			stack.add(newType);
		return rt;
	}
	
	public Type replaceStack(Type newType) {
		Type rt = stack.pop();
		if (newType != Type.VOID_TYPE)
			stack.add(newType);
		return rt;
	}
	
	public Type swapStack() {
		Type t = stack.pop();
		if (getCategory(t) != ComputationalCategory.CAT_1
			|| getCategory(stack.peek()) != ComputationalCategory.CAT_1)
			throw new BuildStackFrameException("Wrong computational type.");
		stack.add(stack.size()-1,t);
		return stack.peek();
	}

	public Type dupStack() {
		Type t = stack.peek();
		if (getCategory(t) == ComputationalCategory.CAT_2)
			throw new BuildStackFrameException("Wrong computational type.");
		return stack.push(t);
	}

	public Type dupX1Stack() {
		int size = stack.size();
		if (getCategory(stack.get(size-1)) != ComputationalCategory.CAT_1
			|| getCategory(stack.get(size-2)) != ComputationalCategory.CAT_1)
			throw new BuildStackFrameException("Wrong computational type.");
		stack.add(size-2, stack.get(size-1));
		return stack.peek();
	}

	public Type dupX2Stack() {
		int size = stack.size();
		Type value2 = stack.get(size-2);
		if (getCategory(stack.peek()) != ComputationalCategory.CAT_1)
			throw new BuildStackFrameException("Wrong computational type.");
		switch (getCategory(value2)) {
		case CAT_1:
			if (getCategory(stack.get(size-3)) != ComputationalCategory.CAT_1)
				throw new BuildStackFrameException("Wrong computational type.");
			stack.add(size-3, stack.peek());
			break;
		case CAT_2:
			stack.add(size-2, stack.peek());
		    break;
		}
		return stack.peek();
	}
	
	public Type dup2Stack() {
		int size = stack.size();
		Type value = stack.peek();
		switch (getCategory(value)) {
		case CAT_1:
			if (getCategory(stack.get(size-2)) != ComputationalCategory.CAT_1)
				throw new BuildStackFrameException("Wrong computational type.");
			Type value2 = stack.get(size-2);
			stack.add(size-2, value);
			stack.add(size-2, value2);
			break;
		case CAT_2:
			stack.push(stack.peek());
		    break;
		}
		return stack.peek();
	}
	
	public Type dup2X1Stack() {
		int size = stack.size();
		Type value1 = stack.peek();
		switch (getCategory(value1)) {
		case CAT_1:
			if (getCategory(stack.get(size-2)) != ComputationalCategory.CAT_1 ||
				getCategory(stack.get(size-3)) != ComputationalCategory.CAT_1)
				throw new BuildStackFrameException("Wrong computational type.");
			Type value2 = stack.get(size-2);
			stack.add(size-3, value1);
			stack.add(size-3, value2);
			break;
		case CAT_2:
			if (getCategory(stack.get(size-2)) != ComputationalCategory.CAT_1)
					throw new BuildStackFrameException("Wrong computational type.");
			stack.add(size-2, value1);
		    break;
		}
		return stack.peek();
	}
	
	public Type dup2X2Stack() {
		int size = stack.size();
		Type value1 = stack.peek();
		Type value2 = stack.get(size-2);
		ComputationalCategory catVal1 = getCategory(value1);
		ComputationalCategory catVal2 = getCategory(value2);
		if (catVal1 == ComputationalCategory.CAT_1) {
			if (catVal2 == ComputationalCategory.CAT_1) {
				Type value3 = stack.get(size-3);
				ComputationalCategory catVal3 = getCategory(value3);
				if (catVal3 == ComputationalCategory.CAT_1) {
					stack.add(size-4,value1);
					stack.add(size-4,value2);
				} else {
					stack.add(size-3,value1);
					stack.add(size-3,value2);						
				}
			} else {
				throw new BuildStackFrameException("Corrupt Stack. dup2x2 encountered Category 1 on top followed by Category 2");
			}
		} else {
			if (catVal2 == ComputationalCategory.CAT_1) {
				if (getCategory(stack.get(size-3)) != ComputationalCategory.CAT_1)
					throw new BuildStackFrameException("Wrong computational type.");
				stack.add(size-3,value1);					
			} else {
				stack.add(size-2,value1);										
			}
		}
		return stack.peek();
	}
	
	public void appendLocals(int num, Object[] newLocal) {
		int position = locals.size();
		for (int i = 0; i < num; ++i) {
			Type t = deferLocalDesc(newLocal[i]);
			if (t != null) {
				setLocal(position++,t);
				if (getCategory(t) == ComputationalCategory.CAT_2) {
					setLocal(position++,null);
				}
			} else
				setLocal(position++,null);
		}
		
	}


	public void removeLocals(int arg1) {
		while (arg1 > 0) {
			if (locals.lastElement() == null) {
				locals.remove(locals.size()-1);
				continue;
			}
			locals.remove(locals.size()-1);
			--arg1;
		}
	}

	public void clearFrame() {
		locals = new Vector<Type>();
		stack.clear();
	}

	public void appendStack(int num, Object[] newStack) {
		for (int i = 0; i < num; ++i) {
			Type t = deferLocalDesc(newStack[i]);
			if (t != null)
				stack.push(t);
		}			
	}

	static Type deferLocalDesc(Object object) {
		if (object instanceof String)
			return Type.getObjectType((String)object);
		//TODO: analyze opcode at pos label
		if (object instanceof Label)
			return Type.getType(Object.class); 
		int intObject = (Integer)object;
		if (intObject == Opcodes.TOP)
			return null;
		if (intObject == Opcodes.INTEGER)
			return Type.INT_TYPE;
		if (intObject == Opcodes.FLOAT)
			return Type.FLOAT_TYPE;
		if (intObject == Opcodes.LONG)
			return Type.LONG_TYPE;
		if (intObject == Opcodes.DOUBLE)
			return Type.getType(double.class);
		if (intObject == Opcodes.LONG)
			return Type.getType(long.class);
		if (intObject == Opcodes.NULL)
			return Type.getType(Object.class);
		 //TODO: defer from containing class
		if (intObject == Opcodes.UNINITIALIZED_THIS)	
			return Type.getType(Object.class);
		throw new BuildStackFrameException("Couldnt defer desc for "+object);
	}

	static ComputationalCategory getCategory(Type t) {
		if (t.getSort() == Type.LONG
			|| t.getSort() == Type.DOUBLE)
			return ComputationalCategory.CAT_2;
		return ComputationalCategory.CAT_1;
	}

	static boolean compatible(Type t, Type ref) {
		switch (t.getSort()) {
		case Type.BYTE:
		case Type.CHAR:
		case Type.BOOLEAN:
		case Type.SHORT:
		case Type.INT:
			return ref.getSort() == Type.INT || ref.getSort() == Type.SHORT || ref.getSort() == Type.BYTE || ref.getSort() == Type.BOOLEAN || ref.getSort() == Type.CHAR;
		}
		//Arrays sind Objects
		if (t.getSort() == 9) {
			if (ref.getSort() == 10)
				return true;
		}
		if (t.getSort() == 10) {
			if (ref.getSort() == 9)
				return true;
		}
		return t.getSort() == ref.getSort();
	}
	
	public int localsSize() {
		return locals.size();
	}

	public int stackSize() {
		return stack.size();
	}
}