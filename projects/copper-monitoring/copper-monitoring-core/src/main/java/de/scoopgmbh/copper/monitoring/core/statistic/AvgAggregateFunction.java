package de.scoopgmbh.copper.monitoring.core.statistic;

import java.util.List;

import de.scoopgmbh.copper.monitoring.core.statistic.converter.DoubleConverter;


public class AvgAggregateFunction<T> implements AggregateFunction<T,TimeValuePair<Double>>{
	private static final long serialVersionUID = 4882013677988826331L;
	
	private final DoubleConverter<T> doubleConverter;
	
	public AvgAggregateFunction(DoubleConverter<T> doubleConverter) {
		super();
		this.doubleConverter = doubleConverter;
	}

	@Override
	public TimeValuePair<Double> doAggregate(List<T> group, TimeframeGroup<T,TimeValuePair<Double>> usedGroup) {
		double sum=0;
		for (T value: group){
			sum += doubleConverter.getDouble(value);
		}
		int size= group.size();
		if (size==0){
			size=1;
		}
		return new TimeValuePair<Double>(usedGroup.from,sum/size);
	}
	
}