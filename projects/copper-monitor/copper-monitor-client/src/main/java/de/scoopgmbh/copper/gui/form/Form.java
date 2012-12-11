package de.scoopgmbh.copper.gui.form;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import de.scoopgmbh.copper.gui.util.MessageProvider;

public abstract class Form {

	private final TabPane mainTabPane;
	private final String menueItemtextKey;
	protected final MessageProvider messageProvider;
	
	public Form(TabPane mainTabPane, String menueItemtextKey, MessageProvider messageProvider) {
		super();
		this.mainTabPane = mainTabPane;
		this.menueItemtextKey = menueItemtextKey;
		this.messageProvider = messageProvider;
	}

	public String getMenueItemText(){
		return messageProvider.getText(menueItemtextKey);
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
