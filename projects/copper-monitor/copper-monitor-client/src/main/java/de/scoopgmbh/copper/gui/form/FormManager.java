package de.scoopgmbh.copper.gui.form;
 
import java.util.ArrayList;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
 
public class FormManager {
    
	ArrayList<Form> forms = new ArrayList<>();
	
	public FormManager(Form... forms){
		this.forms.addAll(Arrays.asList(forms));
	}
	
	public Menu createMenue(){
		final Menu fileMenu = new Menu("Window");
		for (final Form form: forms){
			MenuItem menueItem = MenuItemBuilder
					.create()
					.text(form.getMenueItemText())
					.onAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent e) {
							form.showInTab();
						}
					}).build();
			fileMenu.getItems().add(menueItem);
		}
		return fileMenu;
	}
	
}