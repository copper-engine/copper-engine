package de.scoopgmbh.copper.gui.form;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class TabPaneShowFormStrategie extends ShowFormStrategy<TabPane> {

	public TabPaneShowFormStrategie(TabPane component) {
		super(component);
	}

	public void show(Form<?> form){
		Tab tab = new Tab();
		tab.setText("new tab");
		tab.setContent(form.createContent());
		tab.setText(form.getTitle());
		component.getTabs().add(tab);
		component.getSelectionModel().select(tab);
	}
}