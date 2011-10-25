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
package de.scoopgmbh.copper.util;

import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * Implements a queue datastructure backed by a customized implementation of a LinkedList. <br>
 * Special features are:
 * <ul>
 * <li>capacity check, enqueue throws exception, if initial capacity has been already reached.
 * <li>dequeue waits until a timeout is reached or another thread has populated the queue
 * </ul>
 * @author SCOOP Software GmbH
 */
public class Queue {

	private static final Logger logger = Logger.getLogger(Queue.class);
	private static final boolean OSF1 = System.getProperty("os.name").equalsIgnoreCase("OSF1");
	private static final TimeoutException timeoutException = new TimeoutException();
	protected String name = null;

	protected static class List extends LinkedList {
		private static final long serialVersionUID = 1L;

		protected Object lock = new Object();
		private boolean closed = false;
		private String closingThread = null;
		protected int capacity;
		protected boolean verbose;

		public List(int capacity, boolean verbose) {
			this.capacity = capacity;
			this.verbose = verbose;
		}

		public final boolean isClosed() {
			return closed;
		}

		public void close() {
			synchronized (lock) {
				if (verbose) logger.info("closing queue " + this + " ...");
				closingThread = Thread.currentThread().getName();
				closed = true;
				if (OSF1)
					lock.notifyAll();
				else
					lock.notify();
			}
		}

		public void addFirst(final Object o) {
			synchronized (lock) {
				super.addFirst(o);
				if (OSF1)
					lock.notifyAll();
				else
					lock.notify();
			}
		}

		public void addLast(final Object o) {
			// bl�d, aber im Moment f�llt mir nichts besseres ein:
			if (verbose) logger.fatal("illegal method call", new IllegalStateException());
			assert false : "illegal method call";
		}

		public void addLastElement(final Object o) throws OverflowException, IllegalStateException {
			synchronized (lock) {
                // TODO_MA: Ugly: Should fire a ClosedException, but due to downward compatibility a RuntimeException is thrown.
				if (isClosed()) throw new IllegalStateException("Queue is closed.");  
				if (capacity > -1 && size() >= capacity) {
					throw new OverflowException();
				}
				super.addLast(o);
				if (OSF1)
					lock.notifyAll();
				else
					lock.notify();
			}
		}
		
		private final Object removeOSF1(final long timeout) throws ClosedException, TimeoutException, InterruptedException {
			final long start = timeout == 0 ? 0 : System.currentTimeMillis();
			synchronized (lock) {
				while (true) {
					if (closed) {
						if (verbose) logger.info("queue was closed by thread " + closingThread);
						throw new ClosedException();
					}
					if (size() == 0) {
						lock.wait(timeout);
					}
					if (closed) {
						if (verbose) logger.info("queue was closed by thread " + closingThread);
						throw new ClosedException();
					}
					if (size() == 0) {
						if (timeout != 0 && System.currentTimeMillis() > start + timeout) {
							if (verbose) logger.debug("timeout [" + timeout + " msecs] reached while waiting for data in queue");
							throw timeoutException;
						} else {
							if (verbose) logger.debug("unexpected wakeup encountered - retrying ...");
						}
					} else {
						return removeFirst();
					}
				}
			}
		}

		private final Object removeNonOSF1(final long timeout) throws ClosedException, TimeoutException, InterruptedException {
			synchronized (lock) {
				while (true) {
					if (closed) {
						if (verbose) logger.info("queue was closed by thread " + closingThread);
						throw new ClosedException();
					}
					if (size() == 0) {
						lock.wait(timeout);
					}
					if (closed) {
						if (verbose) logger.info("queue was closed by thread " + closingThread);
						throw new ClosedException();
					}
					if (size() == 0) {
						if (timeout != 0) {
							if (verbose) logger.debug("timeout [" + timeout + " msecs] reached while waiting for data in queue");
							throw timeoutException;
						} else {
							if (verbose) logger.debug("unexpected wakeup encountered - retrying ...");
						}
					} else {
						return removeFirst();
					}
				}
			}
		}
		public Object remove(final long timeout) throws ClosedException, TimeoutException, InterruptedException {
			if (OSF1)
				return removeOSF1(timeout);
			else
				return removeNonOSF1(timeout);
		}

		public Object removeOrNull() throws ClosedException, TimeoutException, InterruptedException {
			synchronized (lock) {
				if (size() > 0) {
					return remove(0L);
				} else {
					return null;
				}
			}
		}

		public Object getFirstElement() throws EmptyQueueException {
			synchronized (lock) {
				if (isEmpty())
					throw new EmptyQueueException();
				return getFirst();
			}
		}

		public void setCapacity(int c) {
			if (c < 0) {
				throw new IllegalArgumentException("illegal argument " + c + " in Queue.setCapacity");
			}
			capacity = c;
		}

		public int getCapacity() {
			return capacity;
		}
	}

	protected final List list;
	private final long timeout;

	public static class EmptyQueueException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public static class TimeoutException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public static class ClosedException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public static class OverflowException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public static class TimeoutValue {

		public final long msecs;

		public TimeoutValue(final long msecs) {
			this.msecs = msecs;
		}
	}

	public static class CapacityValue {

		public final int capacity;

		public CapacityValue(final int capacity) {
			this.capacity = capacity;
		}
	}

	public Queue(final TimeoutValue timeout) {
		this(timeout, new CapacityValue(-1), true);
	}

	public Queue(final CapacityValue capacity) {
		this(new TimeoutValue(0), capacity, true);
	}

	public Queue(final TimeoutValue timeout, final CapacityValue capacity) {
		this(timeout, capacity, true);
	}

	public Queue(final TimeoutValue timeout, final CapacityValue capacity, final boolean verbose) {
		this.timeout = timeout.msecs;
		list = createList(capacity.capacity, verbose);
	}

	public Queue() {
		this(new TimeoutValue(0), new CapacityValue(-1), true);
	}

	/**
	 * Add element to the end of the underlying list.
	 * @param o object to add to the queue
	 * @throws OverflowException if maximum capacity of queue is reached
	 */
	public void enqueue(final Object o) throws OverflowException {
		list.addLastElement(o);
	}

	/**
	 * Return first element of the underlying list and remove it. If no element exists, wait (max. timeout milliseconds)
	 * until the queue is filled again.
	 * @param timeout 0 to disable timeout
	 * @return
	 * @throws TimeoutException
	 * @throws ClosedException
	 * @throws InterruptedException
	 */
	public Object dequeue(final long timeout) throws TimeoutException, ClosedException, InterruptedException {
		return list.remove(timeout == 0 ? this.timeout : timeout);
	}

	/**
	 * Return first element of the underlying list and remove it. If no element exists, wait until the queue is filled
	 * again.
	 * @return
	 * @throws TimeoutException
	 * @throws ClosedException
	 * @throws InterruptedException
	 */
	public Object dequeue() throws TimeoutException, ClosedException, InterruptedException {
		return dequeue(0);
	}

	/**
	 * @return Return first element of the underlying list or null, if queue is empty.
	 * @throws TimeoutException
	 * @throws ClosedException
	 * @throws InterruptedException
	 */
	public Object dequeueOrNull() throws TimeoutException, ClosedException, InterruptedException {
		return list.removeOrNull();
	}

	/**
	 * @return First element in the queue
	 * @throws EmptyQueueException
	 */
	public Object front() throws EmptyQueueException {
		return list.getFirstElement();
	}

	public final int size() {
		return list.size();
	}

	public final void close() {
		list.close();
	}

	public final boolean isClosed() {
		return list.isClosed();
	}

	public int getCapacity() {
		return list.getCapacity();
	}

	public void setCapacity(final Queue.CapacityValue val) {
		list.setCapacity(val.capacity);
	}

	public String getName() {
		return name == null ? getClass().getName() : name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Create the internal list (allow derived classes to supply their own list implementation)
	 * @param capacity
	 * @return
	 */
	protected List createList(int capacity, boolean verbose) {
		return new List(capacity, verbose);
	}
}
