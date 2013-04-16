package de.scoopgmbh.copper.gui.form.enginefilter;

public class EngineFilterModelBase implements EngineFilterModel {

	public final EnginePoolModel enginePoolModel = new EnginePoolModel();
	@Override
	public EnginePoolModel getEngineFilterModel() {
		return enginePoolModel;
	}

}
