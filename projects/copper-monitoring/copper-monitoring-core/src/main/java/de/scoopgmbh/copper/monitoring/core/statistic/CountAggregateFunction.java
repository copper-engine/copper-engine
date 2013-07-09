package de.scoopgmbh.copper.monitoring.core.statistic;

import java.util.List;


public class CountAggregateFunction<T> implements AggregateFunction<T,TimeValuePair<Double>>{
	private static final long serialVersionUID = -714208820060517375L;

	public CountAggregateFunction() {
		super();
	}

	@Override
	public TimeValuePair<Double> doAggregate(List<T> value, TimeframeGroup<T,TimeValuePair<Double>> usedGroup) {
		return new TimeValuePair<Double>(usedGroup.to,(double) value.size());
	}

}