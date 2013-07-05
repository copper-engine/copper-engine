package de.scoopgmbh.copper.monitoring.core.statistic;

import java.util.List;


public class CountAggregateFunction<T> implements AggregateFunction<T,TimeValuePair<Long>>{
	private static final long serialVersionUID = -714208820060517375L;

	public CountAggregateFunction() {
		super();
	}

	@Override
	public TimeValuePair<Long> doAggregate(List<T> value, TimeframeGroup<T,TimeValuePair<Long>> usedGroup) {
		return new TimeValuePair<Long>(usedGroup.to,(long) value.size());
	}

}