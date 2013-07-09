package de.scoopgmbh.copper.monitoring.core.statistic;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.scoopgmbh.copper.monitoring.core.statistic.converter.DoubleConverter;


public class QuantilAggregateFunction<T> implements AggregateFunction<T,TimeValuePair<Double>>{
	private static final long serialVersionUID = -714208820060517375L;
	private DoubleConverter<T> doubleConverter;
	private double quantil;
	/**0 bis 1 e.g 0.5 median
	 * @param quantil
	 */
	public QuantilAggregateFunction(double quantil, DoubleConverter<T> doubleConverter) {
		super();
		this.doubleConverter = doubleConverter;
		this.quantil = quantil;
	}

	@Override
	public TimeValuePair<Double> doAggregate(List<T> groupContent, TimeframeGroup<T,TimeValuePair<Double>> usedGroup) {
		if (groupContent.isEmpty()){
			return new TimeValuePair<Double>(usedGroup.to,0d);
		}
		Collections.sort(groupContent, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return Double.compare(doubleConverter.getDouble(o1),doubleConverter.getDouble(o2));
			}
		});
		double result = doubleConverter.getDouble(groupContent.get((int) (groupContent.size()*quantil)));
		return new TimeValuePair<Double>(usedGroup.to,result);
	}

}