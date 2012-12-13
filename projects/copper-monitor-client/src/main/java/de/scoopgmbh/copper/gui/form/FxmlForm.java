package de.scoopgmbh.copper.gui.form;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import de.scoopgmbh.copper.gui.util.MessageProvider;


public class FxmlForm<C extends FxmlController> extends Form<C> {
	String fxmlPath;

	public FxmlForm(String menueItemtextKey, C controller, MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie) {
		super(menueItemtextKey, messageProvider, showFormStrategie,controller);
		this.fxmlPath = fxmlPath;
	}
	
	public FxmlForm(String menueItemtextKey, C controller, MessageProvider messageProvider) {
		super(menueItemtextKey, messageProvider, new NotShowFormStrategie(),controller);
		this.fxmlPath = fxmlPath;
	}

	@Override
	public Node createContent() {
		FXMLLoader fxmlLoader = new FXMLLoader(controller.getFxmlRessource());
		fxmlLoader.setController(controller);
		fxmlLoader.setResources(messageProvider.getBundle());
		try {
			return (Parent) fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	

	
	
}
