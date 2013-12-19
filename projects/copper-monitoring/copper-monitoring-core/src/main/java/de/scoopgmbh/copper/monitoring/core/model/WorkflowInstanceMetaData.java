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
package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;

import de.scoopgmbh.copper.monitoring.core.debug.WorkflowInstanceDetailedInfo;

public class WorkflowInstanceMetaData implements Serializable {
    private static final long serialVersionUID = -3474254791751446121L;

    private WorkflowClassMetaData workflowClassMetaData;
    final WorkflowInstanceDetailedInfo workflowInstanceDetailedInfo;

    public WorkflowInstanceMetaData(WorkflowClassMetaData workflowClassMetaData, WorkflowInstanceDetailedInfo workflowInstanceDetailedInfo) {
        super();
        this.workflowClassMetaData = workflowClassMetaData;
        this.workflowInstanceDetailedInfo = workflowInstanceDetailedInfo;
    }

    public WorkflowClassMetaData getWorkflowClassMetaData() {
        return workflowClassMetaData;
    }

    public void setWorkflowClassMetaData(WorkflowClassMetaData workflowClassMetaData) {
        this.workflowClassMetaData = workflowClassMetaData;
    }

    public WorkflowInstanceDetailedInfo getWorkflowInstanceDetailedInfo() {
        return workflowInstanceDetailedInfo;
    }

}
