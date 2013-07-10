package de.scoopgmbh.copper.monitoring.client.form.filter.defaultfilter;

import javafx.beans.property.SimpleObjectProperty;

public class MaxCountFilterModel {
	public final SimpleObjectProperty<Integer> maxCount = new SimpleObjectProperty<Integer>(1000);
	
	public int getMaxCount(){
		return maxCount.get()==null?0:maxCount.get();
	}

}
