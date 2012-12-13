package de.scoopgmbh.copper.gui.form;

import javafx.scene.layout.BorderPane;

public class NotShowFormStrategie extends ShowFormStrategy<BorderPane> {
	public NotShowFormStrategie() {
		super(null);
	}

	public void show(Form<?> form){
		throw new IllegalStateException("no way defined to show the Form");
	}
}