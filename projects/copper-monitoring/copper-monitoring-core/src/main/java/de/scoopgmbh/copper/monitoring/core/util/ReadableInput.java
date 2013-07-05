package de.scoopgmbh.copper.monitoring.core.util;

import java.util.Date;

import com.esotericsoftware.kryo.io.Input;

public interface ReadableInput {

	Iterable<Input> read();
	
	Date getMinDate();
	
	Date getMaxDate();
	
}
