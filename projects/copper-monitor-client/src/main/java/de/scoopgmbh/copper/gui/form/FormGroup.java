package de.scoopgmbh.copper.gui.form;
 
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
 
public class FormGroup {
    
	ArrayList<Form<?>> forms = new ArrayList<>();
	
	public FormGroup(List<Form<?>> forms){
		this.forms.addAll(forms);
	}
	
	public Menu createMenue(){
		final Menu fileMenu = new Menu("Window");
		for (final Form<?> form: forms){
			MenuItem menueItem = MenuItemBuilder
					.create()
					.text(form.getTitle())
					.onAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent e) {
							form.show();
						}
					}).build();
			fileMenu.getItems().add(menueItem);
		}
		return fileMenu;
	}
	
}