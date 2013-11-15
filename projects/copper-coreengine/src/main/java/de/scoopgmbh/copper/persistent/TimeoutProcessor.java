package de.scoopgmbh.copper.persistent;

import java.sql.Timestamp;

class TimeoutProcessor {

	public static final Timestamp processTimout(Long internalProcessingTimout, long defaultStaleResponseRemovalTimeout) {
		final long ts = System.currentTimeMillis() + (internalProcessingTimout != null ? internalProcessingTimout : defaultStaleResponseRemovalTimeout);
		if (ts <= 0) {
			return new Timestamp(Long.MAX_VALUE);
		}		
		return new Timestamp(ts);
	}

}
