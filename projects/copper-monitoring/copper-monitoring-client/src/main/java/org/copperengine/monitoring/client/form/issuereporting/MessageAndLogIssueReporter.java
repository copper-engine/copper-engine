/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.form.issuereporting;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.copperengine.monitoring.client.util.CSSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

public class MessageAndLogIssueReporter implements IssueReporter {

    Logger logger = LoggerFactory.getLogger(MessageAndLogIssueReporter.class);
    private final StackPane target;

    public MessageAndLogIssueReporter(StackPane stackPane) {
        super();
        this.target = stackPane;
    }

    @Override
    public void reportError(Throwable e) {
        logger.error("", e);
        showErrorMessage(e.getMessage(), e);
    }

    @Override
    public void reportError(String message, Throwable e) {
        logger.error("", e);
        showErrorMessage(message, e);
    }

    @Override
    public void reportWarning(Throwable e) {
        logger.warn("", e);
        showWarningMessage(e.getMessage(), e);
    }

    @Override
    public void reportWarning(String message, Throwable e) {
        logger.warn("", e);
        showWarningMessage(message, e);
    }

    @Override
    public void reportError(String message, Throwable e, Runnable finishAction) {
        logger.error("", e);
        showErrorMessage(message, e, finishAction);
    }

    @Override
    public void reportWarning(String message, Throwable e, Runnable finishAction) {
        logger.warn("", e);
        showWarningMessage(message, e, finishAction);
    }

    private void showErrorMessage(String message, Throwable e) {
        showErrorMessage(message, e, null);
    }

    private void showErrorMessage(String message, Throwable e, Runnable okOnACtion) {
        showMessage(message, e, Color.rgb(255, 0, 0, 0.55), new ImageView(getClass().getResource("/org/copperengine/gui/icon/error.png").toExternalForm()), okOnACtion);
    }

    private void showWarningMessage(String message, Throwable e, Runnable okOnACtion) {
        showMessage(message, e, Color.rgb(255, 200, 90, 0.75), new ImageView(getClass().getResource("/org/copperengine/gui/icon/warning.png").toExternalForm()), okOnACtion);
    }

    private void showWarningMessage(String message, Throwable e) {
        showWarningMessage(message, e, null);
    }

    private void showMessage(final String message, final Throwable e, final Color backColor, final ImageView icon, final Runnable okOnAction) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final Pane backShadow = new Pane();
                backShadow.setStyle("-fx-background-color: " + CSSHelper.toCssColor(backColor) + ";");
                target.getChildren().add(backShadow);

                String blackOrWhiteDependingFromBack = "ladder(" + CSSHelper.toCssColor(backColor) + ", white 49%, black 50%);";

                final VBox back = new VBox(3);
                StackPane.setMargin(back, new Insets(150));
                back.setStyle("-fx-border-color: " + blackOrWhiteDependingFromBack + "; -fx-border-width: 1px; -fx-padding: 3; -fx-background-color: derive(" + CSSHelper.toCssColor(backColor) + ",-50%);");
                back.setAlignment(Pos.CENTER_RIGHT);
                final Label label = new Label(message);
                label.prefWidthProperty().bind(target.widthProperty());
                StackPane.setMargin(back, new Insets(150));
                label.setStyle("-fx-text-fill: " + blackOrWhiteDependingFromBack + ";");
                label.setWrapText(true);
                label.setGraphic(icon);
                back.getChildren().add(label);

                final TextArea area = new TextArea();
                area.setPrefRowCount(10);
                if (e != null) {
                    area.setText(Throwables.getStackTraceAsString(e));
                }
                area.setOpacity(0.4);
                area.setEditable(false);
                VBox.setVgrow(area, Priority.ALWAYS);
                back.getChildren().add(area);
                area.getStyleClass().add("consoleFont");

                ContextMenu menue = new ContextMenu();
                MenuItem item = new MenuItem("copy to clipboard");
                item.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(area.getText());
                        clipboard.setContent(content);
                    }
                });
                menue.getItems().add(item);
                area.setContextMenu(menue);

                Button ok = new Button("OK");
                ok.setPrefWidth(100);
                ok.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        target.getChildren().remove(back);
                        target.getChildren().remove(backShadow);
                        if (okOnAction != null) {
                            okOnAction.run();
                        }
                    }
                });
                back.getChildren().add(ok);

                target.getChildren().add(back);
            }
        });
    }

}
