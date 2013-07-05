package de.scoopgmbh.copper.monitoring.core.statistic;

import java.util.Date;

public class TimeValuePair<T>{
	public final Date date;
	public final T value;
	
	public TimeValuePair(Date date, T value) {
		super();
		this.date = date;
		this.value = value;
	}
}