package de.scoopgmbh.copper.monitoring.core.statistic;

import java.io.Serializable;
import java.util.Date;

public interface DateConverter<T> extends Serializable{
	public Date getDate(T value);
}
