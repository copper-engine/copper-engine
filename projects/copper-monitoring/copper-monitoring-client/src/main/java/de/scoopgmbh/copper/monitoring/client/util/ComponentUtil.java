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

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import javafx.util.StringConverter;

import com.google.common.base.Throwables;

public class ComponentUtil {
	
	public static Node createProgressIndicator(){
		ProgressIndicator indicator = new ProgressIndicator();
		indicator.setMaxHeight(350);
		indicator.setMaxWidth(350);
		
		BorderPane borderPane = new BorderPane();
		BorderPane.setMargin(indicator, new Insets(5));
		borderPane.setCenter(indicator);
		borderPane.setStyle("-fx-background-color: rgba(230,230,230,0.7);");
		return borderPane;
	}
	
	public static void startValueSetAnimation(final Pane parent) {
		final javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle();
		Insets margin = BorderPane.getMargin(parent);
		if (margin==null){
			margin= new Insets(0);
		}
		rectangle.widthProperty().bind(parent.widthProperty().subtract(margin.getLeft()+margin.getRight()));
		rectangle.heightProperty().bind(parent.heightProperty().subtract(margin.getTop()+margin.getBottom()));
		rectangle.setFill(Color.rgb(0, 150, 201));
		parent.getChildren().add(rectangle);
		
        BoxBlur bb = new BoxBlur();
        bb.setWidth(5);
        bb.setHeight(5);
        bb.setIterations(3);
        rectangle.setEffect(bb);
        
		FadeTransition ft = new FadeTransition(Duration.millis(250), rectangle);
		ft.setFromValue(0.2);
		ft.setToValue(0.8);
		ft.setCycleCount(2);
		ft.setAutoReverse(true);
		ft.play();
		ft.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				parent.getChildren().remove(rectangle);
			}
		});
	}
	
	
	public static void executeWithProgressDialogInBackground(final Runnable runnable, final StackPane target, final String text){
		Thread th = new Thread(){
			@Override
			public void run() {
				final Node progressIndicator = createProgressIndicator();
				final Label label = new Label(text);
				try {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							target.getChildren().add(progressIndicator);
							label.setWrapText(true);
							target.getChildren().add(label);
						}
					});
					runnable.run();
				} finally {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							target.getChildren().remove(progressIndicator);
							target.getChildren().remove(label);
						}
					});	
				}
			}
		};
		th.setDaemon(true);
		th.start();
	}
	
	
	public static void setupXAxis(NumberAxis numberAxis, ObservableList<XYChart.Series<Number, Number>> seriesList){
		long min=Long.MAX_VALUE;
		long max=0;

		for (XYChart.Series<Number, ?> series: seriesList){
			for (Data<Number, ?> data: series.getData()){
				min = Math.min(data.getXValue().longValue(),min);
				max = Math.max(data.getXValue().longValue(),max);
			}
		}
		setupXAxis(numberAxis, min, max );
	}
	
	public static void setupXAxis(NumberAxis numberAxis, long min, long max ){
		numberAxis.setAutoRanging(false);
		numberAxis.setTickUnit((max-min)/20);
		numberAxis.setLowerBound(min);
		numberAxis.setUpperBound(max);
		numberAxis.setTickLabelFormatter(new StringConverter<Number>() {
			private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy\nHH:mm:ss,SSS");
			@Override
			public String toString(Number object) {
				return format.format(new Date(object.longValue()));
			}
			
			@Override
			public Number fromString(String string) {
				return null;
			}
		});
	}
	
	public static void addMarker(final XYChart<?, ?> chart, final StackPane chartWrap) {
		final Line valueMarker = new Line();
		final Node chartArea = chart.lookup(".chart-plot-background");

		chartArea.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Point2D scenePoint = chart.localToScene(event.getSceneX(), event.getSceneY());
				Point2D position = chartWrap.sceneToLocal(scenePoint.getX(), scenePoint.getY());
				
				Bounds chartAreaBounds = chartArea.localToScene(chartArea.getBoundsInLocal());
				valueMarker.setStartY(0);
				valueMarker.setEndY(chartWrap.sceneToLocal(chartAreaBounds).getMaxY()-chartWrap.sceneToLocal(chartAreaBounds).getMinY());
				
				valueMarker.setStartX(0);
				valueMarker.setEndX(0);
				valueMarker.setTranslateX(position.getX()-chartWrap.getWidth()/2);
				
				double ydelta = chartArea.localToScene(0, 0).getY()-chartWrap.localToScene(0,0).getY();
				valueMarker.setTranslateY(-ydelta*2);
			}
		});
		
		chartWrap.getChildren().add(valueMarker);
	}
	
	public static void showErrorMessage(StackPane target, String message, Throwable e){
		showErrorMessage(target,message,e,null);
	}
	
	public static void showErrorMessage(StackPane target, String message, Throwable e, Runnable okOnACtion){
		showMessage(target,message,e,Color.rgb(255,0,0,0.55), new ImageView(ComponentUtil.class.getResource("/de/scoopgmbh/copper/gui/icon/error.png").toExternalForm()),okOnACtion);
	}
	
	public static void showWarningMessage(StackPane target, String message, Throwable e, Runnable okOnACtion){
		showMessage(target,message,e,Color.rgb(255,200,90,0.75), new ImageView(ComponentUtil.class.getResource("/de/scoopgmbh/copper/gui/icon/warning.png").toExternalForm()),okOnACtion);
	}
	
	public static void showWarningMessage(StackPane target, String message, Throwable e){
		showWarningMessage(target,message,e,null);
	}
	
	public static interface MessageDisplayer {
		void showMessage(StackPane target, String message, Throwable e, Color backColor, ImageView icon, Runnable okOnAction);
	}
	public static final MessageDisplayer DEFAULT_MESSAGE_DISPLAYER = new MessageDisplayer() {		
		@Override
		public void showMessage(final StackPane target, final String message, final Throwable e, final Color backColor, final ImageView icon, final Runnable okOnAction) {
			final Pane backShadow = new Pane();
			backShadow.setStyle("-fx-background-color: "+CSSHelper.toCssColor(backColor)+";");
			target.getChildren().add(backShadow);
			
			String blackOrWhiteDependingFromBack ="ladder("+CSSHelper.toCssColor(backColor)+", white 49%, black 50%);";
			
			final VBox back = new VBox(3);
			StackPane.setMargin(back, new Insets(150));
			back.setStyle("-fx-border-color: "+blackOrWhiteDependingFromBack +"; -fx-border-width: 1px; -fx-padding: 3; -fx-background-color: derive("+CSSHelper.toCssColor(backColor)+",-50%);");
			back.setAlignment(Pos.CENTER_RIGHT);
			final Label label = new Label(message);
			label.prefWidthProperty().bind(target.widthProperty());
			StackPane.setMargin(back, new Insets(150));
			label.setStyle("-fx-text-fill: "+blackOrWhiteDependingFromBack +";");
			label.setWrapText(true);
			label.setGraphic(icon);
			back.getChildren().add(label);
			
			final TextArea area = new TextArea();
			area.setPrefRowCount(10);
			if (e!=null){
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
					if (okOnAction!=null){
						okOnAction.run();
					}
				}
			});
			back.getChildren().add(ok);
			
			target.getChildren().add(back);			
		}
	}; 
	private static MessageDisplayer messageDisplayer = DEFAULT_MESSAGE_DISPLAYER;
	
	public static void setMessageDisplayer(MessageDisplayer messageDisplayer) {
		ComponentUtil.messageDisplayer = messageDisplayer;
	}
	
	public static void showMessage(final StackPane target, final String message, final Throwable e, final Color backColor, final ImageView icon, final Runnable okOnAction){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				messageDisplayer.showMessage(target, message, e, backColor, icon, okOnAction);
			}
		});
	}

}
