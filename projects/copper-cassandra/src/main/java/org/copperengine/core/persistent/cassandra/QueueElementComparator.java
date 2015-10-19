package org.copperengine.core.persistent.cassandra;

import java.util.Comparator;

class QueueElementComparator implements Comparator<QueueElement> {
    @Override
    public int compare(QueueElement o1, QueueElement o2) {
        if (o1.prio != o2.prio) {
            return o1.prio - o2.prio;
        } else {
            if (o1.enqueueTS == o2.enqueueTS)
                return 0;
            if (o1.enqueueTS > o2.enqueueTS)
                return 1;
            return -1;
        }
    }
}
