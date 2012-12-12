package de.scoopgmbh.copper.gui.form;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class TabPaneShowFormStrategie implements ShowFormStrategy<TabPane> {
	public void showOn(TabPane component, Form<TabPane> form){
		Tab tab = new Tab();
		tab.setText("new tab");
		tab.setContent(form.createContent());
		tab.setText(form.getMenueItemText());
		component.getTabs().add(tab);
	}
}