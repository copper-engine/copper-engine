package org.copperengine.core.persistent.hybrid;

import java.util.Date;
import java.util.Map;

import org.copperengine.core.ProcessingState;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.SerializedWorkflow;

public class WorkflowInstance {

    public WorkflowInstance() {
    }

    public String id;
    public String ppoolId;
    public int prio;
    public Date creationTS;
    public SerializedWorkflow serializedWorkflow;
    public Map<String, String> cid2ResponseMap;
    public WaitMode waitMode;
    public Date timeout;
    public ProcessingState state;

    @Override
    public String toString() {
        return "WorkflowInstance [id=" + id + ", state=" + state + ", ppoolId=" + ppoolId + ", prio=" + prio + ", creationTS=" + creationTS + ", waitMode=" + waitMode + ", timeout=" + timeout + ", cid2ResponseMap.size=" + (cid2ResponseMap != null ? cid2ResponseMap.size() : 0) + "]";
    }

}
