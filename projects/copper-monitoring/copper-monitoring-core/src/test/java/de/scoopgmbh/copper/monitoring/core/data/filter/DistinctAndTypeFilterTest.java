package de.scoopgmbh.copper.monitoring.core.data.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;


public class DistinctAndTypeFilterTest {
	
	@Test
	public void test(){
		DistinctAndTypeFilter<MeasurePointData> distinctAndTypeFilter = 
				new DistinctAndTypeFilter<MeasurePointData>(MeasurePointData.class,new MeasurePointComperator());
		
		assertTrue(distinctAndTypeFilter.isValid(createDummy("1")));
		assertTrue(distinctAndTypeFilter.isValid(createDummy("2")));
		assertTrue(distinctAndTypeFilter.isValid(createDummy("3")));
		assertFalse(distinctAndTypeFilter.isValid(createDummy("1")));
		assertFalse(distinctAndTypeFilter.isValid(createDummy("2")));
		assertFalse(distinctAndTypeFilter.isValid(createDummy("3")));
	}
	
	private MeasurePointData createDummy(String id){
		final MeasurePointData measurePointData = new MeasurePointData(id);
		return measurePointData;
	}

}
