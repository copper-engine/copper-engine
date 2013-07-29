package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.annimation;

import javafx.scene.paint.Color;


public class NotifyAnnimation extends EventAnnimationBase{
	
	public static final Color ADAPTER_NOTIFY_COLOR = Color.CORNFLOWERBLUE;

	public NotifyAnnimation(AnnimationPartParameter animationPartBaseParameter) {
		super(ADAPTER_NOTIFY_COLOR,animationPartBaseParameter);
	}

}
