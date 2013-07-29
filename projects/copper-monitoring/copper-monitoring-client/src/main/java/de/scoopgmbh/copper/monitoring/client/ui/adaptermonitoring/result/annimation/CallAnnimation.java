package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.annimation;

import javafx.scene.paint.Color;


public class CallAnnimation extends EventAnnimationBase{

	public static final Color ADAPTER_CALL_COLOR = Color.CORAL;
	public int count=1;
	
	public CallAnnimation(AnnimationPartParameter animationPartBaseParameter) {
		super(ADAPTER_CALL_COLOR,animationPartBaseParameter);
	}
	
	@Override
	public String getDisplayText(){
		if (count>1){
			return id+" "+count+"x";
		}
		return id;
	}


}
