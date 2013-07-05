package de.scoopgmbh.copper.monitoring.core.statistic;

public interface DoubleConverter<T> {
	public double getDouble(T value);
}