package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;


public class WorkflowClassesInfo implements Serializable{
	private static final long serialVersionUID = -1189606285407748364L;
	
    String classname;
	String versionMajor;
	String versionMinor;
	public WorkflowClassesInfo(String classname, String versionMajor, String versionMinor) {
		super();
		this.classname = classname;
		this.versionMajor = versionMajor;
		this.versionMinor = versionMinor;
	}
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
	public String getVersionMajor() {
		return versionMajor;
	}
	public void setVersionMajor(String versionMajor) {
		this.versionMajor = versionMajor;
	}
	public String getVersionMinor() {
		return versionMinor;
	}
	public void setVersionMinor(String versionMinor) {
		this.versionMinor = versionMinor;
	}
	

	

	
}
