package de.scoopgmbh.copper.monitoring.core.statistic;

import java.util.Date;

import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;

public class SystemResourcesInfoDateConverter implements DateConverter<SystemResourcesInfo> {
	private static final long serialVersionUID = -7075396487547958290L;

	@Override
	public Date getDate(SystemResourcesInfo value) {
		return value.getTimestamp();
	}
}