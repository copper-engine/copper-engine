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
package de.scoopgmbh.copper.monitoring.client.form.filter.defaultfilter;

import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.DateStringConverter;
import javafx.util.converter.IntegerStringConverter;
import de.scoopgmbh.copper.monitoring.client.form.filter.enginefilter.EnginePoolFilterModel;
import de.scoopgmbh.copper.monitoring.client.form.filter.enginefilter.EngineSelectionWidget;
import de.scoopgmbh.copper.monitoring.client.util.DateValidationHelper;
import de.scoopgmbh.copper.monitoring.client.util.NumberOnlyTextField;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;

public class DefaultFilterFactory {
	
	public static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";


	public Node createFromTo(FromToFilterModel fromToFilterModel){
		HBox hbox = createBackpane();
		createFromToUI(hbox,fromToFilterModel);
		return hbox;
	}
	
	public Node createMaxCount(MaxCountFilterModel maxCountFilterModel){
		HBox hbox = createBackpane();
		createMaxCount(hbox,maxCountFilterModel);
		return hbox;
	}

	public Node createFromToMaxCount(FromToMaxCountFilterModel fromToMaxCountFilterModel){
		HBox hbox = createBackpane();
		createFromToUI(hbox,fromToMaxCountFilterModel.fromToFilterModel);
		createMaxCount(hbox,fromToMaxCountFilterModel.maxCountFilterModel);
		return hbox;
	}
	
	public Node createVerticalMultiFilter(Node... filterrows){
		VBox vbox = new VBox(3);
		for (int i = 0; i < filterrows.length; i++) {
			Node filterrow = filterrows[i];
			vbox.getChildren().add(filterrow);
			if (i < filterrows.length-1){
				vbox.getChildren().add(new Separator(Orientation.HORIZONTAL));
			}
		}
		return vbox;
	}
	
	public Node createEngineFilterUI(EnginePoolFilterModel model, List<ProcessingEngineInfo> engineList){
		HBox hbox = new HBox(3);
		hbox.setAlignment(Pos.CENTER_LEFT);
		EngineSelectionWidget engineSelectionWidget = new EngineSelectionWidget(model, engineList);
		Node node = engineSelectionWidget.createContent();
		hbox.getChildren().add(node);
		return hbox;
	}
	
	private void createMaxCount(HBox parent,MaxCountFilterModel maxCountFilterModel){
		TextField maxCount = new NumberOnlyTextField();
		maxCount.setPrefWidth(100);
		Bindings.bindBidirectional(maxCount.textProperty(), maxCountFilterModel.maxCount, new IntegerStringConverter());
		parent.getChildren().add(new Label("limit"));
		parent.getChildren().add(maxCount);
	}
	
	private void createFromToUI(HBox parent,FromToFilterModel fromToFilterModel){
		TextField from = new TextField();
		from.setPrefWidth(170);
		Bindings.bindBidirectional(from.textProperty(), fromToFilterModel.from, new DateStringConverter(DATE_FORMAT));
		from.setPromptText(DATE_FORMAT);
		parent.getChildren().add(new Label("from"));
		parent.getChildren().add(from);
		
		TextField to = new TextField();
		to.setPrefWidth(170);
		Bindings.bindBidirectional(to.textProperty(), fromToFilterModel.to, new DateStringConverter(DATE_FORMAT));
		to.setPromptText(DATE_FORMAT);
		parent.getChildren().add(new Label("to"));
		parent.getChildren().add(to);
		
		DateValidationHelper.addValidation(from, DATE_FORMAT);
		DateValidationHelper.addValidation(to, DATE_FORMAT);
	}
	
	private HBox createBackpane(){
		HBox hbox = new HBox(3);
		hbox.setAlignment(Pos.CENTER_LEFT);
		return hbox;
	}

}
