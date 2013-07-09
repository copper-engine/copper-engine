package de.scoopgmbh.copper.monitoring.core.statistic;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import de.scoopgmbh.copper.monitoring.core.statistic.converter.TimeConverter;


public class TimeframeGroupTest {
	
	@Test
	public void test_factory_method(){
		TimeframeGroup<Date,TimeValuePair<Double>> group = TimeframeGroup.<Date,TimeValuePair<Double>>createGroups(3, new Date(0), new Date(12), new CountAggregateFunction<Date>(), new TimeConverter<Date>() {
			private static final long serialVersionUID = -4045708565200297994L;

			@Override
			public Date getTime(Date value) {
				return value;
			}
		});
		assertEquals(0, group.from.getTime());
		assertEquals(4, group.to.getTime());
		
		
		group = group.nextGroup();
		assertEquals(4, group.from.getTime());
		assertEquals(8, group.to.getTime());
		
		
	}

}
