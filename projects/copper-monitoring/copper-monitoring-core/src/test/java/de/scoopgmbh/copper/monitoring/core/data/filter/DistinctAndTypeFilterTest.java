/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
