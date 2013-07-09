package de.scoopgmbh.copper.monitoring.core.statistic.converter;

import java.io.Serializable;

public interface DoubleConverter<T> extends Serializable  {
	public double getDouble(T value);
}