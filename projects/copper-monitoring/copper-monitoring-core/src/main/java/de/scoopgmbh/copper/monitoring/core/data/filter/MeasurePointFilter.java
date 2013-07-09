package de.scoopgmbh.copper.monitoring.core.data.filter;

import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;

public class MeasurePointFilter extends MonitoringDataFilter<MeasurePointData>{
	private static final long serialVersionUID = -5872080135025414079L;
	
	public String measurePointId;
	
	public MeasurePointFilter(String measurePointId) {
		super(MeasurePointData.class);
		assert measurePointId!=null ;
		this.measurePointId = measurePointId;
	}

	@Override
	protected boolean isValidImpl(MeasurePointData value) {
		return measurePointId.equals(value.getMeasurePointId());
	}

}
