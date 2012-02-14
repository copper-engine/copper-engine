/*
 * Copyright 2002-2012 SCOOP Software GmbH
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

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
class ScottyDynamicFindInterruptableMethodsVisitor extends EmptyVisitor {

	private static final Logger logger = LoggerFactory.getLogger(ScottyDynamicFindInterruptableMethodsVisitor.class);

	private final ClassLoader classLoader;
	private final Set<String> interruptableMethods = new HashSet<String>();
	private final Set<String> loadedClasses = new HashSet<String>();
	private String method;

	public ScottyDynamicFindInterruptableMethodsVisitor(final ClassLoader classLoader) {
		this.classLoader= classLoader;
	}

	public void reset() {
		method = null;
	}

	public Set<String> getInterruptableMethods() {
		return interruptableMethods;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		loadedClasses.add(name);
		if (!superName.equals("de/scoopgmbh/copper/Workflow") && !superName.startsWith("java") && !loadedClasses.contains(superName)) {
			try {
				classLoader.loadClass(superName.replace("/", "."));
			} 
			catch (ClassNotFoundException e) {
				logger.error("loadClass for "+superName+" failed",e);
			}
		}
		
		reset();
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		method = name+desc;
		if (exceptions != null && exceptions.length > 0) {
			for (String e : exceptions) {
				if ("de/scoopgmbh/copper/InterruptException".equals(e)) {
					interruptableMethods.add(method);
				}
			}
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		super.visitMethodInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		if ("de/scoopgmbh/copper/InterruptException".equals(type)) {
			throw new RuntimeException("InterruptException must not be handled!");
		}
		super.visitTryCatchBlock(start, end, handler, type);
	}

}
