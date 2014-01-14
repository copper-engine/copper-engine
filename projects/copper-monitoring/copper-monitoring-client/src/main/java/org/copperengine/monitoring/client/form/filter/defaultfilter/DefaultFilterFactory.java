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
package org.copperengine.monitoring.client.form.filter.defaultfilter;

import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;
import org.copperengine.monitoring.client.form.filter.enginefilter.EnginePoolFilterModel;
import org.copperengine.monitoring.client.form.filter.enginefilter.EngineSelectionWidget;
import org.copperengine.monitoring.client.util.DateTimePicker;
import org.copperengine.monitoring.client.util.NumberOnlyTextField;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;

public class DefaultFilterFactory {

    public static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

    public Node createFromTo(FromToFilterModel fromToFilterModel) {
        VBox vbox = createBackpane();
        createFromToUI(vbox, fromToFilterModel);
        return vbox;
    }

    public Node createMaxCount(MaxCountFilterModel maxCountFilterModel) {
        VBox vbox = createBackpane();
        createMaxCount(vbox, maxCountFilterModel);
        return vbox;
    }

    public Node createFromToMaxCount(FromToMaxCountFilterModel fromToMaxCountFilterModel) {
        VBox vbox = createBackpane();
        createFromToUI(vbox, fromToMaxCountFilterModel.fromToFilterModel);
        createMaxCount(vbox, fromToMaxCountFilterModel.maxCountFilterModel);
        return vbox;
    }

    public Node createVerticalMultiFilter(Node... filterrows) {
        VBox vbox = new VBox(3);
        for (int i = 0; i < filterrows.length; i++) {
            Node filterrow = filterrows[i];
            vbox.getChildren().add(filterrow);
            if (i < filterrows.length - 1) {
                vbox.getChildren().add(new Separator(Orientation.HORIZONTAL));
            }
        }
        return vbox;
    }

    public Node createEngineFilterUI(EnginePoolFilterModel model, List<ProcessingEngineInfo> engineList) {
        HBox hbox = new HBox(3);
        hbox.setAlignment(Pos.CENTER_LEFT);
        EngineSelectionWidget engineSelectionWidget = new EngineSelectionWidget(model, engineList);
        Node node = engineSelectionWidget.createContent();
        hbox.getChildren().add(node);
        return hbox;
    }

    private void createMaxCount(VBox parent, MaxCountFilterModel maxCountFilterModel) {
        TextField maxCount = new NumberOnlyTextField();
        maxCount.setPrefWidth(100);
        Bindings.bindBidirectional(maxCount.textProperty(), maxCountFilterModel.maxCount, new IntegerStringConverter());
        parent.getChildren().add(createDescriptionVale("limit",maxCount));
    }

    private void createFromToUI(VBox parent, FromToFilterModel fromToFilterModel) {
        final DateTimePicker fromDateTimePicker = new DateTimePicker();
        fromDateTimePicker.bindBidirectionalSelected(fromToFilterModel.from);
        Pane from = fromDateTimePicker.createContent();
        from.setPrefWidth(170);
        parent.getChildren().add(createDescriptionVale("from",from));

        final DateTimePicker toDateTimePicker = new DateTimePicker();
        toDateTimePicker.bindBidirectionalSelected(fromToFilterModel.to);
        Pane to = toDateTimePicker.createContent();
        to.setPrefWidth(170);

        parent.getChildren().add(createDescriptionVale("to",to));
    }

    private HBox createDescriptionVale(String description, Node value){
        final HBox result = new HBox();
        result.setAlignment(Pos.CENTER_LEFT);
        result.setSpacing(3);
        final Label label = new Label(description);
        label.setMinWidth(50);
        HBox.setHgrow(label, Priority.NEVER);
        result.getChildren().add(label);
        result.getChildren().add(value);
        HBox.setHgrow(value, Priority.ALWAYS);
        return result;
    }

    private VBox createBackpane() {
        VBox vBox = new VBox(3);
        vBox.setAlignment(Pos.TOP_LEFT);
        return vBox;
    }

}
