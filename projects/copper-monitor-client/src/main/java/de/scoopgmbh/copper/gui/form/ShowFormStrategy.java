package de.scoopgmbh.copper.gui.form;

import javafx.scene.Node;

public interface ShowFormStrategy<E extends Node> {
	public void showOn(E component, Form<E> form);
}