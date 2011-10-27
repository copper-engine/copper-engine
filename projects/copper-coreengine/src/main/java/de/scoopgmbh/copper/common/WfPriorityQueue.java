/*
 * Copyright 2002-2011 SCOOP Software GmbH
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
package de.scoopgmbh.copper.common;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

import de.scoopgmbh.copper.Workflow;

/**
 * Priority queue for {@link Workflow} instances.
 * 
 * Entries in the queue are ordered using their priority and enqueue timestamp
 * 
 * @author austermann
 *
 */
public class WfPriorityQueue implements Queue<Workflow<?>> {

	private static class QueueEntry {
		long enqueueTS = System.currentTimeMillis();
		Workflow<?> workflow;
		public QueueEntry(Workflow<?> workflow) {
			this.workflow = workflow;
		}
	}
	
	private Queue<QueueEntry> queue;

	public WfPriorityQueue() {
		this(10000);
	}
	
	public WfPriorityQueue(final int initialSize) {
		queue = new PriorityQueue<QueueEntry>(10000, new Comparator<QueueEntry>() {
			public int compare(QueueEntry o1, QueueEntry o2) {
				if (o1.workflow.getPriority() != o2.workflow.getPriority()) {
					return o1.workflow.getPriority() - o2.workflow.getPriority();
				}
				else {
					if (o1.enqueueTS == o2.enqueueTS)
						return 0;
					if (o1.enqueueTS > o2.enqueueTS)
						return 1;
					return -1;
				}
			};
		});		
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
	public Iterator<Workflow<?>> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Workflow<?>> c) {
		for (Workflow<?> wf : c) {
			add(wf);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		queue.clear();
	}

	@Override
	public boolean add(Workflow<?> e) {
		return queue.add(new QueueEntry(e));
	}

	@Override
	public boolean offer(Workflow<?> e) {
		return queue.offer(new QueueEntry(e));
	}

	@Override
	public Workflow<?> remove() {
		QueueEntry e = queue.remove();
		return e != null ? e.workflow : null;
	}

	@Override
	public Workflow<?> poll() {
		QueueEntry e = queue.poll();
		return e != null ? e.workflow : null;
	}

	@Override
	public Workflow<?> element() {
		QueueEntry e = queue.element();
		return e != null ? e.workflow : null;
	}

	@Override
	public Workflow<?> peek() {
		QueueEntry e = queue.peek();
		return e != null ? e.workflow : null;
	}

}
