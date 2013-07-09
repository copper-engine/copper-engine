package de.scoopgmbh.copper.monitoring.core.statistic;



import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import de.scoopgmbh.copper.monitoring.core.statistic.converter.DoubleConverter;


public class QuantilAggregateFunctionTest {
	
	@Test
	public void test_emptygroup(){
		QuantilAggregateFunction<Double> quantil = new QuantilAggregateFunction<Double>(0.5,new DoubleConverter<Double>() {
			private static final long serialVersionUID = 1L;
			@Override
			public double getDouble(Double value) {
				return value;
			}
		});
		
		final ArrayList<Double> group = new ArrayList<Double>();
		assertEquals(0, quantil.doAggregate(group, new TimeframeGroup<Double,TimeValuePair<Double>>(null, new Date(), new Date(), null)).value,0.0001);
	}
	
	@Test
	public void test_50(){
		QuantilAggregateFunction<Double> quantil = new QuantilAggregateFunction<Double>(0.5,new DoubleConverter<Double>() {
			private static final long serialVersionUID = 1L;
			@Override
			public double getDouble(Double value) {
				return value;
			}
		});
		
		final ArrayList<Double> group = new ArrayList<Double>();
		group.add(5d);
		group.add(9d);
		group.add(4d);
		group.add(1d);
	
		assertEquals(5, quantil.doAggregate(group, new TimeframeGroup<Double,TimeValuePair<Double>>(null, new Date(), new Date(), null)).value,0.0001);
	}
	
	@Test
	public void test_99(){
		QuantilAggregateFunction<Double> quantil = new QuantilAggregateFunction<Double>(0.99,new DoubleConverter<Double>() {
			private static final long serialVersionUID = 1L;
			@Override
			public double getDouble(Double value) {
				return value;
			}
		});
		
		final ArrayList<Double> group = new ArrayList<Double>();
		group.add(5d);
		group.add(9d);
		group.add(4d);
		group.add(1d);
	
		assertEquals(9, quantil.doAggregate(group, new TimeframeGroup<Double,TimeValuePair<Double>>(null, new Date(), new Date(), null)).value,0.0001);
	}

}
