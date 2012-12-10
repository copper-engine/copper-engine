package de.scoopgmbh.copper.gui.form;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import de.scoopgmbh.copper.gui.util.Message;

public abstract class Form {

	private final TabPane mainTabPane;
	private final String menueItemtextKey;
	
	public Form(TabPane mainTabPane, String menueItemtextKey) {
		super();
		this.mainTabPane = mainTabPane;
		this.menueItemtextKey = menueItemtextKey;
	}

	public String getMenueItemText(){
		return Message.getText(menueItemtextKey);
	}
	
	public void showInTab(){
		Tab tab = new Tab();
		tab.setText("new tab");
		tab.setContent(createContent());
		tab.setText(getMenueItemText());
		mainTabPane.getTabs().add(tab);
	}
	
	public abstract Node createContent();
	
}
