package org.copperengine.core;

import java.util.Comparator;

class ResponseComparator implements Comparator<Response<?>> {

    static ResponseComparator INSTANCE = new ResponseComparator();

    @Override
    public int compare(Response<?> o1, Response<?> o2) {
        if (o1.getSequenceId() == null && o2.getSequenceId() == null) {
            return 0;
        }
        if (o1.getSequenceId() == null && o2.getSequenceId() != null) {
            return -1;
        }
        if (o1.getSequenceId() != null && o2.getSequenceId() == null) {
            return 1;
        }
        return o1.getSequenceId().compareTo(o2.getSequenceId());
    }

}
