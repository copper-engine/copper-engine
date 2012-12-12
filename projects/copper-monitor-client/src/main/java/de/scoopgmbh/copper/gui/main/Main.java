package de.scoopgmbh.copper.gui.main;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import de.scoopgmbh.copper.gui.factory.MainFactory;

public class Main extends Application {

	@Override
	public void start(final Stage primaryStage) { //Stage = window
		
		primaryStage.setTitle("Copper gui");
		final Group rootGroup = new Group();
		final Scene scene = new Scene(rootGroup, 1024, 768, Color.WHEAT);

		
		MainFactory mainFactory = new MainFactory(primaryStage);
	
		

		mainFactory.getMainPane().prefHeightProperty().bind(scene.heightProperty());
		mainFactory.getMainPane().prefWidthProperty().bind(scene.widthProperty());
		rootGroup.getChildren().add(mainFactory.getMainPane());
		
		primaryStage.setScene(scene);
		primaryStage.show();
		
		
		//getParameters().
		if (true){// TODO only if default parameter not passed
			mainFactory.createLoginForm().show();
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		
	}

	public static void main(final String[] arguments) {
		Application.launch(arguments);
	}
}