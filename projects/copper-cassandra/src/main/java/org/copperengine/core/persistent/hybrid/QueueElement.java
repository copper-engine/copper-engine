package org.copperengine.core.persistent.hybrid;

class QueueElement {

    public final String wfId;
    public final int prio;
    public final long enqueueTS = System.currentTimeMillis();

    public QueueElement(String wfId, int prio) {
        this.wfId = wfId;
        this.prio = prio;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((wfId == null) ? 0 : wfId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QueueElement other = (QueueElement) obj;
        if (wfId == null) {
            if (other.wfId != null)
                return false;
        } else if (!wfId.equals(other.wfId))
            return false;
        return true;
    }

}
