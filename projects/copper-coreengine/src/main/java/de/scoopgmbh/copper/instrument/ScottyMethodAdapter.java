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
package de.scoopgmbh.copper.instrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import de.scoopgmbh.copper.StackEntry;

class ScottyMethodAdapter extends MethodAdapter implements Opcodes {
	
	static final Logger logger = Logger.getLogger(ScottyMethodAdapter.class);
	public static final Set<String> waitMethods;
	
	static {
		waitMethods = new HashSet<String>();
		waitMethods.add("waitForAll([Ljava/lang/String;)V");
		waitMethods.add("waitForAll([Lde/scoopgmbh/copper/Callback;)V");
		waitMethods.add("wait(Lde/scoopgmbh/copper/WaitMode;I[Ljava/lang/String;)V");
		waitMethods.add("wait(Lde/scoopgmbh/copper/WaitMode;I[Lde/scoopgmbh/copper/Callback;)V");
		waitMethods.add("resubmit()V");
	}	

	private final String currentClassName;
	private final List<Label> labels = new ArrayList<Label>();
	private final Map<Label,StackInfo> labelInfo = new HashMap<Label,StackInfo>();
	private final Label switchLabelAtEnd = new Label();
	private final Label begin = new Label();
	private final Set<String> interruptableMethods;
	private final ByteCodeStackInfo stackInfo;
	private final String methodName;

	public ScottyMethodAdapter(MethodVisitor mv, String currentClassName, Set<String> interruptableMethods, ByteCodeStackInfo stackInfo, String name) {
		super(mv);
		this.currentClassName = currentClassName;
		this.interruptableMethods = interruptableMethods;
		this.stackInfo = stackInfo;
		this.methodName = name;
	}


	@Override
	public void visitCode() {
		super.visitCode();
		visitJumpInsn(GOTO, switchLabelAtEnd);
		visitLabel(begin);
	}
	
	void pushLocals(StackInfo info) {
		super.visitIntInsn(SIPUSH, info.localsSize());
		super.visitTypeInsn(ANEWARRAY, "java/lang/Object");
		for (int i = 0; i < info.localsSize(); ++i) {
			Type t = info.getLocal(i);
			if (t != null) {
				super.visitInsn(DUP);
				super.visitIntInsn(SIPUSH, i);
				if (t == Type.BOOLEAN_TYPE || t == Type.BYTE_TYPE || t == Type.SHORT_TYPE || t == Type.INT_TYPE || t == Type.CHAR_TYPE) {
					super.visitVarInsn(ILOAD,i);
					super.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
				} else if (t == Type.FLOAT_TYPE) {
					super.visitVarInsn(FLOAD, i);
					super.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
				} else if (t == Type.LONG_TYPE) {
					super.visitVarInsn(LLOAD, i);
					super.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
				} else if (t == Type.DOUBLE_TYPE) {
					super.visitVarInsn(DLOAD, i);
					super.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
				} else {
					super.visitVarInsn(ALOAD, i);
				}
				super.visitInsn(AASTORE);
			}
		}
	}

	void recreateLocals(StackInfo info) {
		if (info.localsSize() == 0)
			return;
		visitVarInsn(ALOAD, 0);
		visitFieldInsn(GETFIELD, currentClassName, "__stack", "Ljava/util/Stack;");
		visitVarInsn(ALOAD, 0);
		visitFieldInsn(GETFIELD, currentClassName, "__stackPosition", "I");
		visitMethodInsn(INVOKEVIRTUAL, "java/util/Stack", "get", "(I)Ljava/lang/Object;");
		visitTypeInsn(CHECKCAST, "de/scoopgmbh/copper/StackEntry");
		visitFieldInsn(GETFIELD, "de/scoopgmbh/copper/StackEntry", "locals", "[Ljava/lang/Object;");
		for (int i = 0; i < info.localsSize(); ++i) {
			Type t = info.getLocal(i);
			if (t != null) {
				super.visitInsn(DUP);
				super.visitIntInsn(SIPUSH, i);
				super.visitInsn(AALOAD);
				if (t == Type.BOOLEAN_TYPE || t == Type.BYTE_TYPE || t == Type.SHORT_TYPE || t == Type.INT_TYPE || t == Type.CHAR_TYPE) {
					super.visitTypeInsn(CHECKCAST, Type.getInternalName(Integer.class));
					super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
					super.visitVarInsn(ISTORE, i);
				} else if (t == Type.FLOAT_TYPE) {
					super.visitTypeInsn(CHECKCAST, Type.getInternalName(Float.class));
					super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
					super.visitVarInsn(FSTORE, i);
				} else if (t == Type.LONG_TYPE) {
					super.visitTypeInsn(CHECKCAST, Type.getInternalName(Long.class));
					super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
					super.visitVarInsn(LSTORE, i);
				} else if (t == Type.DOUBLE_TYPE) {
					super.visitTypeInsn(CHECKCAST, Type.getInternalName(Double.class));
					super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
					super.visitVarInsn(DSTORE, i);
				} else {
					if (!t.getInternalName().equals(Type.getInternalName(Object.class)))
						super.visitTypeInsn(CHECKCAST, t.getInternalName());
					super.visitVarInsn(ASTORE, i);
				}
			}
		}
		visitInsn(POP);
	}

	void pushStack(StackInfo info) {
		super.visitIntInsn(SIPUSH, info.stackSize());
		super.visitTypeInsn(ANEWARRAY, "java/lang/Object");
		for (int i = info.stackSize()-1; i >= 0 ; --i) {
			Type t = info.getStack(i);
			if (t != null) {
				if (t == Type.BOOLEAN_TYPE || t == Type.BYTE_TYPE || t == Type.SHORT_TYPE || t == Type.INT_TYPE || t == Type.CHAR_TYPE) {
					super.visitInsn(DUP_X1);
					super.visitInsn(SWAP);
					super.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
					super.visitIntInsn(SIPUSH, i);
					super.visitInsn(SWAP);
				} else if (t == Type.FLOAT_TYPE) {
					super.visitInsn(DUP_X1);
					super.visitInsn(SWAP);
					super.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
					super.visitIntInsn(SIPUSH, i);
					super.visitInsn(SWAP);
				} else if (t == Type.LONG_TYPE) {
					super.visitInsn(DUP_X2);
					super.visitInsn(DUP_X2);
					super.visitInsn(POP);
					super.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
					super.visitIntInsn(SIPUSH, i);
					super.visitInsn(SWAP);
				} else if (t == Type.DOUBLE_TYPE) {
					super.visitInsn(DUP_X2);
					super.visitInsn(DUP_X2);
					super.visitInsn(POP);
					super.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
					super.visitIntInsn(SIPUSH, i);
					super.visitInsn(SWAP);
				} else {
					super.visitInsn(DUP_X1);
					super.visitInsn(SWAP);
					super.visitIntInsn(SIPUSH, i);
					super.visitInsn(SWAP);
				}
				super.visitInsn(AASTORE);
			}
		}
	}

	private void createStackEntry(int idx, StackInfo info) {
		pushStack(info); //Der Stack muss sofort abger�umt werden!
		visitTypeInsn(NEW, "de/scoopgmbh/copper/StackEntry");
		visitInsn(DUP_X1);
		visitInsn(DUP_X1);
		visitInsn(POP);
		super.visitIntInsn(SIPUSH, idx);
		pushLocals(info);
		visitMethodInsn(INVOKESPECIAL, "de/scoopgmbh/copper/StackEntry", "<init>", "([Ljava/lang/Object;I[Ljava/lang/Object;)V");			
		visitVarInsn(ALOAD, 0);
		visitFieldInsn(GETFIELD, currentClassName, "__stack", "Ljava/util/Stack;");
		visitInsn(SWAP);
		visitMethodInsn(INVOKEVIRTUAL, "java/util/Stack", "push", "(Ljava/lang/Object;)Ljava/lang/Object;");
		visitInsn(POP);
	}

	private void recreateStack(StackInfo info) {
		if (info.stackSize() == 0)
			return;
		visitVarInsn(ALOAD, 0);
		visitFieldInsn(GETFIELD, currentClassName, "__stack", "Ljava/util/Stack;");
		visitVarInsn(ALOAD, 0);
		visitFieldInsn(GETFIELD, currentClassName, "__stackPosition", "I");
		visitMethodInsn(INVOKEVIRTUAL, "java/util/Stack", "get", "(I)Ljava/lang/Object;");
		visitTypeInsn(CHECKCAST, "de/scoopgmbh/copper/StackEntry");
		visitFieldInsn(GETFIELD, "de/scoopgmbh/copper/StackEntry", "stack", "[Ljava/lang/Object;");
		for (int i = 0; i < info.stackSize(); ++i) {
			Type t = info.getStack(i);
			if (t != null) {
				super.visitInsn(DUP);
				super.visitIntInsn(SIPUSH, i);
				super.visitInsn(AALOAD);
				if (t == Type.BOOLEAN_TYPE || t == Type.BYTE_TYPE || t == Type.SHORT_TYPE || t == Type.INT_TYPE || t == Type.CHAR_TYPE) {
					super.visitTypeInsn(CHECKCAST, Type.getInternalName(Integer.class));
					super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
					super.visitInsn(SWAP);
				} else if (t == Type.FLOAT_TYPE) {
					super.visitTypeInsn(CHECKCAST, Type.getInternalName(Float.class));
					super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
					super.visitInsn(SWAP);
				} else if (t == Type.LONG_TYPE) {
					super.visitTypeInsn(CHECKCAST, Type.getInternalName(Long.class));
					super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
					super.visitInsn(DUP2_X1);
					super.visitInsn(POP2);					
				} else if (t == Type.DOUBLE_TYPE) {
					super.visitTypeInsn(CHECKCAST, Type.getInternalName(Double.class));
					super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
					super.visitInsn(DUP2_X1);
					super.visitInsn(POP2);					
				} else {
					if (!t.getInternalName().equals(Type.getInternalName(Object.class)))
						super.visitTypeInsn(CHECKCAST, t.getInternalName());
					super.visitInsn(SWAP);
				}
			}
		}
		super.visitInsn(POP);
	}

	private void popStackEntry() {
		visitVarInsn(ALOAD, 0);
		visitFieldInsn(GETFIELD, currentClassName, "__stack", "Ljava/util/Stack;");
		visitMethodInsn(INVOKEVIRTUAL, "java/util/Stack", "pop", "()Ljava/lang/Object;");
		visitInsn(POP);
		decStackPos();
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		final String signature = name+desc;
		if (waitMethods.contains(signature)) {
			super.visitMethodInsn(opcode, owner, name, desc);

			int idx = labels.size();
			StackInfo currentStackInfo = stackInfo.getCurrentStackInfo();
			Label label = new Label();
			labels.add(label);
			labelInfo.put(label, currentStackInfo);
			createStackEntry(idx, currentStackInfo);
			incStackPos();
			if ("main".equals(name) && "()V".equals(desc)) {
				visitInsn(RETURN);
			}
			else {
				visitTypeInsn(NEW, "de/scoopgmbh/copper/InterruptException");
				visitInsn(DUP);
				visitMethodInsn(INVOKESPECIAL, "de/scoopgmbh/copper/InterruptException", "<init>", "()V");
				visitInsn(ATHROW);
			}
			visitLabel(label);
			popStackEntry();
		}
		else if (interruptableMethods.contains(signature)) {
			Label invokeLabel = new Label();
			Label afterInvokeLabel = new Label();
			Label nopLabel = new Label();
			Label interruptLabel = new Label();
			Label throwableHandler = new Label();
			int idx = labels.size();
			StackInfo info = stackInfo.getPreviousStackInfo();
			labels.add(invokeLabel);
			labelInfo.put(invokeLabel, info);
			createStackEntry(idx, info);
			recreateStack(info);
			incStackPos();
			visitLabel(invokeLabel);
			super.visitMethodInsn(opcode, owner, name, desc);
			visitLabel(afterInvokeLabel);
			visitJumpInsn(GOTO, nopLabel);

			visitLabel(interruptLabel);
			visitInsn(ATHROW);

			visitLabel(throwableHandler);
			popStackEntry();
			visitInsn(ATHROW);

			visitLabel(nopLabel);
			popStackEntry();
			visitInsn(NOP);

			//logger.info("Calling super.visitTryCatchBlock("+invokeLabel+", "+afterInvokeLabel+", "+interruptLabel+", \"de/scoopgmbh/copper/InterruptException\")");
			super.visitTryCatchBlock(invokeLabel, afterInvokeLabel, interruptLabel, "de/scoopgmbh/copper/InterruptException");
			super.visitTryCatchBlock(invokeLabel, afterInvokeLabel, throwableHandler, "java/lang/Throwable");
		}
		else {
			super.visitMethodInsn(opcode, owner, name, desc);
		}
	}
	
	@Override
	public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
		
		super.visitTryCatchBlock(arg0, arg1, arg2, arg3);
	}


	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack+7, maxLocals);
	}

	@Override
	public void visitEnd() {
		super.visitEnd();

		Label switchStmtLabel = new Label();
		visitLabel(switchLabelAtEnd);
		visitVarInsn(ALOAD, 0);
		visitFieldInsn(GETFIELD, currentClassName, "__stack", "Ljava/util/Stack;");
		visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Stack.class), "size", "()I");
		visitVarInsn(ALOAD, 0);
		visitFieldInsn(GETFIELD, currentClassName, "__stackPosition", "I");
		visitJumpInsn(IF_ICMPNE, switchStmtLabel);
		visitJumpInsn(GOTO, begin);
		visitLabel(switchStmtLabel);

		visitVarInsn(ALOAD, 0);
		visitFieldInsn(GETFIELD, currentClassName, "__stack", "Ljava/util/Stack;");
		visitVarInsn(ALOAD, 0);
		visitFieldInsn(GETFIELD, currentClassName, "__stackPosition", "I");
		visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Stack.class), "get", "(I)Ljava/lang/Object;");
		visitTypeInsn(CHECKCAST, Type.getInternalName(StackEntry.class));
		visitFieldInsn(GETFIELD, Type.getInternalName(StackEntry.class), "jumpNo", "I");

		if (!labels.isEmpty()) {
			int labelNo = 0;
			for (Label label : labels) {
				Label nextCheck = new Label();
				visitInsn(DUP);
				visitIntInsn(SIPUSH, labelNo);
				visitJumpInsn(IF_ICMPNE, nextCheck);
				visitInsn(POP);
				recreateLocals(labelInfo.get(label));
				recreateStack(labelInfo.get(label));				
				incStackPos();
				visitJumpInsn(GOTO, label);
				visitLabel(nextCheck);
				++labelNo;
			}
			visitTypeInsn(NEW, "java/lang/RuntimeException");
			visitInsn(DUP);
			visitLdcInsn("No such label");
			visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
			visitInsn(ATHROW);
		}
		else {
			visitInsn(POP);
			visitJumpInsn(GOTO, begin);
		}
	}


	private void incStackPos() {
		visitVarInsn(ALOAD, 0);
		visitInsn(DUP);
		visitFieldInsn(GETFIELD, currentClassName, "__stackPosition", "I");
		visitInsn(ICONST_1);
		visitInsn(IADD);
		visitFieldInsn(PUTFIELD, currentClassName, "__stackPosition", "I");
	}

	private void decStackPos() {
		visitVarInsn(ALOAD, 0);
		visitInsn(DUP);
		visitFieldInsn(GETFIELD, currentClassName, "__stackPosition", "I");
		visitInsn(ICONST_1);
		visitInsn(ISUB);
		visitFieldInsn(PUTFIELD, currentClassName, "__stackPosition", "I");
	}

}
