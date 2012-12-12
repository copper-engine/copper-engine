package de.scoopgmbh.copper.gui.form;

import javafx.scene.layout.BorderPane;

public class BorderPaneShowFormStrategie implements ShowFormStrategy<BorderPane> {
	public void showOn(BorderPane component, Form<BorderPane> form){
		component.setCenter(form.createContent());
	}
}