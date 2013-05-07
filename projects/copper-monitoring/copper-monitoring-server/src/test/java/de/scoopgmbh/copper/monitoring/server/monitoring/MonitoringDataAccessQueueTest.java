package de.scoopgmbh.copper.monitoring.server.monitoring;

import org.junit.Test;


public class MonitoringDataAccessQueueTest {
	private static final class Mock extends MonitoringDataAwareRunnable {
		boolean endless=true;
		@Override
		public void run() {
			while(endless){
				
			}
		}
	}

	@Test
	public void test_offer_to_full(){
		MonitoringDataAccessQueue monitoringEventQueue = new MonitoringDataAccessQueue(1, new MonitoringData());
		Mock runnable = new Mock();
		monitoringEventQueue.offer(runnable);
		
		monitoringEventQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				System.out.println(Math.random());
			}
		});
		
		//runnable.endless=false;
	}
}
