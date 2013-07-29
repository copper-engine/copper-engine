package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.annimation;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;

public class EventAnnimationBase extends AnnimationPartBase{
	
	public EventAnnimationBase(Color color, AnnimationPartParameter animationPartBaseParameter) {
		super(animationPartBaseParameter);
		this.color = color;
	}

	public static final int EVENT_HEIGHT = 35;
	public static final int EVENT_WIDTH = 110;
	Color color;

	@Override
	public Node createVisualRepresentaion() {
		Pane pane = new Pane();
		final Rectangle rectangle = new Rectangle(EVENT_WIDTH,EVENT_HEIGHT);
		rectangle.setFill(color);
		rectangle.setArcHeight(20);
		rectangle.setArcWidth(20);
		final Text text = new Text(getDisplayText());
		text.setFontSmoothingType(FontSmoothingType.LCD);
		text.translateXProperty().bind(rectangle.translateXProperty());
		text.translateYProperty().bind(rectangle.translateYProperty().add(rectangle.getHeight()/2).add(text.getBoundsInLocal().getHeight()/4));
		pane.getChildren().add(rectangle);
		pane.getChildren().add(text);
		return pane;
	}
	
	
	protected String getDisplayText(){
		return id;
	}
	



}
