 package de.scoopgmbh.copper.instrument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScottyClassAdapter extends ClassVisitor implements Opcodes {

	private static final Logger logger = LoggerFactory.getLogger(ScottyClassAdapter.class);
	
	private String currentClassName;
	private final Set<String> interruptableMethods;
	private final List<MethodInfo> methodInfos = new ArrayList<MethodInfo>();

	public ScottyClassAdapter(ClassVisitor cv, Set<String> interruptableMethods) {
		super(ASM4,cv);
		this.interruptableMethods = interruptableMethods;
	}
	
	@Override
	public void visit(
			final int version,
			final int access,
			final String name,
			final String signature,
			final String superName,
			final String[] interfaces)
	{
		currentClassName = name;
		logger.info("Transforming "+currentClassName);
		super.visit(version, access, name, signature, superName, interfaces);
		super.visitAnnotation("Lde/scoopgmbh/copper/instrument/Transformed;", true);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (interruptableMethods.contains(name+desc) && ((access & ACC_ABSTRACT) == 0)) {
			logger.debug("Transforming {}.{}{}",new Object[] {currentClassName,name,desc});
			MethodVisitor mv = cv.visitMethod(access,
					name,
					desc,
					signature,
					exceptions);
			
			String classDesc = Type.getObjectType(currentClassName).getDescriptor();
			BuildStackInfoAdapter stackInfo = new BuildStackInfoAdapter(classDesc,(access & ACC_STATIC) > 0, name, desc, signature);
			final ScottyMethodAdapter scotty = new ScottyMethodAdapter(mv, currentClassName, interruptableMethods, stackInfo, name, access, desc);
			MethodVisitor collectMethodInfo = new MethodVisitor(Opcodes.ASM4,stackInfo) {
				@Override
				public void visitEnd() {
					super.visitEnd();
					methodInfos.add(scotty.getMethodInfo());
				}
			};
			stackInfo.setMethodVisitor(scotty);
//			ScottyMethodAdapter stackInfo = new ScottyMethodAdapter(mv, currentClassName, interruptableMethods);
			return collectMethodInfo;
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
	
	@Override
	public void visitEnd() {
		super.visitEnd();
	}
	
	public ClassInfo getClassInfo() {
		return new ClassInfo(methodInfos);		
	}
}
