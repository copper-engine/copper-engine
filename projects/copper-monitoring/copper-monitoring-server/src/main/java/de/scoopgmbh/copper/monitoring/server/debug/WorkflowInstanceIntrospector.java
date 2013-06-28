package de.scoopgmbh.copper.monitoring.server.debug;

import java.util.ArrayList;
import java.util.List;

import de.scoopgmbh.copper.StackEntry;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.instrument.ClassInfo;
import de.scoopgmbh.copper.instrument.MethodInfo;
import de.scoopgmbh.copper.instrument.MethodInfo.LabelInfo;
import de.scoopgmbh.copper.instrument.MethodInfo.LocalVariable;
import de.scoopgmbh.copper.instrument.MethodInfo.SerializableType;
import de.scoopgmbh.copper.monitoring.core.debug.Data;
import de.scoopgmbh.copper.monitoring.core.debug.DataTool;
import de.scoopgmbh.copper.monitoring.core.debug.Member;
import de.scoopgmbh.copper.monitoring.core.debug.Method;
import de.scoopgmbh.copper.monitoring.core.debug.StackFrame;
import de.scoopgmbh.copper.monitoring.core.debug.WorkflowInstanceDetailedInfo;
import de.scoopgmbh.copper.persistent.ScottyDBStorageInterface;

public class WorkflowInstanceIntrospector {

	ScottyDBStorageInterface dbStorageInterface;
	WorkflowRepository       workflowRepository;
	
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
			throw new RuntimeException("Metadata for workflow "+wf.getId()+" not found in repository");
		return getInstanceInfo(classInfo, wf);
	}
	
	
	public WorkflowInstanceDetailedInfo getInstanceInfo(ClassInfo classInfo, Workflow<?> workflow) {
		List<StackEntry> stack;
		try {
			stack = workflow.get__stack();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		List<StackFrame>  verboseStack = new ArrayList<StackFrame>(stack.size());
		ClassInfo[] definingClass = new ClassInfo[1];
		MethodInfo currentMethod = getMethod(classInfo, "main", "()V", definingClass);
		for (StackEntry en : stack) {
			Method method = new Method(currentMethod.getDefiningClass(), currentMethod.getDeclaration());
			LabelInfo lf = currentMethod.getLabelInfos().get(en.jumpNo);
			StackFrame sf = new StackFrame(method, lf.getLineNo(), definingClass[0].getSourceCode());
			for (int i = 0; i < lf.getLocals().length; ++i) {
				LocalVariable v = lf.getLocals()[i];
				if (v != null) {
					Object local = en.locals[i];
					Member m = new Member(v.getName(), v.getDeclaredType(), local != null?DataTool.convert(local):Data.NULL );
					sf.getLocals().add(m);
				}
			}
			for (int i = 0; i < lf.getStack().length; ++i) {
				SerializableType v = lf.getStack()[i];
				if (v != null) {
					Object local = en.stack[i];
					Member m = new Member(""+i, v.getDeclaredType(), local != null?DataTool.convert(local):Data.NULL );
					sf.getStack().add(m);
				}
			}
			verboseStack.add(sf);
			currentMethod = getMethod(classInfo, lf.getCalledMethodName(), lf.getCalledMethodDescriptor(), definingClass);
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
