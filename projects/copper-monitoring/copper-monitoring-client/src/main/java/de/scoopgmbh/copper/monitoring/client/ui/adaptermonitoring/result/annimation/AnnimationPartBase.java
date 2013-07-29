package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.annimation;

import javafx.scene.Node;

public abstract class AnnimationPartBase{
	public long startTime;
	public long endTime;
	public String id;
	
	public double startx;
	public double starty;
	public double endx;
	public double endy;

	public AnnimationPartBase(AnnimationPartParameter parameterObject) {
		super();
		this.startTime = parameterObject.startTime;
		this.endTime = parameterObject.endTime;
		this.id = parameterObject.id;
		this.startx = parameterObject.startx;
		this.starty = parameterObject.starty;
		this.endx = parameterObject.endx;
		this.endy = parameterObject.endy;
	}
	
	public abstract Node createVisualRepresentaion();
}