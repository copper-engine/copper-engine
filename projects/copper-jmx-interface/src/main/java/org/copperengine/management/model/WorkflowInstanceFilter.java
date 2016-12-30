/*
 * Copyright 2002-2017 SCOOP Software GmbH
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

package org.copperengine.management.model;

import java.beans.ConstructorProperties;
import java.io.Serializable;

public class WorkflowInstanceFilter implements Serializable {
    
    private static final long serialVersionUID = 3695017848783764269L;
    
    private String state;
    private HalfOpenTimeInterval lastModTS;
    private HalfOpenTimeInterval creationTS;
    private String processorPoolId;
    private String workflowClassname;
    private int max = 50;
    
    public WorkflowInstanceFilter() {
    }

    @ConstructorProperties({"state","lastModTS","creationTS","processorPoolId","workflowClassname","max"})
    public WorkflowInstanceFilter(String state, HalfOpenTimeInterval lastModTS, HalfOpenTimeInterval creationTS, String processorPoolId, String workflowClassname, int max) {
        this.state = state;
        this.lastModTS = lastModTS;
        this.creationTS = creationTS;
        this.processorPoolId = processorPoolId;
        this.workflowClassname = workflowClassname;
        this.max = max;
    }
    
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public HalfOpenTimeInterval getLastModTS() {
        return lastModTS;
    }

    public void setLastModTS(HalfOpenTimeInterval lastModTS) {
        this.lastModTS = lastModTS;
    }

    public HalfOpenTimeInterval getCreationTS() {
        return creationTS;
    }

    public void setCreationTS(HalfOpenTimeInterval creationTS) {
        this.creationTS = creationTS;
    }

    public String getProcessorPoolId() {
        return processorPoolId;
    }

    public void setProcessorPoolId(String processorPoolId) {
        this.processorPoolId = processorPoolId;
    }

    public String getWorkflowClassname() {
        return workflowClassname;
    }

    public void setWorkflowClassname(String workflowClassname) {
        this.workflowClassname = workflowClassname;
    }
    
    public int getMax() {
        return max;
    }
    
    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "WorkflowInstanceFilter [state=" + state + ", lastModTS=" + lastModTS + ", creationTS=" + creationTS + ", processorPoolId=" + processorPoolId + ", workflowClassname=" + workflowClassname +", max=" + max + "]";
    }
    
}
