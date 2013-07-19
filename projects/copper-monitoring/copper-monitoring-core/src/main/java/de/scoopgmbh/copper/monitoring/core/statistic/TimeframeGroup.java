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
package de.scoopgmbh.copper.monitoring.core.statistic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import de.scoopgmbh.copper.monitoring.core.statistic.converter.TimeConverter;

public class TimeframeGroup<T,R> implements Serializable{
	private static final long serialVersionUID = -4529367917791924193L;
	
	final Date from; 
	final Date to;
	private final TimeConverter<T> dateConverter;
	AggregateFunction<T, R> aggregateFunction;
	
	public TimeframeGroup(AggregateFunction<T, R> aggregateFunction, Date from, Date to, TimeConverter<T> dateConverter) {
		this.aggregateFunction = aggregateFunction;
		this.from = from;
		this.to = to;
		this.dateConverter = dateConverter;
	}
	

	ArrayList<T> group = new ArrayList<T>();
	public void addToGroup(T value){
		group.add(value);
	}
	
	public R getAggregate(){
		return aggreagte;
	}
	
	public boolean isAggregated(){
		return aggreagte!=null;
	}
	
	R aggreagte;
	public void doAggregateAndSaveResult(){
		aggreagte = aggregateFunction.doAggregate(group,this);
	}
	
	public void clear(){
		group.clear();
	}
	
	
	public TimeframeGroup<T,R> nextGroup(){
		return nextGroup(aggregateFunction);
	}

	public boolean isInGroup(T listvalue) {
		return dateConverter.getTime(listvalue).getTime()>=from.getTime() && dateConverter.getTime(listvalue).getTime()<to.getTime();
	}
	
	public static <T,R> TimeframeGroup<T,R> createGroups(int groupCount, Date from, Date to,
			AggregateFunction<T, R> aggregateFunction, TimeConverter<T> dateConverter){
		long delta = to.getTime()-from.getTime();
		if (delta<=0){
			throw new IllegalArgumentException();
		}
		long timeframe = delta/groupCount;
		
		Date frameFrom=new Date(from.getTime());
		Date frameTo=new Date(from.getTime()+timeframe);
		return new TimeframeGroup<T,R>(aggregateFunction,frameFrom,frameTo,dateConverter);
	}

	protected TimeframeGroup<T,R>  nextGroup(AggregateFunction<T, R> aggregateFunction) {
		long timeframe = to.getTime()-from.getTime();
		return new TimeframeGroup<T, R>((AggregateFunction<T, R>)aggregateFunction, to, new Date(to.getTime()+timeframe), dateConverter);
	}

	public Date getFrom() {
		return from;
	}

	public Date getTo() {
		return to;
	}



	
}
