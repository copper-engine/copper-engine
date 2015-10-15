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
    public Long timeout;

}
