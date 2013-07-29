/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.annimation;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;

public class WorkflowAnnimation extends AnnimationPartBase{
	
	public static final Color WORKFLOW_COLOR = Color.GOLD;
	public static final int WIDTH = EventAnnimationBase.EVENT_WIDTH+20;
	String workflowClass;

	public WorkflowAnnimation(String workflowClass, AnnimationPartParameter animationPartBaseParameter) {
		super(animationPartBaseParameter);
		this.workflowClass = workflowClass;
	}

	@Override
	public Node createVisualRepresentaion() {
		Pane pane = new Pane();
		final Rectangle workflowRectangle = new Rectangle(WIDTH+20,EventAnnimationBase.EVENT_HEIGHT+15);
		workflowRectangle.setFill(WORKFLOW_COLOR);
		workflowRectangle.setArcHeight(25);
		workflowRectangle.setArcWidth(25);
		final Text classText = new Text(workflowClass);
		classText.setFontSmoothingType(FontSmoothingType.LCD);
		classText.xProperty().bind(workflowRectangle.xProperty().add(workflowRectangle.getWidth()/2).subtract(classText.getBoundsInLocal().getWidth()/2));
		classText.yProperty().bind(workflowRectangle.yProperty().subtract(18));
		final Text instanceIdText = new Text(id);
		instanceIdText.setFontSmoothingType(FontSmoothingType.LCD);
		instanceIdText.xProperty().bind(workflowRectangle.xProperty().add(workflowRectangle.getWidth()/2).subtract(classText.getBoundsInLocal().getWidth()/2));
		instanceIdText.yProperty().bind(workflowRectangle.yProperty().subtract(3));
		pane.getChildren().add(workflowRectangle);
		pane.getChildren().add(classText);
		pane.getChildren().add(instanceIdText);
		return pane;
	}

}
