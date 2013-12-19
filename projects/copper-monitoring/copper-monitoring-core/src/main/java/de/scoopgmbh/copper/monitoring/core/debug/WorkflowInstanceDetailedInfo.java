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
package de.scoopgmbh.copper.monitoring.core.debug;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class WorkflowInstanceDetailedInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    String workflowInstanceId;
    List<StackFrame> stack;

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public List<StackFrame> getStack() {
        return Collections.unmodifiableList(stack);
    }

    public WorkflowInstanceDetailedInfo(String workflowInstanceId, List<StackFrame> stack) {
        this.workflowInstanceId = workflowInstanceId;
        this.stack = stack;
    }

}
