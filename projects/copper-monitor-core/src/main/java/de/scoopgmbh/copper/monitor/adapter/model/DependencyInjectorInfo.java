package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;

public class DependencyInjectorInfo implements Serializable{
	private static final long serialVersionUID = -629782420394773711L;
	
	public static enum DependencyInjectorTyp{
		POJO,SPRING
	}
	DependencyInjectorTyp typ ;
	public DependencyInjectorInfo(DependencyInjectorTyp typ) {
		super();
		this.typ = typ;
	}
	public DependencyInjectorTyp getTyp() {
		return typ;
	}
	public void setTyp(DependencyInjectorTyp typ) {
		this.typ = typ;
	}
	
	
	
	
	
}
