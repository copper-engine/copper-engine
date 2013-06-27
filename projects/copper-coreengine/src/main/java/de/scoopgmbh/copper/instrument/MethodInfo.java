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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MethodInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	List<LabelInfo> labelInfos = new ArrayList<LabelInfo>();
	final String definingClass;
	final String methodName;
	final int access;
	final String descriptor;
	
	public MethodInfo(String definingClass, String methodName, int access, String descriptor) {
		this.definingClass = definingClass;
		this.methodName = methodName;
		this.access = access;
		this.descriptor = descriptor;
	}
		
	public void addLabelInfo(LabelInfo info) {
		this.labelInfos.add(info);
	}
	
	public List<LabelInfo> getLabelInfos() {
		return labelInfos;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(1000);
		sb.append(getDeclaration());
		sb.append(" {\n");
		for (LabelInfo lb : getLabelInfos()) {
			sb.append("\t\t").append(lb).append('\n');
		}
		sb.append("\t}\n");
		return sb.toString();
	}

	public String getDeclaration() {
		StringBuilder sb = new StringBuilder();
		if ((access & Opcodes.ACC_PRIVATE) != 0)
			sb.append("private ");
		if ((access & Opcodes.ACC_PROTECTED) != 0)
			sb.append("protected ");
		if ((access & Opcodes.ACC_PUBLIC) != 0)
			sb.append("public ");
		if ((access & Opcodes.ACC_STATIC) != 0)
			sb.append("static ");
		if ((access & Opcodes.ACC_FINAL) != 0)
			sb.append("final ");
		sb.append(Type.getReturnType(descriptor).getClassName()).append(' ').append(methodName).append('(');
		
		boolean first = true;
		for (Type t : Type.getArgumentTypes(descriptor)) {
			if (!first)
				sb.append(", ");
			first = false;
			sb.append(t.getClassName());
		}
		sb.append(')');
		return sb.toString();
	}

	public static class SerializableType implements Serializable {

		private static final long serialVersionUID = 1L;

		String descriptor;
		
		public SerializableType(Type t) {
			descriptor = t.getDescriptor();
		}
		
		public SerializableType(String descriptor) {
			this.descriptor = descriptor;
		}
		
		public Type toType() {
			return Type.getType(descriptor);
		}
		
		public String toString() {
			return toType().getClassName();
		}
		
		public String getDescriptor() {
			return descriptor;
		}
		
		public String getDeclaredType() {
			return Type.getType(descriptor).getClassName();
		}

	}
	
	public static class LocalVariable extends SerializableType {

		private static final long serialVersionUID = 1L;

		String name;
		
		public LocalVariable(String name, Type type) {
			super(type);
			this.name = name;
		}
		
		public LocalVariable(String name, String descriptor) {
			super(descriptor);
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return super.toString()+" "+getName();
		}
		
		
		
	}
	
	public static class LabelInfo implements Serializable  {

		private static final long serialVersionUID = 1L;

		final String calledMethodName;
		final String calledMethodDescriptor;
		final int                labelNo;
		final Integer            lineNo;
		final LocalVariable[]    locals;
		final SerializableType[] stack;
		
		public LabelInfo(Integer labelNo, int lineNo, List<String> localNames, List<Type> localDescriptors, List<Type> localTypes, List<Type> stack, String calledMethodName, String calledMethodDescriptor) {
			
			assert localNames.size() == localTypes.size();
			this.labelNo = labelNo;
			this.lineNo  = lineNo > -1?lineNo:null;
			
			this.locals = new LocalVariable[localNames.size()];
			for (int i = 0; i < localNames.size(); ++i) {
				Type actualType = localTypes.get(i);
				if (actualType == null)
					actualType = localDescriptors.get(i);
				if (actualType != null)
					locals[i] = new LocalVariable(localNames.get(i), actualType);
			}
			this.stack = new SerializableType[stack.size()];
			for (int i = 0; i < stack.size(); ++i) {
				this.stack[i] = new SerializableType(stack.get(i));
			}
			this.calledMethodDescriptor = calledMethodDescriptor;
			this.calledMethodName = calledMethodName;

		}
		
		public String getCalledMethodName() {
			return calledMethodName;
		}

		public String getCalledMethodDescriptor() {
			return calledMethodDescriptor;
		}

		public int getLabelNo() {
			return labelNo;
		}

		public Integer getLineNo() {
			return lineNo;
		}

		public LocalVariable[] getLocals() {
			return locals;
		}

		public SerializableType[] getStack() {
			return stack;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(200);
			sb.append("labelNo: ").append(getLabelNo()).append(", line: ").append(getLineNo()).append(", locals: {");
			boolean first = true;
			for (LocalVariable var : locals) {
				if (!first)
					sb.append(", ");
				first = false;
				sb.append(var);
			}
			sb.append("} stack: {");
			first = true;
			for (SerializableType var : stack) {
				if (!first)
					sb.append(", ");
				first = false;
				sb.append(var);
			}
			sb.append("}");
			return sb.toString();
		}

	}

	public String getDefiningClass() {
		return definingClass;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getDescriptor() {
		return descriptor;
	}



}
