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
package org.copperengine.monitoring.server.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.copperengine.core.StackEntry;
import org.copperengine.core.Workflow;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.instrument.ClassInfo;
import org.copperengine.core.instrument.MethodInfo;
import org.copperengine.core.instrument.MethodInfo.LabelInfo;
import org.copperengine.core.instrument.MethodInfo.LocalVariable;
import org.copperengine.core.instrument.MethodInfo.SerializableType;
import org.copperengine.core.persistent.ScottyDBStorageInterface;
import org.copperengine.monitoring.core.debug.Data;
import org.copperengine.monitoring.core.debug.DataTool;
import org.copperengine.monitoring.core.debug.Member;
import org.copperengine.monitoring.core.debug.Method;
import org.copperengine.monitoring.core.debug.StackFrame;
import org.copperengine.monitoring.core.debug.WorkflowInstanceDetailedInfo;
import org.objectweb.asm.Type;

public class WorkflowInstanceIntrospector {

    ScottyDBStorageInterface dbStorageInterface;
    WorkflowRepository workflowRepository;

    public WorkflowInstanceIntrospector(ScottyDBStorageInterface dbStorageInterface, WorkflowRepository workflowRepository) {
        this.dbStorageInterface = dbStorageInterface;
        this.workflowRepository = workflowRepository;
    }

    public WorkflowInstanceDetailedInfo getInstanceInfo(String workflowInstanceId) throws Exception {
        Workflow<?> wf = dbStorageInterface.read(workflowInstanceId);
        if (wf == null)
            return null;
        ClassInfo classInfo = workflowRepository.getClassInfo(wf.getClass());
        if (classInfo == null)
            throw new RuntimeException("Metadata for workflow " + wf.getId() + " not found in repository");
        return getInstanceInfo(classInfo, wf);
    }

    public WorkflowInstanceDetailedInfo getInstanceInfo(ClassInfo classInfo, Workflow<?> workflow) {
        List<StackEntry> stack;
        try {
            stack = workflow.get__stack();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        List<StackFrame> verboseStack = new ArrayList<StackFrame>(stack.size());
        ClassInfo[] definingClass = new ClassInfo[1];
        MethodInfo currentMethod = getMethod(classInfo, "main", "()V", definingClass);
        for (StackEntry en : stack) {
            Method method = new Method(currentMethod.getDefiningClass(), currentMethod.getDeclaration());
            List<LabelInfo> labelInfos = currentMethod.getLabelInfos();
            LabelInfo lf = labelInfos.size() >= en.jumpNo ?
                    new LabelInfo(en.jumpNo, -1, Collections.<String> emptyList(), Collections.<Type> emptyList(), Collections.<Type> emptyList(), Collections.<Type> emptyList(), "INCOMPATIBLE_STACKINFO_OUTPUT_ABORTED", "()V")
                    : labelInfos.get(en.jumpNo);
            StackFrame sf = new StackFrame(method, lf.getLineNo(), definingClass[0].getSourceCode());
            for (int i = 0; i < lf.getLocals().length; ++i) {
                LocalVariable v = lf.getLocals()[i];
                if (v != null) {
                    Object local = en.locals[i];
                    Member m = new Member(v.getName(), v.getDeclaredType(), local != null ? DataTool.convert(local) : Data.NULL);
                    sf.getLocals().add(m);
                }
            }
            for (int i = 0; i < lf.getStack().length; ++i) {
                SerializableType v = lf.getStack()[i];
                if (v != null) {
                    Object local = en.stack[i];
                    Member m = new Member("" + i, v.getDeclaredType(), local != null ? DataTool.convert(local) : Data.NULL);
                    sf.getStack().add(m);
                }
            }
            verboseStack.add(sf);
            currentMethod = getMethod(classInfo, lf.getCalledMethodName(), lf.getCalledMethodDescriptor(), definingClass);
            if (currentMethod == null)
                break;
        }
        return new WorkflowInstanceDetailedInfo(workflow.getId(), verboseStack);
    }

    private MethodInfo getMethod(ClassInfo classInfo,
            String methodName, String methodDescriptor, ClassInfo[] definingClass) {
        definingClass[0] = classInfo;
        for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
            if (methodName.equals(methodInfo.getMethodName()) && methodDescriptor.equals(methodInfo.getDescriptor()))
                return methodInfo;
        }
        if (classInfo.getSuperClassInfo() != null)
            return getMethod(classInfo.getSuperClassInfo(), methodName, methodDescriptor, definingClass);
        return null;
    }

}
