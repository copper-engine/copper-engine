package de.scoopgmbh.copper.monitoring.core.data.filter;

import java.io.Serializable;

public abstract class MonitoringDataFilter<T> implements Serializable {

	private static final long serialVersionUID = -5171173614183598375L;
	
	Class<T> clazz;
	public MonitoringDataFilter(Class<T> clazz) {
		super();
		this.clazz = clazz;
	}

	public boolean isValid(Object value){
		if (value!=null && value.getClass().isAssignableFrom(clazz)){
			return isValidImpl(clazz.cast(value));
		} else {
			return false;
		}
	}
	
	public T castValid(Object value){
		return clazz.cast(value);
	}
	
	protected abstract boolean isValidImpl(T value);

}
