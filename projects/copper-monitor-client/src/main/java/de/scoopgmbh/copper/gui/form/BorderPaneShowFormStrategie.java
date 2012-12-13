package de.scoopgmbh.copper.gui.form;

import javafx.scene.layout.BorderPane;

public class BorderPaneShowFormStrategie extends ShowFormStrategy<BorderPane> {
	public BorderPaneShowFormStrategie(BorderPane component) {
		super(component);
	}

	public void show(Form<?> form){
		component.setCenter(form.createContent());
	}
}