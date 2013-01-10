package de.scoopgmbh.copper.gui.ui.settings;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SettingsModel {
	public ObservableList<AuditralColorMapping> auditralColorMappings = FXCollections.observableList(new ArrayList<AuditralColorMapping>());
	
}
