package de.scoopgmbh.copper.persistent.adapter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.batcher.Batcher;
import de.scoopgmbh.copper.persistent.adapter.AdapterCallPersisterFactory.Selector;
import de.scoopgmbh.copper.persistent.txn.DatabaseTransaction;
import de.scoopgmbh.copper.persistent.txn.TransactionController;

public class NonTransactionalAdapterQueue {
	
	private static final Logger logger = LoggerFactory.getLogger(ReloadThread.class);

	final int transientQueueLength;
	final int triggerReloadQueueLength;
	final AdapterCallPersisterFactory persistence;
	final TransactionController ctrl;
	final Collection<String> adapterIds;
	final PriorityBlockingQueue<AdapterCall> queue;
	volatile boolean run = true;
	volatile boolean[] stopped = new boolean[]{false};
	final Object reloadLock = new Object();
	final Set<Thread> waitingThreads = new HashSet<Thread>();
	final ReloadThread reloadThread;
	final Batcher batcher;
	
	class ReloadThread extends Thread {
		
		{
			setDaemon(true);
		}

		long waitMillis;
		static final long MAX_WAIT_MILLIS = 2000;

		
		@Override
		public void run() {
			waitMillis = 1;
			loop: while (run) {
				synchronized (reloadLock) {
					try {
						waitMillis *= 4;
						if (waitMillis > MAX_WAIT_MILLIS)
							waitMillis = MAX_WAIT_MILLIS;
						reloadLock.wait(waitMillis);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						continue loop;
					}
				}
				if (run && queue.size() < triggerReloadQueueLength) {
					try {
						List<AdapterCall> newElements = ctrl.run(new DatabaseTransaction<List<AdapterCall>>() {
							@Override
							public List<AdapterCall> run(Connection con) throws Exception {
								Selector s = persistence.createSelector();
								return s.dequeue(con, adapterIds, transientQueueLength-queue.size());
							}
						});
						if (!newElements.isEmpty())
							waitMillis = 1;
						queue.addAll(
							newElements
						);
					} catch (Exception e) {
						logger.error("Dequeue error",e);
					}
				}
			}
			synchronized (stopped) {
				stopped[0] = true;
				stopped.notify();
			}
			synchronized (waitingThreads) {
				for (Thread t : waitingThreads) {
					t.interrupt();
				}
			}
		}
	}
	

	public NonTransactionalAdapterQueue(Collection<String> adapterIds, AdapterCallPersisterFactory persistence, int transientQueueLength, TransactionController ctrl, Batcher batcher) {
		this (adapterIds, persistence, transientQueueLength, transientQueueLength, ctrl, batcher);
	}
	
	public NonTransactionalAdapterQueue(Collection<String> adapterIds, AdapterCallPersisterFactory persistence, int transientQueueLength, int triggerReloadQueueLength, TransactionController ctrl, Batcher batcher) {
		this.transientQueueLength = transientQueueLength;
		this.triggerReloadQueueLength = triggerReloadQueueLength;
		this.persistence = persistence;
		this.adapterIds = new ArrayList<String>(adapterIds);
		this.ctrl = ctrl;
		this.batcher = batcher;
		this.queue = new PriorityBlockingQueue<AdapterCall>(transientQueueLength, new Comparator<AdapterCall>() {

			@Override
			public int compare(AdapterCall o1, AdapterCall o2) {
				if (o1.getPriority() < o2.getPriority())
					return -1;
				if (o1.getPriority() > o2.getPriority())
					return 1;
				return 0;
			}
			
		});
		this.reloadThread = new ReloadThread();
		this.reloadThread.start();
		this.triggerReload();
	}
	
	
	public AdapterCall dequeue() throws InterruptedException {
		synchronized (waitingThreads) {
			waitingThreads.add(Thread.currentThread());
		}
		AdapterCall c = null;
		try {
			while (run) {
				try {
					c = queue.take();
					break;
				} catch (InterruptedException ex) {
					if (isRunning()) /* interrupt from unknown origin */
						continue;
				}
			} 
			
			if (c == null) {
				c = queue.poll();
				if (c == null)
					throw new InterruptedException();
			}
		} finally {
			synchronized (waitingThreads) {
				waitingThreads.remove(Thread.currentThread());
			}
		}
		if (queue.size() < triggerReloadQueueLength && run)
			triggerReload();
		return c;
	}
	
	public void finished(AdapterCall c) {
		batcher.submitBatchCommand(persistence.createDeleteCommand(c));
	}
	
	protected boolean isRunning() {
		synchronized (stopped) {
			return !stopped[0];
		}
	}
	
	public void shutdown() {
		run = false;
		reloadThread.interrupt();
		try {
			reloadThread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void triggerReload() {
		synchronized (reloadLock) {
			reloadLock.notify();
		}
	}
	
	
	public static abstract class DefaultWorkerThread extends Thread {
		
		final NonTransactionalAdapterQueue queue;
		
		public DefaultWorkerThread(NonTransactionalAdapterQueue queue) {
			this.queue = queue;
		}
		
		@Override
		public void run() {
			while (queue.isRunning()) {
				try {
					AdapterCall c = queue.dequeue();
					try {
						handle(c);
					} finally {
						queue.finished(c);
					}
				} catch (InterruptedException e) {
				}
			}
			
		}

		protected abstract void handle(AdapterCall c);
		
	}

}
