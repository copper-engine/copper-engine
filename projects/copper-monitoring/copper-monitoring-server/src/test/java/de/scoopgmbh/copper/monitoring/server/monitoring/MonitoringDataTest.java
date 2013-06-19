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
package de.scoopgmbh.copper.monitoring.server.monitoring;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringData.LimitedBuffer;


public class MonitoringDataTest {
	
	@Test
	public void test_imitedList(){
		LimitedBuffer<Object> limitedList = new LimitedBuffer<Object> (10);
		for (int i=0;i<20;i++){
			limitedList.addWitdhLimit(new Object());
		}
		assertEquals(10, limitedList.createList().size());
	}
	
	@Test
	public void test_less_added_than_limit(){
		LimitedBuffer<String> limitedList = new LimitedBuffer<String> (10);
		for (int i=0;i<3;i++){
			limitedList.addWitdhLimit(""+i);
		}
		assertEquals(3, limitedList.createList().size());
		assertEquals("0", limitedList.createList().get(0));
		assertEquals("1", limitedList.createList().get(1));
		assertEquals("2", limitedList.createList().get(2));
	}
	
	@Test
	public void test_more_added_than_limit(){
		LimitedBuffer<String> limitedList = new LimitedBuffer<String> (3);
		for (int i=0;i<5;i++){
			limitedList.addWitdhLimit(""+i);
		}
		assertEquals(3, limitedList.createList().size());
		assertEquals("2", limitedList.createList().get(0));
		assertEquals("3", limitedList.createList().get(1));
		assertEquals("4", limitedList.createList().get(2));
	}
	
	@Test
	public void test_clear(){
		LimitedBuffer<String> limitedList = new LimitedBuffer<String> (3);
		for (int i=0;i<2;i++){
			limitedList.addWitdhLimit(""+i);
		}
		limitedList.clear();
		assertEquals(0, limitedList.createList().size());
	}
	
	
	@Ignore
	@Test
	public void test_performanceBuffer(){
		ArrayList<Long> chronometries = new ArrayList<Long>();
		
		for (int u=0;u<100;u++){
			long start = System.currentTimeMillis();
			LimitedBuffer<Object> limitedList = new LimitedBuffer<Object> (1000);
			for (int i=0;i<2000000;i++){
				limitedList.addWitdhLimit(new Object());
			}
			chronometries.add(System.currentTimeMillis()-start);
		}
		
		long sum=0;
		for (long chronometry: chronometries){
			sum+=chronometry;
		}
		System.out.println(sum/chronometries.size());
			
//		assertEquals(10, limitedList.getList().size());
	}
	

}
