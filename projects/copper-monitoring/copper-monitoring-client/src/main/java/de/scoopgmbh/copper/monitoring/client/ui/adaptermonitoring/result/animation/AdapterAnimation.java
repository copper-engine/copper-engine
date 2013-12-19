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
package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.animation;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;

public class AdapterAnimation extends AnimationPartBase {

    public static final int ADAPTER_HEIGHT = 100;
    public static final int ADAPTER_WIDTH = 150;
    public static final Color ADAPTER_COLOR = Color.LIGHTGREEN;

    public AdapterAnimation(AnimationPartParameter animationPartBaseParameter) {
        super(animationPartBaseParameter);
    }

    @Override
    public Node createVisualRepresentation() {
        Pane pane = new Pane();
        final Rectangle adapterRectangle = new Rectangle(ADAPTER_WIDTH, ADAPTER_HEIGHT);
        adapterRectangle.setFill(ADAPTER_COLOR);
        adapterRectangle.setArcHeight(25);
        adapterRectangle.setArcWidth(25);
        final Text adapterText = new Text(id);
        adapterText.setFontSmoothingType(FontSmoothingType.LCD);
        adapterText.xProperty().bind(adapterRectangle.xProperty().add(adapterRectangle.getWidth() / 2).subtract(adapterText.getBoundsInLocal().getWidth() / 2));
        adapterText.yProperty().bind(adapterRectangle.yProperty().subtract(5));
        pane.getChildren().add(adapterRectangle);
        pane.getChildren().add(adapterText);
        return pane;
    }

}
