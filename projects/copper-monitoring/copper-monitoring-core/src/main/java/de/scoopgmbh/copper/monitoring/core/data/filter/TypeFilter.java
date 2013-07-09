package de.scoopgmbh.copper.monitoring.core.data.filter;

public class TypeFilter<T> extends MonitoringDataFilter<T>{
	private static final long serialVersionUID = -2939822094074942823L;

	public TypeFilter(Class<T> clazz) {
		super(clazz);
	}

	@Override
	protected boolean isValidImpl(T value) {
		return true;
	}

}
