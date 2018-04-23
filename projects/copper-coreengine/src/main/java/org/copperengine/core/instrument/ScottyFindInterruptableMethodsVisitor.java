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

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ScottyFindInterruptableMethodsVisitor extends ClassVisitor implements Opcodes {

    private final Set<String> interruptableMethods = new HashSet<String>();
    private String method = null;
    private String classname;
    private String superClassname;

    public ScottyFindInterruptableMethodsVisitor() {
        super(ASM6);
    }

    public void reset() {
        method = null;
    }

    public Set<String> getInterruptableMethods() {
        return interruptableMethods;
    }

    public String getSuperClassname() {
        return superClassname;
    }

    public String getClassname() {
        return classname;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.classname = name;
        this.superClassname = superName;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        method = name + desc;
        if (exceptions != null && exceptions.length > 0) {
            for (String e : exceptions) {
                if ("org/copperengine/core/Interrupt".equals(e)) {
                    interruptableMethods.add(method);
                }
            }
        }
        return new MethodVisitor(ASM6, super.visitMethod(access, name, desc, signature, exceptions)) {
            @Override
            public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                if ("org/copperengine/core/Interrupt".equals(type)) {
                    throw new RuntimeException("Interrupt must not be handled!");
                }
                super.visitTryCatchBlock(start, end, handler, type);
            }
        };
    }

}
