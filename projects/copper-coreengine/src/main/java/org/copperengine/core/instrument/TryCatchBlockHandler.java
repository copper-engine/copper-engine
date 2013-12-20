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
package org.copperengine.core.instrument;

import java.util.HashSet;
import java.util.List;

import org.copperengine.core.InterruptException;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds "if (e instanceof InterruptException) throw (InterruptException)e;" to each catch block.
 * This prevent COPPERs InterruptExceptions thrown by the COPPER wait calls to be handled in the exception handlers.
 *
 * @author austermann
 */
public class TryCatchBlockHandler {

    private static final Logger logger = LoggerFactory.getLogger(TryCatchBlockHandler.class);

    private static final String INTERRUPT_EXCEPTION_NAME = InterruptException.class.getName().replace('.', '/');

    @SuppressWarnings("unchecked")
    public void instrument(ClassNode cn) {
        // if (1 == 1) return;

        for (MethodNode m : (List<MethodNode>) cn.methods) {
            if (!m.exceptions.contains(INTERRUPT_EXCEPTION_NAME) || m.tryCatchBlocks.isEmpty()) {
                continue;
            }
            logger.info("Instrument " + cn.name + "." + m.name);
            HashSet<Label> labels = new HashSet<Label>();
            for (TryCatchBlockNode catchNode : (List<TryCatchBlockNode>) m.tryCatchBlocks) {
                if (labels.contains(catchNode.handler.getLabel())) {
                    // some handlers share their handling code - check it out to prevent double instrumentation
                    logger.info("skipping node");
                    continue;
                }
                labels.add(catchNode.handler.getLabel());

                LabelNode labelNode = catchNode.handler;
                AbstractInsnNode lineNumberNode = labelNode.getNext() instanceof LineNumberNode ? labelNode.getNext() : labelNode;
                FrameNode frameNode = (FrameNode) lineNumberNode.getNext();
                VarInsnNode varInsnNode = (VarInsnNode) frameNode.getNext();
                AbstractInsnNode insertPoint = varInsnNode;

                if (catchNode.type == null) {
                    // this is probably a finally block;
                    if (insertPoint.getNext() != null && insertPoint.getNext() instanceof LabelNode) {
                        insertPoint = insertPoint.getNext();
                    }
                }

                LabelNode labelNode4ifeg = new LabelNode();
                InsnList newCode = new InsnList();
                newCode.add(new VarInsnNode(Opcodes.ALOAD, varInsnNode.var));
                newCode.add(new TypeInsnNode(Opcodes.INSTANCEOF, INTERRUPT_EXCEPTION_NAME));
                newCode.add(new JumpInsnNode(Opcodes.IFEQ, labelNode4ifeg));
                newCode.add(new VarInsnNode(Opcodes.ALOAD, varInsnNode.var));
                newCode.add(new TypeInsnNode(Opcodes.CHECKCAST, INTERRUPT_EXCEPTION_NAME));
                newCode.add(new InsnNode(Opcodes.ATHROW));
                newCode.add(labelNode4ifeg);
                m.instructions.insert(insertPoint, newCode);
            }
        }
    }
}
