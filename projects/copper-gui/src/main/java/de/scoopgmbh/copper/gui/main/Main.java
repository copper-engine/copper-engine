package de.scoopgmbh.copper.gui.main;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import de.scoopgmbh.copper.gui.factory.MainFactory;

public class Main extends Application {

	@Override
	public void start(final Stage primaryStage) { //Stage = window
		
		MainFactory mainFactory = new MainFactory();
		
		
		
		primaryStage.setTitle("Copper gui");
		final Group rootGroup = new Group();
		final Scene scene = new Scene(rootGroup, 1024, 768, Color.WHEAT);
		
		final MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(mainFactory.getFormManager().createMenue());
		menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
		

		
		BorderPane mainPane = new BorderPane();
		mainPane.setCenter(mainFactory.getMainTabPane());
		mainPane.setTop(menuBar);
		mainPane.prefHeightProperty().bind(scene.heightProperty());
		mainPane.prefWidthProperty().bind(scene.widthProperty());
		rootGroup.getChildren().add(mainPane);
		
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(final String[] arguments) {
		Application.launch(arguments);
	}
}