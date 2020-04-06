/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.core.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuspendableQueue<T> implements Queue<T> {

    private static final Logger logger = LoggerFactory.getLogger(SuspendableQueue.class);

    private Queue<T> queue;
    private boolean suspended = false;

    public SuspendableQueue(Queue<T> queue) {
        if (queue == null)
            throw new NullPointerException();
        this.queue = queue;
    }

    public void setSuspended(boolean suspended) {
        logger.info("Setting suspended to {}", suspended);
        this.suspended = suspended;
    }

    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return queue.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <X> X[] toArray(X[] a) {
        return queue.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return queue.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return queue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return queue.retainAll(c);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public boolean add(T e) {
        return queue.add(e);
    }

    @Override
    public boolean offer(T e) {
        return queue.offer(e);
    }

    @Override
    public T remove() {
        if (suspended)
            throw new NoSuchElementException();

        return queue.remove();
    }

    @Override
    public T poll() {
        if (suspended)
            return null;

        return queue.poll();
    }

    @Override
    public T element() {
        if (suspended)
            throw new NoSuchElementException();

        return queue.element();
    }

    @Override
    public T peek() {
        if (suspended)
            return null;

        return queue.peek();
    }

}
