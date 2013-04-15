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
package de.scoopgmbh.copper.monitor.adapter.model;

import java.beans.ConstructorProperties;
import java.io.Serializable;

public class MeasurePointData implements Serializable {
	private static final long serialVersionUID = -2755509084700249664L;
	
	private final String measurePointId;
	private long elementCount = 0L;
	private long elapsedTimeMicros = 0L;
	private long count = 0L;
	
	@ConstructorProperties({"mpId", "elementCount", "elapsedTimeMicros", "count"})
	public MeasurePointData(String measurePointId, long elementCount, long elapsedTimeMicros, long count) {
		super();
		this.measurePointId = measurePointId;
		this.elementCount = elementCount;
		this.elapsedTimeMicros = elapsedTimeMicros;
		this.count = count;
	}

	
	public MeasurePointData(String measurePointId) {
		this.measurePointId = measurePointId;
	}
	void reset() {
		elementCount = 0L;
		elapsedTimeMicros = 0L;
		count = 0L;
	}
	public long getElementCount() {
		return elementCount;
	}
	public void setElementCount(long elementCount) {
		this.elementCount = elementCount;
	}
	public long getElapsedTimeMicros() {
		return elapsedTimeMicros;
	}
	public void setElapsedTimeMicros(long elapsedTimeMicros) {
		this.elapsedTimeMicros = elapsedTimeMicros;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public String getMeasurePointId() {
		return measurePointId;
	}
	
	public void update( long elementCount,long elapsedTimeMicros){
		this.elementCount+=elementCount;
		this.elapsedTimeMicros+=elapsedTimeMicros;
		this.count++;
		if (this.elapsedTimeMicros<0 || this.elementCount<0 || this.count<0){//long overflow
			reset();
		}
	}
	
	@Override
	public String toString() {
		final String DOTS = ".................................................1";
		long calcCount = 0L;

		calcCount = count > 0 ? count : 1;

		final long avgElementCount = elementCount/calcCount;
		final double avgTimePerElement = elementCount > 0 ? (double)elapsedTimeMicros/(double)elementCount/1000.0 : 0.0;
		final double avgTimePerExecution = count > 0 ? (double)elapsedTimeMicros/(double)calcCount/1000.0 : 0.0;
		return String.format("%1$55.55s #elements=%2$6d; avgCount=%3$6d; avgTime/Element=%4$12.5f msec; avgTime/Exec=%5$12.5f msec", measurePointId+DOTS, elementCount, avgElementCount, avgTimePerElement, avgTimePerExecution);
	}
	

}
