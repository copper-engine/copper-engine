/*
 * Copyright 2002-2012 SCOOP Software GmbH
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

import java.util.Arrays;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import org.apache.log4j.Logger;


/**
 * 
 * Helper class that implements a <i>semaphore</i> to support a system overload mechanism.<br>
 * Before a request is to be accepted, a <i>ticket</i> must be obtained. If the
 * maximum amount of tickets in the pool is reached, the request is delayed
 * until another process has released its ticket. After obtaining a ticket, the
 * system must release this ticket back to the TicketPool.
 * 
 * @author jsiebeck
 * 
 */
public class TicketPool {
	private static final long TIMESLICE_WIDTH = 10000;

	private static Logger logger = Logger.getLogger(TicketPool.class);

	private final String id;
	private int maxTickets;
	private int used;

	/* variables for JMX observation */
	private long lastTimeslice = System.currentTimeMillis() / TIMESLICE_WIDTH;
	private long notificationSequence = 0;
	/**
	 * stores the number of waits and the wait times within one time slice,
	 * broken down logarithmically into wait times of
	 * <code><10, <100, <1000, ... msec</code> as follows:
	 * 
	 * <code>pos 2n</code>: number of waits <= (n+1)^10 msec
	 * <code>pos 2n+1</code>: sum of wait ms of waits in pos 2n
	 */
	long waitTimes[] = new long[10];
	private boolean traceEnabled = false;
	private NotificationBroadcasterSupport notificationSupport = null;

	public TicketPool(String id, int availableTickets) {
		super();
		/* set the id to a unique name, if none was provided */
		this.id = (id == null ? super.toString() : id);
		setCapacity(availableTickets);

		//TicketPoolObserver.getInstance().register(this, this.id);
	}

	public TicketPool(int availableTickets) {
		this(null, availableTickets);
	}

	public long availableTickets() {
		return maxTickets - used;
	}

	public int getUsedTickets() {
		return used;
	}

	public int getMaxTickets() {
		return maxTickets;
	}

	public synchronized void setCapacity(int availableTickets) {
		if (availableTickets <= 0)
			throw new IllegalArgumentException("There should be at least one ticket available!");
		maxTickets = availableTickets;
	}

	/**
	 * Obtain a ticket.
	 */
	public void obtain() {
		this.obtain(1, false);
	}

	/**
	 * Obtain a given amount of tickets. The thread is locked, until all tickets
	 * are obtained.
	 * 
	 * @param count number of tickets to obtain. must be 0 > count < maxTickets
	 * @throws IllegalArgumentException
	 */
	public void obtain(int count) throws IllegalArgumentException {
		obtain(count, false);
	}

	public void obtain(int count, boolean force) throws IllegalArgumentException {
		obtain(count, force, false);
	}

	public synchronized boolean obtain(int count, boolean force, boolean noWait) throws IllegalArgumentException {
		boolean waited = false;
		final long startWait = System.currentTimeMillis();
		if (!force && count > maxTickets)
			throw new IllegalArgumentException("Cannot obtain more tickets than maximum.");
		if (count <= 0)
			throw new IllegalArgumentException("Cannot obtain zero or less tickets.");
		if (!force) {
			if (logger.isDebugEnabled())
				logger.debug("Trying to obtain " + count + " tickets. " + this.toString());
		} else {
			if (logger.isDebugEnabled())
				logger.debug("Forcing to obtain " + count + " tickets. " + this.toString());
		}
		if (!force) {
			while (used + count > maxTickets) {
				try {
					if (!noWait) {
						if (logger.isDebugEnabled())
							logger.debug("I am waiting...");
						wait();
						waited = true;
					} else {
						return false;
					}
				} catch (InterruptedException e) {
					logger.warn("wait interrupted", e);
				}
			}
		}
		used += count;
		if (logger.isDebugEnabled())
			logger.debug("Obtained my tickets! " + this.toString());
		notifyAll();

		/* if enabled, collect statistics for JMX Notifications */
		if (waited && traceEnabled && notificationSupport != null) {
			long stopWait = System.currentTimeMillis() - startWait;

			final int pos = Math.min((int) Math.log10(stopWait + 1), 4);
			waitTimes[2 * pos]++;
			waitTimes[2 * pos + 1] += stopWait;

			long currentTimeslice = System.currentTimeMillis() / TIMESLICE_WIDTH;
			if (currentTimeslice > lastTimeslice) {
				final Notification notification = new Notification("10sec. stats", this, notificationSequence++, Arrays.toString(waitTimes));
				notification.setUserData(waitTimes);
				notificationSupport.sendNotification(notification);
				waitTimes = new long[10];
				lastTimeslice = currentTimeslice;
			}
		}
		return true;
	}

	/**
	 * Releases the given number of tickets and notifies potentially waiting
	 * threads, that new tickets are in the pool.
	 * 
	 * @param count
	 */
	public synchronized void release(int count) {
		used -= count;
		if (used < 0)
			used = 0; // no negative number of ticket!
		if (logger.isDebugEnabled())
			logger.debug("Released " + count + " tickets! (Now " + this.toString() + ")");
		notifyAll();
	}

	public synchronized String toString() {
		return id + ": " + used + " of " + maxTickets + " tickets used";
	}

	/**
	 * Releases a ticket and notifies potentially waiting threads, that new
	 * tickets are in the pool.
	 */
	public void release() {
		this.release(1);
	}

	public String getName() {
		return id;
	}

	public String getId() {
		return id;
	}

	public int getCapacity() {
		return getMaxTickets();
	}

	public boolean isTraceEnabled() {
		return traceEnabled;
	}

	/**
	 * you must set {@link #traceEnabled} to <code>true</code> and set a
	 * {@link NotificationBroadcasterSupport} to allow monitoring wait times via
	 * JMX {@link Notification}s
	 * 
	 * @param traceEnabled
	 * @see #setNotificationBroadcasterSupport(NotificationBroadcasterSupport)
	 */
	public void setTraceEnabled(boolean traceEnabled) {
		this.traceEnabled = traceEnabled;
	}

	/**
	 * you must set a {@link NotificationBroadcasterSupport} and set
	 * {@link #traceEnabled} to <code>true</code> and to allow monitoring wait
	 * times via JMX {@link Notification}s
	 * 
	 * @param nbs
	 * @see #setTraceEnabled(boolean)
	 */
	public void setNotificationBroadcasterSupport(NotificationBroadcasterSupport nbs) {
		notificationSupport = nbs;
	}
}
