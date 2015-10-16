package org.copperengine.core.persistent.cassandra;

class QueueElement {

    public final String wfId;
    public final int prio;
    public final long enqueueTS = System.currentTimeMillis();

    public QueueElement(String wfId, int prio) {
        this.wfId = wfId;
        this.prio = prio;
    }

}
