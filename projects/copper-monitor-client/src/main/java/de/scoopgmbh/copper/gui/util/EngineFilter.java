package de.scoopgmbh.copper.gui.util;

import javafx.beans.property.SimpleObjectProperty;
import de.scoopgmbh.copper.monitor.adapter.model.EngineDiscriptor;

public class EngineFilter {
	
	public final SimpleObjectProperty<EngineDiscriptor> engine = new SimpleObjectProperty<>();

	public EngineFilter(){
		
	}
	
	public EngineFilter(EngineDiscriptor engine){
		this.engine.setValue(engine);
	}
}
