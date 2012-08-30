package de.scoopgmbh.copper.sample.datamodel;

import java.io.Serializable;

public class CustomerData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String contractNumber;
	private String msisdn;
	
	public String getContractNumber() {
		return contractNumber;
	}
	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}
	
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	
	
}
