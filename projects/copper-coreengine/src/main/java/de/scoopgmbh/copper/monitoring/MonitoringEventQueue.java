package de.scoopgmbh.copper.monitoring;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *	provide thread save access to monitoring data by serialize the access with {@link java.util.concurrent.ArrayBlockingQueue}.
 *  Accessing monitoring data is not blocking so monitoring can't block copper core functionality.
 */
public class MonitoringEventQueue {

	public static final String IGNORE_WARN_TEXT = "could not process monitoring data. total ignored:";
	final ArrayBlockingQueue<Runnable> queue;
	AtomicLong ignored = new AtomicLong();
	private static final Logger logger = LoggerFactory.getLogger(MonitoringEventQueue.class);
	
	private final MonitoringData monitoringData; //only access over the monitoring queue

	public MonitoringEventQueue(){
		this(1000, new MonitoringData());
	}
	
	public MonitoringEventQueue(int queueCapacity, MonitoringData monitoringData){
		this.monitoringData = monitoringData;
		queue = new ArrayBlockingQueue<>(queueCapacity);
		new Thread("monitoringEventQueue") {
			{
				setDaemon(true);
			}
			public void run() {
				while (true) {
					work();
				}
			};
		}.start();
		
	}

	public boolean offer(MonitoringDataAwareRunnable runnable) {
		runnable.setMonitoringData(monitoringData);
		boolean result=queue.offer(runnable);
		if (!result) {
			logger.warn(IGNORE_WARN_TEXT+ignored.incrementAndGet());
		}
		return result;
	}
	
	/**@see java.util.concurrent.BlockingQueue#put
	 * @param runnable
	 */
	public void put(MonitoringDataAwareRunnable runnable) {
		runnable.setMonitoringData(monitoringData);
		try {
			queue.put(runnable);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public <T> T callAndWait(MonitoringDataAwareCallable<T> callable) {
		callable.setMonitoringData(monitoringData);
		try {
			FutureTask<T> futureTask = new FutureTask<T>(callable);
			queue.put(futureTask);
			try {
				return futureTask.get();
			} catch (ExecutionException e) {
				if (e.getCause() instanceof RuntimeException)
					throw (RuntimeException)e.getCause();
				throw new RuntimeException(e.getCause());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}
	
	ArrayList<Runnable> elements = new ArrayList<Runnable>(100);
	public void work() {
		try {
			elements.clear();
			elements.add(queue.take());
			queue.drainTo(elements,100);
			for (Runnable runnable : elements) {
				runnable.run();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	
}
