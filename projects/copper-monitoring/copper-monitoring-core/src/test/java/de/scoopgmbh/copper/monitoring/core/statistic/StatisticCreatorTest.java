package de.scoopgmbh.copper.monitoring.core.statistic;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.Test;


public class StatisticCreatorTest {
	
	private class Pair extends TimeValuePair<Double>{
		public Pair(Date date, Double value) {
			super(date, value);
		}
	}
	
	@Test
	public void test_group_avg(){
		
		final DoubleConverter<TimeValuePair<Double>> doubleConverter = new DoubleConverter<TimeValuePair<Double>>() {
			@Override
			public double getDouble(TimeValuePair<Double> value) {
				return value.value;
			}
		};
		
		
		AvgAggregateFunction<TimeValuePair<Double>> avg = new AvgAggregateFunction<TimeValuePair<Double>>(doubleConverter);
		final DateConverter<TimeValuePair<Double>> dateConverter = new DateConverter<TimeValuePair<Double>>() {
			@Override
			public Date getDate(TimeValuePair<Double> value) {
				return value.date;
			}
		};
		
		TimeframeGroup<TimeValuePair<Double>,TimeValuePair<Double>> group = new TimeframeGroup<TimeValuePair<Double>,TimeValuePair<Double>>(avg,new Date(0),new Date(5),dateConverter);
		
		StatisticCreator<TimeValuePair<Double>,TimeValuePair<Double>> statisticCreator = new StatisticCreator<TimeValuePair<Double>,TimeValuePair<Double>>(group);
		
		statisticCreator.add(new Pair(new Date(0),2.0));
		statisticCreator.add(new Pair(new Date(0),2.0));
		statisticCreator.add(new Pair(new Date(5),6.0));
		statisticCreator.add(new Pair(new Date(5),6.0));
		
		List<TimeValuePair<Double>> result = statisticCreator.getAggregatedResult();
		assertEquals(2, result.size());
		
		assertEquals(2.0, result.get(0).value, 0.000001);
		assertEquals(6.0, result.get(1).value, 0.000001);
	}
	
	@Test
	public void test_group_count(){
		CountAggregateFunction<Pair> count = new CountAggregateFunction<Pair>();
		TimeframeGroup<Pair,TimeValuePair<Long>> group = new TimeframeGroup<Pair,TimeValuePair<Long>>(count,new Date(0),new Date(5),new DateConverter<Pair>() {
			@Override
			public Date getDate(Pair value) {
				return value.date;
			}
		});

		StatisticCreator<Pair,TimeValuePair<Long>> statisticCreator = new StatisticCreator<Pair,TimeValuePair<Long>>(group);
		
		statisticCreator.add(new Pair(new Date(0),2.0));
		statisticCreator.add(new Pair(new Date(0),2.0));
		statisticCreator.add(new Pair(new Date(5),6.0));
		statisticCreator.add(new Pair(new Date(5),6.0));
		statisticCreator.add(new Pair(new Date(5),6.0));
		
		List<TimeValuePair<Long>> result = statisticCreator.getAggregatedResult();
		assertEquals(2, result.size());
		
		assertEquals(2l, result.get(0).value.longValue());
		assertEquals(3l, result.get(1).value.longValue());
	}
	
	@Test
	public void test_empty_groups(){
		CountAggregateFunction<Pair> count = new CountAggregateFunction<Pair>();
		TimeframeGroup<Pair,TimeValuePair<Long>> group = new TimeframeGroup<Pair,TimeValuePair<Long>>(count,new Date(0),new Date(5),new DateConverter<Pair>() {
			@Override
			public Date getDate(Pair value) {
				return value.date;
			}
		});


		StatisticCreator<Pair,TimeValuePair<Long>> statisticCreator = new StatisticCreator<Pair,TimeValuePair<Long>>(group);
		
		statisticCreator.add(new Pair(new Date(0),2.0));
		//5-10 empty
		//10-15 empty
		//15-20 empty
		statisticCreator.add(new Pair(new Date(20),6.0));
		statisticCreator.add(new Pair(new Date(20),6.0));
		
		List<TimeValuePair<Long>> result = statisticCreator.getAggregatedResult();
		assertEquals(5, result.size());
		
		assertEquals(1l, result.get(0).value.longValue());
		assertEquals(0l, result.get(1).value.longValue());
		assertEquals(0l, result.get(2).value.longValue());
		assertEquals(0l, result.get(3).value.longValue());
		assertEquals(2l, result.get(4).value.longValue());
	}

}
