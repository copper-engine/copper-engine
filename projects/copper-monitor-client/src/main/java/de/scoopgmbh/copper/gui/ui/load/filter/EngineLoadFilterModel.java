package de.scoopgmbh.copper.gui.ui.load.filter;

import javafx.beans.property.SimpleBooleanProperty;

public class EngineLoadFilterModel {
	public SimpleBooleanProperty showRunning = new SimpleBooleanProperty(true);
	public SimpleBooleanProperty showWaiting = new SimpleBooleanProperty(true);
}
