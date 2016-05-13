package org.copperengine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class SortedReponseList extends ArrayList<Response<?>> {

    private static final long serialVersionUID = 1066747813168770997L;

    private boolean sorted = false;

    private void makeSureListIsSorted() {
        if (sorted)
            return;
        Collections.sort(this, ResponseComparator.INSTANCE);
        sorted = true;
    }

    @Override
    public Response<?> get(int index) {
        makeSureListIsSorted();
        return super.get(index);
    }

    @Override
    public int indexOf(Object o) {
        makeSureListIsSorted();
        return super.indexOf(o);
    }

    @Override
    public Iterator<Response<?>> iterator() {
        makeSureListIsSorted();
        return super.iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        makeSureListIsSorted();
        return super.lastIndexOf(o);
    }

    @Override
    public ListIterator<Response<?>> listIterator() {
        makeSureListIsSorted();
        return super.listIterator();
    }

    @Override
    public ListIterator<Response<?>> listIterator(int index) {
        makeSureListIsSorted();
        return super.listIterator(index);
    }

    @Override
    public List<Response<?>> subList(int fromIndex, int toIndex) {
        makeSureListIsSorted();
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        makeSureListIsSorted();
        return super.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        makeSureListIsSorted();
        return super.toArray(a);
    }

    @Override
    public String toString() {
        makeSureListIsSorted();
        return super.toString();
    }

    @Override
    public Object clone() {
        makeSureListIsSorted();
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        makeSureListIsSorted();
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        makeSureListIsSorted();
        return super.hashCode();
    }

    @Override
    public Response<?> remove(int index) {
        makeSureListIsSorted();
        return super.remove(index);
    }

}
