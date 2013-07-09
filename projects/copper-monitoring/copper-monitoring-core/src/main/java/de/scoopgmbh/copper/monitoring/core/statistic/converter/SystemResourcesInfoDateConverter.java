package de.scoopgmbh.copper.monitoring.core.statistic.converter;

import java.util.Date;

import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;

public class SystemResourcesInfoDateConverter implements TimeConverter<SystemResourcesInfo> {
	private static final long serialVersionUID = -7075396487547958290L;

	@Override
	public Date getTime(SystemResourcesInfo value) {
		return value.getTimestamp();
	}
}