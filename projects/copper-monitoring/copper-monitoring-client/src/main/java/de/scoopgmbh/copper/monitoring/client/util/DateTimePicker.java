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
package de.scoopgmbh.copper.monitoring.client.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import com.google.common.base.Optional;

import de.scoopgmbh.copper.monitoring.client.form.Widget;

public class DateTimePicker extends Application implements Widget{
	
	public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
	
	private static class DateTimeModel{
		public final SimpleObjectProperty<Integer> month = new SimpleObjectProperty<Integer>(0);
		public final SimpleObjectProperty<Integer> year = new SimpleObjectProperty<Integer>(0);
		public final SimpleObjectProperty<Integer> dayInMonth = new SimpleObjectProperty<Integer>(0);
		public final SimpleObjectProperty<Integer> hour = new SimpleObjectProperty<Integer>(0);
		public final SimpleObjectProperty<Integer> minute = new SimpleObjectProperty<Integer>(0);
		public final SimpleObjectProperty<Integer> secound = new SimpleObjectProperty<Integer>(0);
		
		public Optional<Calendar> createCalendar(){
			if (isNull()){
				return Optional.absent();
			}
			Calendar calendar= Calendar.getInstance();
			calendar.set(Calendar.MONTH, month.get()-1);
			calendar.set(Calendar.YEAR, year.get());
			calendar.set(Calendar.DAY_OF_MONTH, dayInMonth.get());
			calendar.set(Calendar.HOUR_OF_DAY, hour.get());
			calendar.set(Calendar.MINUTE, minute.get());
			calendar.set(Calendar.SECOND, secound.get());	
			return Optional.of(calendar);
		}
		
		public void setDate(Date date){
			if (date==null){
				setNull();
			} else {
				Calendar calendar= Calendar.getInstance();
				calendar.setTime(date);
				month.set(calendar.get(Calendar.MONTH)+1);
				year.set(calendar.get(Calendar.YEAR));
				dayInMonth.set(calendar.get(Calendar.DAY_OF_MONTH));
				hour.set(calendar.get(Calendar.HOUR_OF_DAY));
				minute.set(calendar.get(Calendar.MINUTE));
				secound.set(calendar.get(Calendar.SECOND));
			}
		}
		public Date getDate(){
			final Optional<Calendar> calendar = createCalendar();
			if (calendar.isPresent()){
				return calendar.get().getTime();
			} else {
				return null;	
			}
		}
		
		private boolean isNull(){
			return
				month.get()==null ||
				year.get()==null ||
				dayInMonth.get()==null ||
				hour.get()==null ||
				minute.get()==null ||
				secound.get()==null;
		}
		
		private void setNull(){
			month.set(null);
			year.set(null);
			dayInMonth.set(null);
			hour.set(null);
			minute.set(null);
			secound.set(null);
		}
		
		public void addChangeListener(ChangeListener<Integer> listener){
			month.addListener(listener);
			year.addListener(listener);
			dayInMonth.addListener(listener);
			hour.addListener(listener);
			minute.addListener(listener);
			secound.addListener(listener);
		}
	}
	
	SimpleDateFormat dateFormat;
	String dateFormatText;
	public DateTimePicker(String dateFormat){
		this.dateFormat = new SimpleDateFormat(dateFormat);
		this.dateFormat.setLenient(true);
		this.dateFormatText = dateFormat;
	}
	
	public DateTimePicker(){
		this(DEFAULT_DATE_FORMAT);
	}
	
	DateTimeModel model = new DateTimeModel();
	private ToggleButton[] buttons;

	@Override
	public HBox createContent() {
		HBox hbox = new HBox(3);
		final TextField textfield = new TextField();
		textfield.setPromptText(dateFormatText);
		textfield.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				boolean parsed=true;
				Date date=null;
				if (newValue!=null && !newValue.isEmpty()) {
					try {
						date = dateFormat.parse(newValue);
					} catch (ParseException e) {
						parsed=false;
					}
				}
				if (!parsed){
					if (!textfield.getStyleClass().contains("error")){
						textfield.getStyleClass().add("error");
					}
					if (textfield.isFocused()){
						setModelDate(null);
					}
				} else {
					textfield.getStyleClass().remove("error");
					if (textfield.isFocused()){
						setModelDate(date);
					}
				}
			}
		});
		
		model.addChangeListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				if (newValue!=null && !model.isNull() && !textfield.isFocused()){
					textfield.setText(dateFormat.format(model.getDate()));
				} else {
					textfield.clear();
				}
			}
		});
		
		hbox.getChildren().add(textfield);
		MenuButton defaultFilterButton = new MenuButton("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/date.png"))));
		defaultFilterButton.setPrefWidth(20);
		CustomMenuItem defaultFilterContent = new CustomMenuItem();
		defaultFilterContent.setHideOnClick(false);
		defaultFilterButton.getItems().add(defaultFilterContent);
		defaultFilterContent.getStyleClass().setAll("noSelectAnimationMenueItem","menu-item");
		hbox.getChildren().add(defaultFilterButton);
		defaultFilterContent.setContent(createPopupContent());
		
		setModelDate(null);
		bindBidirectionalSelected(selectedDateProperty);
		return hbox;
	}
	
	private void setModelDate(Date date){
		model.setDate(date);
	}
	
	private VBox createPopupContent(){
		VBox vbox= new VBox(3);
		HBox topbuttons = new HBox(3);
		final Button now = new Button("now");
		now.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setModelDate(new Date());
			}
		});
		topbuttons.getChildren().add(now);
		final Button nowMinus5 = new Button("now - 5 min");
		nowMinus5.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MINUTE, -5);
				setModelDate(calendar.getTime());
			}
		});
		topbuttons.getChildren().add(nowMinus5);
		final Button nullBotton = new Button("clear");
		nullBotton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setModelDate(null);
			}
		});
		topbuttons.getChildren().add(nullBotton);
		vbox.getChildren().add(topbuttons);
		final Label label = new Label();
		vbox.getChildren().add(label);
		model.addChangeListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				if (!model.isNull()){
					label.setText(dateFormat.format(model.getDate()));
				} else {
					label.setText("");
				}
			}
		});
		final HBox yearMonthChooser = createYearMonthChooser();
		vbox.getChildren().add(yearMonthChooser);
		
        final GridPane gridPane = new GridPane();
        vbox.getChildren().add(gridPane);
        updateDayInMonthChooser(gridPane);
        model.month.addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				updateDayInMonthChooser(gridPane);
			}
		});
        model.year.addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				updateDayInMonthChooser(gridPane);
			}
		});
        VBox.setMargin(gridPane, new Insets(3));
        final HBox timeChooser = createTimeChooser();
		vbox.getChildren().add(timeChooser);
        
        model.addChangeListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				gridPane.setDisable(model.isNull());
				timeChooser.setDisable(model.isNull());
				yearMonthChooser.setDisable(model.isNull());
			}
		});
		return vbox;
	}
	
	private HBox createYearMonthChooser(){
		final HBox hBox = new HBox(3);
		hBox.setAlignment(Pos.CENTER_LEFT);
		final ComboBox<Integer> months = new ComboBox<Integer>();
		months.setConverter(new IntegerStringConverter());
		months.valueProperty().bindBidirectional(model.month);
		months.setEditable(true);
		for (int i=0;i<12;i++){
			months.getItems().add(i+1);
		}
		final ComboBox<Integer> years = new ComboBox<Integer>();
		for (int i=1970;i<2100;i++){
			years.getItems().add(i);
		}
		years.valueProperty().bindBidirectional(model.year);
		years.setEditable(true);
		years.setConverter(new IntegerStringConverter());
		hBox.getChildren().add(new Label("Date"));
		hBox.getChildren().add(months);
		hBox.getChildren().add(years);
		VBox.setMargin(hBox, new Insets(3));
		return hBox;
	}
	
	private HBox createTimeChooser(){
		final HBox hBox = new HBox(3);
		hBox.setAlignment(Pos.CENTER_LEFT);
		final ComboBox<Integer> hour = new ComboBox<Integer>();
		hour.setConverter(new IntegerStringConverter());
		hour.valueProperty().bindBidirectional(model.hour);
		hour.setEditable(true);
		hour.setPrefWidth(20);
		for (int i=0;i<24;i++){
			hour.getItems().add(i);
		}
		final ComboBox<Integer> minute = new ComboBox<Integer>();
		for (int i=1;i<61;i++){
			minute.getItems().add(i);
		}
		minute.valueProperty().bindBidirectional(model.minute);
		minute.setEditable(true);
		minute.setConverter(new IntegerStringConverter());
		minute.setPrefWidth(20);
		final ComboBox<Integer> secound = new ComboBox<Integer>();
		for (int i=1;i<61;i++){
			secound.getItems().add(i);
		}
		secound.valueProperty().bindBidirectional(model.secound);
		secound.setEditable(true);
		secound.setConverter(new IntegerStringConverter());
		secound.setPrefWidth(20);
		hBox.getChildren().add(new Label("Time"));
		hBox.getChildren().add(hour);
		hBox.getChildren().add(new Label(":"));
		hBox.getChildren().add(minute);
		hBox.getChildren().add(new Label(":"));
		hBox.getChildren().add(secound);
		VBox.setMargin(hBox, new Insets(3));
		return hBox;
	}
	
	private void updateDayInMonthChooser(GridPane gridPane){
		gridPane.getChildren().clear();
		String[] dayNames = new String[]{"Mon","Tue","Wed","Thu","Fri","Sat","Sun",};
		
		for(int i=0; i<dayNames.length; i++){
			gridPane.add(new Label(dayNames[i]), i, 0);
		}

		int firstDay=0;
		int actualDayOfMounthMaximum=30;
		final Optional<Calendar> calendarOptional = model.createCalendar();
		if (calendarOptional.isPresent()){
			Calendar calendar = calendarOptional.get();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			firstDay=getDayInWeek(calendar);
			if (firstDay<0){
				firstDay=6;
			}
			calendar.set(Calendar.DAY_OF_MONTH,1);
			actualDayOfMounthMaximum = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		}

		
		DecimalFormat decimalFormat = new DecimalFormat("00");
		if (buttons==null){
			buttons = new ToggleButton[31];
			for (int i=0;i<31;i++){
				final int day = i+1;
				final ToggleButton button = new ToggleButton(decimalFormat.format(day));
				button.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						model.dayInMonth.set(day);
					}
				});
				model.dayInMonth.addListener(new ChangeListener<Integer>() {
					@Override
					public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
						if (newValue!=null && day==newValue){
							button.setSelected(true);
						} else {
							button.setSelected(false);
						}
					}
				});
				button.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						if (button.isSelected()){
							model.dayInMonth.set(day);
						}
					}
				});
				buttons[i]=button;
			}
		}
		

		for(int i=0; i<actualDayOfMounthMaximum;i++){
			gridPane.add(buttons[i], (i+firstDay)%7, 1+(i+firstDay)/7);
        }
		gridPane.getParent().requestLayout();
	}
	
	private int getDayInWeek(Calendar calendar){
		final int day = calendar.get(Calendar.DAY_OF_WEEK);
		if (day==Calendar.MONDAY){
			return 0;
		};
		if (day==Calendar.TUESDAY){
			return 1;
		};
		if (day==Calendar.WEDNESDAY){
			return 2;
		};
		if (day==Calendar.THURSDAY){
			return 3;
		};
		if (day==Calendar.FRIDAY){
			return 4;
		};
		if (day==Calendar.SATURDAY){
			return 5;
		};
		if (day==Calendar.SUNDAY){
			return 6;
		};
		throw new IllegalStateException();
	}
	
	
	boolean setFlag=false;
	public void  bindBidirectionalSelected(final SimpleObjectProperty<Date> date){
        model.addChangeListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				if (!setFlag){
					setFlag=true;					
					date.set(model.getDate());
					setFlag=false;
				}
			}
		});
		date.addListener(new ChangeListener<Date>() {
			@Override
			public void changed(ObservableValue<? extends Date> observable, Date oldValue, Date newValue) {
				if (!setFlag){
					setFlag=true;					
					setModelDate(newValue);
					setFlag=false;
				}
			}
		});
	}
	SimpleObjectProperty<Date> selectedDateProperty = new SimpleObjectProperty<Date>();
	public SimpleObjectProperty<Date> selectedDateProperty(){
		return selectedDateProperty;
	}
	 
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		final Scene scene = new Scene(createContent());
		scene.getStylesheets().add(this.getClass().getResource("/de/scoopgmbh/copper/gui/css/base.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
		
		SimpleObjectProperty<Date> date = new SimpleObjectProperty<Date>(new Date());
		bindBidirectionalSelected(date);
//		date.set(null);
		
		date.addListener(new ChangeListener<Date>() {
			@Override             
			public void changed(ObservableValue<? extends Date> observable, Date oldValue, Date newValue) {
				System.err.println("old"+oldValue);
				System.err.println("new"+newValue);
			}
		});
	}
	
	
	public static void main(String[] args) {
		DateTimePicker.launch(args);;
	}


	
	
	

}
