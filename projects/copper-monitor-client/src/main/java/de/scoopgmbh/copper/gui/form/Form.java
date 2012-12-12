package de.scoopgmbh.copper.gui.form;

import javafx.scene.Node;
import de.scoopgmbh.copper.gui.util.MessageProvider;

/**
 *
 * @param <T> target component to display the form
 */
public abstract class Form<T extends Node> {

	private final T displayTarget;
	private final String menueItemtextKey;
	protected final MessageProvider messageProvider;
	private final ShowFormStrategy<T> showFormStrategie;
	
	public Form(T displayTarget, String menueItemtextKey, MessageProvider messageProvider, ShowFormStrategy<T> showFormStrategie) {
		super();
		this.displayTarget = displayTarget;
		this.menueItemtextKey = menueItemtextKey;
		this.messageProvider = messageProvider;
		this.showFormStrategie = showFormStrategie;
	}

	public String getMenueItemText(){
		return messageProvider.getText(menueItemtextKey);
	}
	
	public void show(){
		showFormStrategie.showOn(displayTarget,this);
	}
	
	public abstract Node createContent();
	
}
