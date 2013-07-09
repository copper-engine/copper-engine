package de.scoopgmbh.copper.monitoring.core.statistic.converter;

import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;

public class SystemResMeasurePointDataDoubleConverter implements DoubleConverter<MeasurePointData> {
	private static final long serialVersionUID = -3563836946403921134L;

	@Override
	public double getDouble(MeasurePointData value) {
		return value.getSystemCpuLoad();
	}

}