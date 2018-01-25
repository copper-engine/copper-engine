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
import java.util.ArrayList;
import java.util.List;

public class WorkflowInstanceFilter implements Serializable {
    
    private static final long serialVersionUID = 3695017848783764269L;
    
    private List<String> states = new ArrayList<>();
    private HalfOpenTimeInterval lastModTS;
    private HalfOpenTimeInterval creationTS;
    private String processorPoolId;
    private String workflowClassname;
    private int max = 50;
    private int offset = 0;
    
    public WorkflowInstanceFilter() {
    }

    @ConstructorProperties({"states","lastModTS","creationTS","processorPoolId","workflowClassname","max","offset"})
    public WorkflowInstanceFilter(List<String> states, HalfOpenTimeInterval lastModTS, HalfOpenTimeInterval creationTS, String processorPoolId, String workflowClassname, int max, int offset) {
        this.states = states;
        this.lastModTS = lastModTS;
        this.creationTS = creationTS;
        this.processorPoolId = processorPoolId;
        this.workflowClassname = workflowClassname;
        this.max = max;
        this.offset = offset;
    }
    
    public List<String> getStates() {
        return states;
    }

    public void setStates(List<String> state) {
        this.states = state;
    }
    public void addState(String state) {
        this.states.add(state);
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

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "WorkflowInstanceFilter [states=[" + (states != null ? String.join(", ", states) : "") + "], lastModTS=" + lastModTS
                    + ", creationTS=" + creationTS + ", processorPoolId=" + processorPoolId
                    + ", workflowClassname=" + workflowClassname +", max=" + max + ", offset=" + offset + "]";
    }

}
