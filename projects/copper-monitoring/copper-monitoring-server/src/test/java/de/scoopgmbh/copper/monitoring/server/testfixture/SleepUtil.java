package de.scoopgmbh.copper.monitoring.server.testfixture;

public class SleepUtil {
	public static void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
