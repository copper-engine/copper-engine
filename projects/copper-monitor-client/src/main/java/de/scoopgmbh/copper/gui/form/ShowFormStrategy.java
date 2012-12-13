package de.scoopgmbh.copper.gui.form;

import javafx.scene.Node;

public abstract class ShowFormStrategy<E extends Node> {
	protected E component;
	public ShowFormStrategy(E component){
		this.component=component;
	}
	public abstract void show(Form<?> form);
}