package de.scoopgmbh.copper.monitoring.core.statistic;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;


public class TimeframeGroupTest {
	
	@Test
	public void test_factory_method(){
		TimeframeGroup<Date,TimeValuePair<Long>> group = TimeframeGroup.<Date,TimeValuePair<Long>>createGroups(3, new Date(0), new Date(12), new CountAggregateFunction<Date>(), new DateConverter<Date>() {
			@Override
			public Date getDate(Date value) {
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
