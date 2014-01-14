package org.copperengine.core.persistent;

import org.copperengine.core.WorkflowAccessor;


/**
 *  for internal use only.
 *
 */
public class PersistentWorkflowAccessor extends WorkflowAccessor {

    public RegisterCall getRegisterCall(PersistentWorkflow pw){
        return pw.registerCall;
    }
}
