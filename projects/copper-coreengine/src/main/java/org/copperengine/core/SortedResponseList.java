package org.copperengine.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortedResponseList extends AbstractList<Response<?>> {

    private boolean sorted = false;
    private List<Response<?>> data = new ArrayList<>();

    private void makeSureListIsSorted() {
        if (sorted)
            return;
        Collections.sort(data, ResponseComparator.INSTANCE);
        sorted = true;
    }

    @Override
    public boolean add(Response<?> e) {
        return data.add(e);
    }

    @Override
    public Response<?> get(int index) {
        makeSureListIsSorted();
        return data.get(index);
    }

    @Override
    public Response<?> remove(int index) {
        makeSureListIsSorted();
        return data.remove(index);
    }

    @Override
    public int size() {
        return data.size();
    }

}
