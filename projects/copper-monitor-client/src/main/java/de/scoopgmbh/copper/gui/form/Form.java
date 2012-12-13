package de.scoopgmbh.copper.gui.form;

import javafx.scene.Node;
import de.scoopgmbh.copper.gui.util.MessageProvider;

/**
 *
 * @param <T> target component to display the form
 */
public abstract class Form<C> {

	private final String menueItemtextKey;
	protected final MessageProvider messageProvider;
	private final ShowFormStrategy<?> showFormStrategie;
	protected final C controller;
	
	public Form(String menueItemtextKey, MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie, C controller) {
		super();
		this.menueItemtextKey = menueItemtextKey;
		this.messageProvider = messageProvider;
		this.showFormStrategie = showFormStrategie;
		this.controller = controller;
	}

	public String getTitle(){
		return messageProvider.getText(menueItemtextKey);
	}
	
	public void show(){
		showFormStrategie.show(this);
	}
	
	public abstract Node createContent();
	
	public C getController(){
		return controller;
	}
	
	
}
