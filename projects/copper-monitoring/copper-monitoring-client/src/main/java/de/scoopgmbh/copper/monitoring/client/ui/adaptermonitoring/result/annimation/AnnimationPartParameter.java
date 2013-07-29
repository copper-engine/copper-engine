package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.annimation;


public class AnnimationPartParameter {
	public long startTime;
	public long endTime;
	public String id;
	public double startx;
	public double starty;
	public double endx;
	public double endy;

	public AnnimationPartParameter(long startTime, long endTime, String id, double startx, double starty, double endx,
			double endy) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.id = id;
		this.startx = startx;
		this.starty = starty;
		this.endx = endx;
		this.endy = endy;
	}
}