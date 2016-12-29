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
    private boolean details; // TODO remove
    private int max;
    
    public WorkflowInstanceFilter() {
    }

    @ConstructorProperties({"state","lastModTS","creationTS","processorPoolId","workflowClassname","details","max"})
    public WorkflowInstanceFilter(String state, HalfOpenTimeInterval lastModTS, HalfOpenTimeInterval creationTS, String processorPoolId, String workflowClassname, boolean details, int max) {
        this.state = state;
        this.lastModTS = lastModTS;
        this.creationTS = creationTS;
        this.processorPoolId = processorPoolId;
        this.workflowClassname = workflowClassname;
        this.max = max;
        this.details = details;
    }
    
    public boolean isDetails() {
        return details;
    }
    
    public void setDetails(boolean details) {
        this.details = details;
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
        return "WorkflowInstanceFilter [state=" + state + ", lastModTS=" + lastModTS + ", creationTS=" + creationTS + ", processorPoolId=" + processorPoolId + ", workflowClassname=" + workflowClassname + ", details=" + details + ", max=" + max + "]";
    }
    
}
