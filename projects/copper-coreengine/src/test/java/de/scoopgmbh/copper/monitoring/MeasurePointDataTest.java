package de.scoopgmbh.copper.monitoring;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;


public class MeasurePointDataTest {
	
	@Test
	public void testLongOverflow(){
		MeasurePointData statSet = new MeasurePointData("42");
		statSet.setCount(Long.MAX_VALUE);
		statSet.setElementCount(Long.MAX_VALUE);
		statSet.setElapsedTimeMicros(Long.MAX_VALUE);
		
		statSet.update(10, 10);
		
		assertEquals(0, statSet.getCount());
		assertEquals(0, statSet.getElementCount());
		assertEquals(0, statSet.getElapsedTimeMicros());
	}

}
