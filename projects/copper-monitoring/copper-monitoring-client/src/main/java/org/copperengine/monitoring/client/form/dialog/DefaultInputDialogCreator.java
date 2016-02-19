/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.form.dialog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.copperengine.monitoring.client.util.CSSHelper;
import org.copperengine.monitoring.client.util.NumberOnlyTextField;

public class DefaultInputDialogCreator implements InputDialogCreator {

    private final StackPane target;

    public DefaultInputDialogCreator(StackPane stackPane) {
        super();
        this.target = stackPane;
    }

    @Override
    public void showIntInputDialog(final String labelText, final int initialValue, final DialogClosed<Integer> dialogClosed) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final Pane backShadow = new Pane();
                Color color = new Color(0.2f, 0.2f, 0.2f, 0.5);
                backShadow.setStyle("-fx-background-color: " + CSSHelper.toCssColor(color) + ";");
                target.getChildren().add(backShadow);

                HBox back = new HBox(3);
                back.setAlignment(Pos.CENTER_RIGHT);
                final Label label = new Label(labelText);
                label.setStyle("-fx-text-fill: -fx-dark-text-color;");
                label.setWrapText(true);
                back.getChildren().add(label);
                back.setAlignment(Pos.CENTER);
                back.setSpacing(5);
                back.setStyle("-fx-background-color: -fx-background; -fx-border-color: -fx-box-border; -fx-border-width: 1px; -fx-padding: 10;");

                final HBox backWrapper = new HBox();
                backWrapper.setAlignment(Pos.CENTER);
                backWrapper.setFillHeight(false);
                backWrapper.getChildren().add(back);

                final NumberOnlyTextField textField = new NumberOnlyTextField();
                textField.setText(String.valueOf(initialValue));
                back.getChildren().add(textField);

                Button ok = new Button("OK");
                ok.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        target.getChildren().remove(backWrapper);
                        target.getChildren().remove(backShadow);
                        if (textField.getText() != null && !textField.getText().isEmpty()) {
                            dialogClosed.closed(Integer.parseInt(textField.getText()));
                        }
                    }
                });
                back.getChildren().add(ok);

                Button cancel = new Button("Cancel");
                cancel.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        target.getChildren().remove(backWrapper);
                        target.getChildren().remove(backShadow);
                    }
                });
                back.getChildren().add(cancel);

                target.getChildren().add(backWrapper);
            }
        });
    }

}
