package de.scoopgmbh.copper.monitoring.core.statistic;

import java.io.Serializable;
import java.util.List;

/**
 * @param <T> list typ
 * @param <R> aggreagte typ
 * @param <G> used group 
 */
public interface AggregateFunction<T,R> extends Serializable{
	R doAggregate(List<T> group, TimeframeGroup<T,R> usedGroup);
}