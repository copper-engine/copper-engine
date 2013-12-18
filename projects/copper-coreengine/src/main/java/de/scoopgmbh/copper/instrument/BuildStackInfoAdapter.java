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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import de.scoopgmbh.copper.instrument.StackInfo.ComputationalCategory;

public class BuildStackInfoAdapter extends MethodVisitor implements Opcodes, ByteCodeStackInfo {
	
	static final Logger logger = LoggerFactory.getLogger(BuildStackInfoAdapter.class);
	
	static final Type retAddressType = Type.getObjectType("ReturnAddress");

	StackInfo lastDeclaredFrame;
	StackInfo currentFrame;
	StackInfo previousFrame;
	Map<Label, StackInfo> forwardFrames  = new HashMap<Label, StackInfo>();
	Map<Label, int[]>     lineNumbers    = new HashMap<Label, int[]>();
	List<LocalVariable>   localVariables = new ArrayList<LocalVariable>();
	MethodVisitor delegate;

	public BuildStackInfoAdapter(String classType, boolean isStatic, String methodName, String arguments, String extendedArguments) {
		super(ASM4);
		int i = 0;
		Type[] argumentTypes = Type.getArgumentTypes(arguments);
		currentFrame = new StackInfo();
		if (!isStatic)
			currentFrame.setLocal(i++, Type.getType(classType));
		for (Type t : argumentTypes) {
			currentFrame.setLocal(i++, t);
			if (StackInfo.getCategory(t) == ComputationalCategory.CAT_2)
				i++;
		}
		lastDeclaredFrame = new StackInfo(currentFrame);
		this.delegate = new NullMethodVisitor(); 
	}
	
	public void setMethodVisitor(MethodVisitor mVisitor) {
		this.delegate = mVisitor; 
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		return delegate.visitAnnotation(arg0, arg1);
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return delegate.visitAnnotationDefault();
	}

	@Override
	public void visitAttribute(Attribute arg0) {
		delegate.visitAttribute(arg0);
	}

	@Override
	public void visitCode() {
		delegate.visitCode();
	}

	@Override
	public void visitEnd() {
		delegate.visitEnd();
	}

	
	@Override
	public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
		savePreviousFrame();
		Type t = deferTypFromCanonicalName(arg3);
		switch (arg0) {
		case GETSTATIC:
			currentFrame.pushStack(t); break;
		case PUTSTATIC:
			currentFrame.popStackUnchecked();
			break;
		case GETFIELD:
			currentFrame.replaceStack(t); break;
		case PUTFIELD:
			currentFrame.popStackUnchecked(); 
			currentFrame.popStackUnchecked();
			break;
		default: logger.debug("Unhandled: ");			
		}
		if (logger.isDebugEnabled()) logger.debug("fieldInsn "+getOpCode(arg0)+" '"+arg1+"' '"+arg2+"' '"+arg3+"'");
		delegate.visitFieldInsn(arg0, arg1, arg2, arg3);
	}

	
	@Override
	public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3,
			Object[] arg4) {
		savePreviousFrame();
		if (logger.isDebugEnabled()) logger.debug("stackBefore: "+currentFrame.stack);
		if (logger.isDebugEnabled()) logger.debug("localBefore: "+currentFrame.localsToString());
		currentFrame = new StackInfo(lastDeclaredFrame);
		switch (arg0) {
		case F_SAME: // representing frame with exactly the same locals as the previous frame and with the empty stack.
			currentFrame.stack.clear(); break;
		case F_SAME1: //representing frame with exactly the same locals as the previous frame and with single value on the stack (nStack is 1 and stack[0] contains value for the type of the stack item).
			Type t = StackInfo.deferLocalDesc(arg4[0]);
			currentFrame.stack.clear(); currentFrame.stack.push(t); break;
		case F_APPEND: // representing frame with current locals are the same as the locals in the previous frame, except that additional locals are defined (nLocal is 1, 2 or 3 and local elements contains values representing added types).
			currentFrame.appendLocals(arg1, arg2); break;
		case F_CHOP: //�Opcodes.F_CHOP representing frame with current locals are the same as the locals in the previous frame, except that the last 1-3 locals are absent and with the empty stack (nLocals is 1, 2 or 3).
			currentFrame.removeLocals(arg1); currentFrame.stack.clear(); break;
		case Opcodes.F_FULL: //representing complete frame data.
		case Opcodes.F_NEW: 
			currentFrame.clearFrame(); currentFrame.appendLocals(arg1, arg2); currentFrame.appendStack(arg3, arg4); break;
		default:
			throw new BuildStackFrameException("Unkwnon frame type "+arg0);

		}
		lastDeclaredFrame = new StackInfo(currentFrame);
		if (logger.isDebugEnabled()) logger.debug("stack: "+currentFrame.stack);
		if (logger.isDebugEnabled()) logger.debug("local: "+currentFrame.localsToString());
		if (logger.isDebugEnabled()) logger.debug("frame "+getFrameType(arg0)+" '"+arg1+"' '"+Arrays.asList(arg2)+"' '"+arg3+"' '"+Arrays.asList(arg4)+"'");
		delegate.visitFrame(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public void visitIincInsn(int arg0, int arg1) {
		savePreviousFrame();
		delegate.visitIincInsn(arg0, arg1);
	}

	@Override
	public void visitInsn(int arg0) {
		savePreviousFrame();
		switch (arg0) {
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
		case ICONST_M1:
			currentFrame.pushStack(Type.INT_TYPE); break;
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
			currentFrame.pushStack(Type.FLOAT_TYPE); break;
		case DCONST_0:
		case DCONST_1:
			currentFrame.pushStack(Type.DOUBLE_TYPE); break;
		case LCONST_0:
		case LCONST_1:
			currentFrame.pushStack(Type.LONG_TYPE); break;
		case ACONST_NULL:
			currentFrame.pushStack(StackInfo.AconstNullType); break;
//			currentFrame.pushStack(Type.getType(Object.class)); break;
		case DUP:
			currentFrame.dupStack(); break;
		case DUP_X1:
			currentFrame.dupX1Stack(); break;
		case DUP_X2:
			currentFrame.dupX2Stack(); break;
		case DUP2:
			currentFrame.dup2Stack(); break;
		case DUP2_X1:
			currentFrame.dup2X1Stack(); break;
		case DUP2_X2:
			currentFrame.dup2X2Stack(); break;
		case POP:
			currentFrame.popStack(); break;
		case POP2:
			currentFrame.pop2Stack(); break;
		case SWAP:
			currentFrame.swapStack(); break;
		case IADD:
		case IAND:
		case ISUB:
		case IMUL:
		case IDIV:
		case IOR:
		case ISHL:
		case ISHR:
		case IUSHR:
		case IREM:
		case IXOR:
			currentFrame.popStackChecked(Type.INT_TYPE); 
		case INEG:
			currentFrame.replaceStackChecked(Type.INT_TYPE,Type.INT_TYPE); break;
		case LADD:
		case LAND:
		case LOR:
		case LSUB:
		case LMUL:
		case LDIV:
		case LREM:
		case LXOR:
			currentFrame.popStackChecked(Type.LONG_TYPE); 
		case LNEG:
			currentFrame.replaceStackChecked(Type.LONG_TYPE,Type.LONG_TYPE); break;
		case LSHL:
		case LSHR:
		case LUSHR:
			currentFrame.popStackChecked(Type.INT_TYPE); 
			currentFrame.replaceStackChecked(Type.LONG_TYPE,Type.LONG_TYPE); break;
		case DADD:
		case DSUB:
		case DMUL:
		case DDIV:
		case DREM:
			currentFrame.popStackChecked(Type.DOUBLE_TYPE); 
		case DNEG:
			currentFrame.replaceStackChecked(Type.DOUBLE_TYPE,Type.DOUBLE_TYPE); break;
		case FADD:
		case FSUB:
		case FMUL:
		case FDIV:
		case FREM:
			currentFrame.popStackChecked(Type.FLOAT_TYPE); 
		case FNEG:
			currentFrame.replaceStackChecked(Type.FLOAT_TYPE, Type.FLOAT_TYPE); break;
		case FCMPG:
		case FCMPL:
			currentFrame.popStackChecked(Type.FLOAT_TYPE); currentFrame.replaceStackChecked(Type.FLOAT_TYPE, Type.INT_TYPE); break;
		case DCMPG:
		case DCMPL:
			currentFrame.popStackChecked(Type.DOUBLE_TYPE); currentFrame.replaceStackChecked(Type.DOUBLE_TYPE, Type.INT_TYPE); break;
		case F2D:
			currentFrame.replaceStackChecked(Type.FLOAT_TYPE, Type.DOUBLE_TYPE); break;
		case F2I:
			currentFrame.replaceStackChecked(Type.FLOAT_TYPE, Type.INT_TYPE); break;
		case F2L:
			currentFrame.replaceStackChecked(Type.FLOAT_TYPE, Type.LONG_TYPE); break;
		case I2B:
			currentFrame.replaceStackChecked(Type.INT_TYPE, Type.BYTE_TYPE); break;
		case I2C:
			currentFrame.replaceStackChecked(Type.INT_TYPE, Type.CHAR_TYPE); break;
		case I2D:
			currentFrame.replaceStackChecked(Type.INT_TYPE, Type.DOUBLE_TYPE); break;
		case I2F:
			currentFrame.replaceStackChecked(Type.INT_TYPE, Type.FLOAT_TYPE); break;
		case I2L:
			currentFrame.replaceStackChecked(Type.INT_TYPE, Type.LONG_TYPE); break;
		case I2S:
			currentFrame.replaceStackChecked(Type.INT_TYPE, Type.SHORT_TYPE); break;
		case L2D:
			currentFrame.replaceStackChecked(Type.LONG_TYPE, Type.DOUBLE_TYPE); break;
		case L2F:
			currentFrame.replaceStackChecked(Type.LONG_TYPE, Type.FLOAT_TYPE); break;
		case L2I:
			currentFrame.replaceStackChecked(Type.LONG_TYPE, Type.INT_TYPE); break;
		case D2F:
			currentFrame.replaceStackChecked(Type.DOUBLE_TYPE, Type.FLOAT_TYPE); break;
		case D2I:
			currentFrame.replaceStackChecked(Type.DOUBLE_TYPE, Type.INT_TYPE); break;
		case D2L:
			currentFrame.replaceStackChecked(Type.DOUBLE_TYPE, Type.LONG_TYPE); break;
		case LCMP:
			currentFrame.popStackChecked(Type.LONG_TYPE); currentFrame.replaceStackChecked(Type.LONG_TYPE, Type.INT_TYPE); break;
		case ARRAYLENGTH:
			currentFrame.popStack();
			currentFrame.pushStack(Type.INT_TYPE);
			break;
		case RETURN:
		case IRETURN:
		case LRETURN:
		case DRETURN:
		case FRETURN:
		case ARETURN:
			currentFrame.clearFrame();
			break;
		case ATHROW:
			Type t = currentFrame.popStack();
			currentFrame.clearStack();
			currentFrame.pushStack(t);
			break;
		case AALOAD:
			currentFrame.popStackChecked(Type.INT_TYPE);
			Type arrayType = currentFrame.popStack();
			currentFrame.pushStack(arrayType.getElementType());
			break;
		case BALOAD:
			arrayLoad(Type.BYTE_TYPE);
			break;
		case CALOAD:
			arrayLoad(Type.CHAR_TYPE);
			break;
		case DALOAD:
			arrayLoad(Type.DOUBLE_TYPE);
			break;
		case FALOAD:
			arrayLoad(Type.FLOAT_TYPE);
			break;
		case IALOAD:
			arrayLoad(Type.INT_TYPE);
			break;
		case LALOAD:
			arrayLoad(Type.LONG_TYPE);
			break;
		case SALOAD:
			arrayLoad(Type.SHORT_TYPE);
			break;
		case BASTORE:
			arrayStore(Type.BYTE_TYPE);
			break;
		case CASTORE:
			arrayStore(Type.CHAR_TYPE);
			break;		
		case DASTORE:
			arrayStore(Type.DOUBLE_TYPE);
			break;		
		case FASTORE:
			arrayStore(Type.FLOAT_TYPE);
			break;		
		case IASTORE:
			arrayStore(Type.INT_TYPE);
			break;
		case LASTORE:
			arrayStore(Type.LONG_TYPE);
			break;
		case SASTORE:
			arrayStore(Type.SHORT_TYPE);
			break;
		case AASTORE:
			currentFrame.popStack();
			currentFrame.popStackChecked(Type.INT_TYPE);
			currentFrame.popStack();
			break;
		case MONITORENTER:
		case MONITOREXIT:
			currentFrame.popStack();
			break;
		case NOP: break;
		default: logger.debug("Unhandled: ");
		}
		if (logger.isDebugEnabled()) logger.debug("insn "+getOpCode(arg0));
		delegate.visitInsn(arg0);		
	}
	
	@Override
	public void visitIntInsn(int arg0, int arg1) {
		savePreviousFrame();
		switch (arg0) {
		case BIPUSH:
			currentFrame.pushStack(Type.BYTE_TYPE); break;
		case SIPUSH:
			currentFrame.pushStack(Type.SHORT_TYPE); break;
		case NEWARRAY:
			currentFrame.replaceStackChecked(Type.INT_TYPE, getArrayType(arg1)); break;
		default:
			logger.debug("Unhandled: ");
		}
		if (logger.isDebugEnabled()) logger.debug("intInsn "+getOpCode(arg0)+" "+arg1);
		delegate.visitIntInsn(arg0, arg1);
	}

	@Override
	public void visitJumpInsn(int arg0, Label arg1) {
		savePreviousFrame();
		switch (arg0) {
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPNE:
			currentFrame.popStack();
		case Opcodes.IFEQ:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
		case Opcodes.IFLE:
		case Opcodes.IFLT:
		case Opcodes.IFNE:
		case Opcodes.IFNONNULL:
		case Opcodes.IFNULL:
			currentFrame.popStack();
		case Opcodes.GOTO:
			forwardFrames.put(arg1, new StackInfo(currentFrame)); break;
		case Opcodes.JSR:
			currentFrame.pushStack(retAddressType);
			forwardFrames.put(arg1, new StackInfo(currentFrame)); break;
		default:
			logger.debug("Unhandled: ");
		}
		if (logger.isDebugEnabled()) logger.debug("jumpInsn "+getOpCode(arg0)+" "+arg1);
		delegate.visitJumpInsn(arg0, arg1);
	}

	@Override
	public void visitLabel(Label arg0) {
		savePreviousFrame();
		if (logger.isDebugEnabled()) logger.debug("label "+arg0);
		StackInfo f = forwardFrames.get(arg0);
		if (f != null)
			currentFrame = new StackInfo(f);
		delegate.visitLabel(arg0);
	}

	@Override
	public void visitLdcInsn(Object arg0) {
		savePreviousFrame();
		if (arg0 instanceof Type)
			currentFrame.pushStack((Type)arg0);
		else if (arg0 instanceof String)
			currentFrame.pushStack(Type.getType(String.class));
		else if (arg0 instanceof Float)
			currentFrame.pushStack(Type.FLOAT_TYPE);
		else if (arg0 instanceof Double)
			currentFrame.pushStack(Type.DOUBLE_TYPE);
		else if (arg0 instanceof Integer)
			currentFrame.pushStack(Type.INT_TYPE);
		else if (arg0 instanceof Long)
			currentFrame.pushStack(Type.LONG_TYPE);
		else
			logger.debug("Unhandled: ");
		if (logger.isDebugEnabled()) logger.debug("ldcInsn "+arg0);
		delegate.visitLdcInsn(arg0);
	}

	@Override
	public void visitLineNumber(int arg0, Label arg1) {
		getLineNumber(arg1)[0] = arg0;
		currentFrame.setLineNo(arg0);
		delegate.visitLineNumber(arg0, arg1);
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
		localVariables.add(new LocalVariable(name,desc,start,end,index));
		delegate.visitLocalVariable(name, desc, signature, start, end, index);
	}

	@Override
	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		savePreviousFrame();
		if (logger.isDebugEnabled()) logger.debug("lookupSwitchInsn "+arg0+" "+Arrays.toString(arg1)+" "+Arrays.toString(arg2));
		delegate.visitLookupSwitchInsn(arg0, arg1, arg2);
	}

	@Override
	public void visitMaxs(int arg0, int arg1) {
		delegate.visitMaxs(arg0, arg1);
	}

	@Override
	public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
		savePreviousFrame();
		Type deferredReturnType = deferReturnType(arg3);
		switch (arg0) {
		case INVOKESTATIC:
			currentFrame.popStackBySignature(arg3);
			if (deferredReturnType != Type.VOID_TYPE)
				currentFrame.pushStack(deferredReturnType);
			break;
		case INVOKESPECIAL:
		case INVOKEINTERFACE:
		case INVOKEVIRTUAL:
			currentFrame.popStackBySignature(arg3);
			currentFrame.popStack();
			if (deferredReturnType != Type.VOID_TYPE)
				currentFrame.pushStack(deferredReturnType);
			break;
		default:
			logger.debug("Unhandled: ");
		}
		if (logger.isDebugEnabled()) logger.debug("methodInsn "+getOpCode(arg0)+" "+arg1+" "+arg2+" "+arg3);
		delegate.visitMethodInsn(arg0, arg1, arg2, arg3);
	}

	@Override
	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		savePreviousFrame();
		for (int i = 0; i < arg1; ++i)
			currentFrame.popStackChecked(Type.INT_TYPE);
		currentFrame.pushStack(Type.getObjectType(arg0));
		if (logger.isDebugEnabled()) logger.debug("visitMultiANewArrayInsn "+arg0+" "+arg1);
		delegate.visitMultiANewArrayInsn(arg0, arg1);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1,
			boolean arg2) {
		return delegate.visitParameterAnnotation(arg0, arg1, arg2);
	}

	@Override
	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label... arg3) {
		savePreviousFrame();
		if (logger.isDebugEnabled()) logger.debug("tableSwitchInsn "+arg0+" "+arg1+" "+arg2+" "+Arrays.asList(arg3));
		delegate.visitTableSwitchInsn(arg0, arg1, arg2, arg3);
	}

	@Override
	public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2,
			String arg3) {
		if (logger.isDebugEnabled()) logger.debug("tryCatchBlock "+arg0+" "+arg1+" "+arg2+" "+arg3);
		delegate.visitTryCatchBlock(arg0, arg1, arg2, arg3);
	}

	@Override
	public void visitTypeInsn(int arg0, String arg1) {
		savePreviousFrame();
		Type objectType = Type.getObjectType(arg1);
		switch (arg0) {
		case NEW:
			currentFrame.pushStack(objectType); break;
		case CHECKCAST:
			currentFrame.replaceStack(objectType); break;
		case ANEWARRAY:
			currentFrame.replaceStack(Type.getObjectType("["+objectType.getDescriptor())); break;
		case INSTANCEOF:
			currentFrame.replaceStack(Type.INT_TYPE); break;
		default:
			logger.debug("Unhandled:");
		}
		if (logger.isDebugEnabled()) logger.debug("typeInsn: "+getOpCode(arg0)+" "+arg1);
		delegate.visitTypeInsn(arg0, arg1);
	}

	@Override
	public void visitVarInsn(int arg0, int arg1) {
		savePreviousFrame();
		switch (arg0) {
		case ALOAD:
			currentFrame.pushStack(currentFrame.getLocal(arg1)); break;
		case ASTORE:
			currentFrame.setLocal(arg1, currentFrame.popStack()); break;
		case FLOAD:
			if (!StackInfo.compatible(currentFrame.getLocal(arg1), Type.FLOAT_TYPE))
				throw new BuildStackFrameException("FLOAD expects a float, but got "+currentFrame.getLocal(arg1));
			currentFrame.pushStack(currentFrame.getLocal(arg1)); break;			
		case FSTORE:
			currentFrame.setLocal(arg1, currentFrame.popStack()); break;
		case ILOAD:
			if (!StackInfo.compatible(currentFrame.getLocal(arg1), Type.INT_TYPE))
				throw new BuildStackFrameException("ILOAD expects an int, but got "+currentFrame.getLocal(arg1));
			currentFrame.pushStack(Type.INT_TYPE); break;
		case LLOAD:
			if (!StackInfo.compatible(currentFrame.getLocal(arg1), Type.LONG_TYPE))
				throw new BuildStackFrameException("LLOAD expects a long, but got "+currentFrame.getLocal(arg1));
			currentFrame.pushStack(Type.LONG_TYPE); break;
		case DLOAD:
			if (!StackInfo.compatible(currentFrame.getLocal(arg1), Type.DOUBLE_TYPE))
				throw new BuildStackFrameException("DLOAD expects a double, but got "+currentFrame.getLocal(arg1));
			currentFrame.pushStack(Type.DOUBLE_TYPE); break;
		case ISTORE:
			if (!StackInfo.compatible(currentFrame.popStack(), Type.INT_TYPE))
				throw new BuildStackFrameException("ISTORE expects an int, but got "+currentFrame.getLocal(arg1));
			currentFrame.setLocal(arg1, Type.INT_TYPE); break;
		case DSTORE:
			if (!StackInfo.compatible(currentFrame.pop2Stack(), Type.DOUBLE_TYPE))
				throw new BuildStackFrameException("DSTORE expects a double, but got "+currentFrame.getLocal(arg1));
			currentFrame.setLocal(arg1, Type.DOUBLE_TYPE); break;
		case LSTORE:
			if (!StackInfo.compatible(currentFrame.pop2Stack(), Type.LONG_TYPE))
				throw new BuildStackFrameException("LSTORE expects a long, but got "+currentFrame.getLocal(arg1));
			currentFrame.setLocal(arg1, Type.LONG_TYPE); break;
		default:
			logger.debug("Unhandled:");
		}
		if (logger.isDebugEnabled()) logger.debug("varInsn: "+getOpCode(arg0)+" "+arg1);
		delegate.visitVarInsn(arg0, arg1);
	}


	String getOpCode(int opCode) {
		for (Field f : Opcodes.class.getDeclaredFields()) {
			try {
				if (f.getName().startsWith("F_")
						|| f.getName().startsWith("T_")
						|| f.getName().startsWith("ACC_")
						|| f.getName().startsWith("V1_")
					    )
					continue;
				if (f.getInt(null) == opCode) {
					return f.getName();
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return "No Opcode for "+opCode;
	}
	
	String getFrameType(int frameType) {
		for (Field f : Opcodes.class.getDeclaredFields()) {
			try {
				if (!f.getName().startsWith("F_")
					    )
					continue;
				if (f.getInt(null) == frameType) {
					return f.getName();
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return "No frame type for "+frameType;
	}
	
	Type getArrayElementType(int type) {
		switch (type) {
		case T_BOOLEAN: return Type.BOOLEAN_TYPE;
		case T_BYTE: return Type.BYTE_TYPE;
		case T_CHAR: return Type.CHAR_TYPE;
		case T_DOUBLE: return Type.DOUBLE_TYPE;
		case T_FLOAT: return Type.FLOAT_TYPE;
		case T_INT: return Type.INT_TYPE;
		case T_LONG: return Type.LONG_TYPE;
		case T_SHORT: return Type.SHORT_TYPE;
		}
		throw new BuildStackFrameException("Illegal array type code: "+type);
	}
	
	Type getArrayType(int type) {
		Type t = getArrayElementType(type);
		return Type.getObjectType("["+t.getDescriptor());
	}
	
	static Type deferTypFromCanonicalName(String name) {
		return Type.getType(name);
	}

	static Type deferReturnType(String signature) {
		return Type.getReturnType(signature);
	}
	
	void savePreviousFrame() {
		previousFrame = new StackInfo(currentFrame);
	}

	void arrayLoad(Type arrayType) {
		currentFrame.popStackChecked(Type.INT_TYPE);
		currentFrame.replaceStack(arrayType);		
	}

	void arrayStore(Type arrayType) {
		currentFrame.popStackChecked(arrayType);
		currentFrame.popStackChecked(Type.INT_TYPE);
		currentFrame.popStack();		
	}

	@Override
	public StackInfo getPreviousStackInfo() {
		return new StackInfo(previousFrame);
	}
	
	@Override
	public StackInfo getCurrentStackInfo() {
		return new StackInfo(currentFrame);
	}

	private int[] getLineNumber(Label l) {
		int[] lineNo = lineNumbers.get(l);
		if (lineNo == null) {
			lineNumbers.put(l, lineNo = new int[]{-1});
		}
		return lineNo;
	}
	
	@Override
	public String[] getLocalNames(int lineNo, int count) {
		String[] names = new String[count];
		outerLoop: for (int index = 0; index < count; ++index) {
			names[index] = "var"+index;
			for (LocalVariable var : localVariables) {
				if (var.index == index && var.fromLine[0] <= lineNo && var.toLine[0] >= lineNo) {
					names[index] = var.name;
					continue outerLoop;
				}
			}
		}
		return names;
	}

	@Override
	public Type[] getLocalDescriptors(int lineNo, int count) {
		Type[] types = new Type[count];
		outerLoop: for (int index = 0; index < count; ++index) {
			for (LocalVariable var : localVariables) {
				if (var.index == index && var.fromLine[0] <= lineNo && var.toLine[0] >= lineNo) {
					types[index] = Type.getType(var.declaredDescriptor);
					continue outerLoop;
				}
			}
		}
		return types;
	}

	public class LocalVariable {
		
		public LocalVariable(String name, String declaredDescriptor, Label from, Label to, int index) {
			this.name     = name;
			this.fromLine = getLineNumber(from);
			this.toLine   =   getLineNumber(to);
			if (this.toLine[0] == -1)
				this.toLine[0] = Integer.MAX_VALUE;
			this.index    = index;
			this.declaredDescriptor = declaredDescriptor;
		}
		
		String name;
		int[]  fromLine;
		int[]  toLine;
		int    index;
		String declaredDescriptor;
		
	}

}
