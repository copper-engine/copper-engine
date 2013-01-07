package de.scoopgmbh.copper.gui.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import de.scoopgmbh.copper.gui.context.ApplicationContext;

public class ClientMain extends Application {

	@Override
	public void start(final Stage primaryStage) { //Stage = window
		
		primaryStage.setTitle("Copper Monitor");
		ApplicationContext mainFactory = new ApplicationContext();
		final Scene scene = new Scene(mainFactory.getMainPane(), 1200, 768, Color.WHEAT);

		
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