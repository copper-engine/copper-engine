package de.scoopgmbh.copper.monitoring.core.statistic.converter;

import java.util.Date;

import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;

public class MeasurePointDataDateConverter implements TimeConverter<MeasurePointData> {
	private static final long serialVersionUID = -7075396487547958290L;

	@Override
	public Date getTime(MeasurePointData value) {
		return value.getTime();
	}
}