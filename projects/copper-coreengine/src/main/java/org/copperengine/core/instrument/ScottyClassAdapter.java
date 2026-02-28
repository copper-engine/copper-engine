/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.core.instrument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.copperengine.core.wfrepo.checkpoint.CheckpointCollector;
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
    private final CheckpointCollector checkpointCollector;
    private final List<MethodInfo> methodInfos = new ArrayList<MethodInfo>();

    public ScottyClassAdapter(ClassVisitor cv, Set<String> interruptableMethods, CheckpointCollector checkpointCollector) {
        super(ASMConstants.API_VERSION, cv);
        this.interruptableMethods = interruptableMethods;
        this.checkpointCollector = checkpointCollector;
    }

    public ScottyClassAdapter(ClassVisitor cv, Set<String> interruptableMethods) {
        this(cv, interruptableMethods, null);
    }

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        currentClassName = name;
        logger.info("Transforming " + currentClassName);
        if (checkpointCollector != null) {
            checkpointCollector.workflowStart(name, superName);
        }
        super.visit(version, access, name, signature, superName, interfaces);
        super.visitAnnotation("Lorg/copperengine/core/instrument/Transformed;", true);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // Workaround for https://github.com/spotbugs/spotbugs/issues/500:
        if (interruptableMethods.contains(new StringBuilder(name).append(desc).toString()) && ((access & ACC_ABSTRACT) == 0)) {
        // TODO: replace the above workaround with the following line when the spotbug issue has been solved
        // if (interruptableMethods.contains(name + desc) && ((access & ACC_ABSTRACT) == 0)) {
            logger.debug("Transforming {}.{}{}", new Object[] { currentClassName, name, desc });
            MethodVisitor mv = cv.visitMethod(access,
                    name,
                    desc,
                    signature,
                    exceptions);

            String classDesc = Type.getObjectType(currentClassName).getDescriptor();
            BuildStackInfoAdapter stackInfo = new BuildStackInfoAdapter(classDesc, (access & ACC_STATIC) > 0, name, desc, signature);
            final ScottyMethodAdapter scotty = checkpointCollector != null
                    ? new ScottyMethodAdapter(mv, currentClassName, interruptableMethods, stackInfo, name, access, desc, checkpointCollector)
                    : new ScottyMethodAdapter(mv, currentClassName, interruptableMethods, stackInfo, name, access, desc);
            MethodVisitor collectMethodInfo = new MethodVisitor(ASMConstants.API_VERSION, stackInfo) {
                @Override
                public void visitEnd() {
                    super.visitEnd();
                    methodInfos.add(scotty.getMethodInfo());
                }
            };
            stackInfo.setMethodVisitor(scotty);
            // ScottyMethodAdapter stackInfo = new ScottyMethodAdapter(mv, currentClassName, interruptableMethods);
            return collectMethodInfo;
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (checkpointCollector != null) {
            checkpointCollector.workflowEnd(this.currentClassName);
        }
    }

    public ClassInfo getClassInfo() {
        return new ClassInfo(methodInfos);
    }
}
