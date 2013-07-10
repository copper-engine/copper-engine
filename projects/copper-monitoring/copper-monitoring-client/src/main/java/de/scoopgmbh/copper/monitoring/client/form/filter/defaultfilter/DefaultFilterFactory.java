package de.scoopgmbh.copper.monitoring.client.form.filter.defaultfilter;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.converter.DateStringConverter;
import javafx.util.converter.IntegerStringConverter;
import de.scoopgmbh.copper.monitoring.client.util.DateValidationHelper;
import de.scoopgmbh.copper.monitoring.client.util.NumberOnlyTextField;

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
	
	public void createMaxCount(HBox parent,MaxCountFilterModel maxCountFilterModel){
		TextField maxCount = new NumberOnlyTextField();
		maxCount.setPrefWidth(100);
		Bindings.bindBidirectional(maxCount.textProperty(), maxCountFilterModel.maxCount, new IntegerStringConverter());
		parent.getChildren().add(new Label("limit"));
		parent.getChildren().add(maxCount);
	}
	
	public void createFromToUI(HBox parent,FromToFilterModel fromToFilterModel){
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
