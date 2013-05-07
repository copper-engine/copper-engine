package de.scoopgmbh.copper.monitoring.example.adapter;

import java.util.Set;

public interface BillAdapter {

	public void publishBill(Bill bill);

	public Set<String> takeCorrelationIds();

}