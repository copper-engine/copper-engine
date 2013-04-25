package de.scoopgmbh.copper.monitoring.example.adapter;

import java.io.Serializable;
import java.math.BigDecimal;

public class BillableService implements Serializable{
	private static final long serialVersionUID = 2305002679023503444L;
	private BigDecimal amount;

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BillableService(BigDecimal amount) {
		super();
		this.amount = amount;
	}

	public BillableService() {
		super();
	}

	
	
}
