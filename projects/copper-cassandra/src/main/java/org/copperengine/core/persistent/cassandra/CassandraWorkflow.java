package org.copperengine.core.persistent.cassandra;

import java.util.Date;
import java.util.Map;

import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.SerializedWorkflow;

public class CassandraWorkflow {

    public CassandraWorkflow() {
    }

    public String id;
    public String ppoolId;
    public int prio;
    public Date creationTS;
    public SerializedWorkflow serializedWorkflow;
    public Map<String, String> cid2ResponseMap;
    public WaitMode waitMode;
    public Date timeout;

    @Override
    public String toString() {
        return "CassandraWorkflow [id=" + id + ", ppoolId=" + ppoolId + ", prio=" + prio + ", creationTS=" + creationTS + ", waitMode=" + waitMode + ", timeout=" + timeout + ", cid2ResponseMap.size=" + (cid2ResponseMap != null ? cid2ResponseMap.size() : 0) + "]";
    }

}
