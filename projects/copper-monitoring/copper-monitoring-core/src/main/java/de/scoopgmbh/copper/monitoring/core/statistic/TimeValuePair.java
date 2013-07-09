package de.scoopgmbh.copper.monitoring.core.statistic;

import java.io.Serializable;
import java.util.Date;

public class TimeValuePair<T extends Serializable> implements Serializable{
	private static final long serialVersionUID = -7525940171969565828L;
	public final Date date;
	public final T value;
	
	public TimeValuePair(Date date, T value) {
		super();
		this.date = date;
		this.value = value;
	}
}