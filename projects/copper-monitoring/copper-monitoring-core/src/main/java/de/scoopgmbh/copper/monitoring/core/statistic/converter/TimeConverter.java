package de.scoopgmbh.copper.monitoring.core.statistic.converter;

import java.io.Serializable;
import java.util.Date;

public interface TimeConverter<T> extends Serializable{
	public Date getTime(T value);
}
