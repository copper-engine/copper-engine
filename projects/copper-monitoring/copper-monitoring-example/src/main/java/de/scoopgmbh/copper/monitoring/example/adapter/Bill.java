package de.scoopgmbh.copper.monitoring.example.adapter;

import java.io.Serializable;
import java.math.BigDecimal;

public class Bill implements Serializable{
	private static final long serialVersionUID = -8189909245649542035L;
	BigDecimal totalAmount;

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Bill(BigDecimal totalAmount) {
		super();
		this.totalAmount = totalAmount;
	}

	public Bill() {
		super();
	}
	
}
