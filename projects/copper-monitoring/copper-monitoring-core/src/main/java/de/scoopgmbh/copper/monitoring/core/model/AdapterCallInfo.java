package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;

public class AdapterCallInfo extends AdapterEventBase implements Serializable{
	private static final long serialVersionUID = 6386090675365126626L;
	
	String method;
	String parameter;
	
	public AdapterCallInfo() {
		super();
	}
	public AdapterCallInfo(String method, String parameter, Date timestamp, String adapterName) {
		super(adapterName,timestamp);
		this.method = method;
		this.parameter = parameter;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

}
