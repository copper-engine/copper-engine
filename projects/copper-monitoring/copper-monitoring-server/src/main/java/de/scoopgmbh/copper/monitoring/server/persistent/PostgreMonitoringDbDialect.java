package de.scoopgmbh.copper.monitoring.server.persistent;


public class PostgreMonitoringDbDialect extends BaseDatabaseMonitoringDialect {
	
	@Override
	public String getResultLimitingQuery(String query, long limit) {
		return query+ " LIMIT "+limit;
	}

}