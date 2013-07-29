package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.annimation;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;

public class AdapterAnnimation extends AnnimationPartBase{
	
	public static final int ADAPTER_HEIGHT = 100;
	public static final int ADAPTER_WIDTH = 150;
	public static final Color ADAPTER_COLOR = Color.LIGHTGREEN;
	public AdapterAnnimation(AnnimationPartParameter animationPartBaseParameter) {
		super(animationPartBaseParameter);
	}

	@Override
	public Node createVisualRepresentaion() {
		Pane pane = new Pane();
		final Rectangle adapterRectangle = new Rectangle(ADAPTER_WIDTH,ADAPTER_HEIGHT);
		adapterRectangle.setFill(ADAPTER_COLOR);
		adapterRectangle.setArcHeight(25);
		adapterRectangle.setArcWidth(25);
		final Text adapterText = new Text(id);
		adapterText.setFontSmoothingType(FontSmoothingType.LCD);
		adapterText.xProperty().bind(adapterRectangle.xProperty().add(adapterRectangle.getWidth()/2).subtract(adapterText.getBoundsInLocal().getWidth()/2));
		adapterText.yProperty().bind(adapterRectangle.yProperty().subtract(5));
		pane.getChildren().add(adapterRectangle);
		pane.getChildren().add(adapterText);
		return pane;
	}

}
