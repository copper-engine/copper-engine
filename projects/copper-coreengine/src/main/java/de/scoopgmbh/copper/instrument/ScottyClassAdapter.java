 package de.scoopgmbh.copper.instrument;

import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ScottyClassAdapter extends ClassAdapter implements Opcodes {

	private static final Logger logger = Logger.getLogger(ScottyClassAdapter.class);
	
	private String currentClassName;
	private final Set<String> interruptableMethods;

	public ScottyClassAdapter(ClassVisitor cv, Set<String> interruptableMethods) {
		super(cv);
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
			logger.debug("Transforming "+currentClassName+"."+name+desc);
			MethodVisitor mv = cv.visitMethod(access,
					name,
					desc,
					signature,
					exceptions);
			
			String classDesc = Type.getObjectType(currentClassName).getDescriptor();
			BuildStackInfoAdapter stackInfo = new BuildStackInfoAdapter(classDesc,(access & ACC_STATIC) > 0, name, desc, signature);
			ScottyMethodAdapter scotty = new ScottyMethodAdapter(mv, currentClassName, interruptableMethods, stackInfo, name);
			stackInfo.setMethodVisitor(scotty);
//			ScottyMethodAdapter stackInfo = new ScottyMethodAdapter(mv, currentClassName, interruptableMethods);
			return stackInfo;
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
	
	@Override
	public void visitEnd() {
		super.visitEnd();
	}
}
