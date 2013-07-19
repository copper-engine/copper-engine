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
package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class AdapterAnnimationCreator {
	
	private static final Color WORKFLOW_COLOR = Color.GOLD;
	private static final Color ADAPTER_NOTIFY_COLOR = Color.CORNFLOWERBLUE;
	private static final Color ADAPTER_CALL_COLOR = Color.CORAL;
	private static final Color ADAPTER_COLOR = Color.LIGHTGREEN;
	private static final Color ADAPTER_LAUNCH_COLOR = Color.SADDLEBROWN; 
	public long FADEDURATION=300;
	public long MOVEDURATION=1600;
	public long TOTAL_ANNIMATION_TIME=FADEDURATION+MOVEDURATION+FADEDURATION;
	
	
	private long min;
	
    public long getMin() {
		return min;
	}
	public void setMin(long min) {
		this.min = min;
	}
	
	Pane annimationPane;
	Timeline timeline;
	
	public AdapterAnnimationCreator(Pane annimationPane, Timeline timeline) {
		super();
		this.annimationPane = annimationPane;
		this.timeline = timeline;
	}
	
	public void create( 
    		ObservableList<AdapterCallRowModel> adapterInput,
    		ObservableList<AdapterLaunchRowModel> adapterOutputLaunch,
    		ObservableList<AdapterNotifyRowModel> adapterOutputNotify,
    		Slider positionSlider){
    	min = Long.MAX_VALUE;
		long max = 0;
		HashMap<String,Double> adapterToYPos = new HashMap<String,Double>();
		double adapterYPos=50;
		for (final AdapterCallRowModel adapterCallRowModel: adapterInput){
			if (!adapterToYPos.containsKey(adapterCallRowModel.adapterName.get())){
				adapterToYPos.put(adapterCallRowModel.adapterName.get(), adapterYPos);
				adapterYPos=adapterYPos+250;
			}
			min = Math.min(min, adapterCallRowModel.timestamp.get().getTime()-TOTAL_ANNIMATION_TIME);
			max = Math.max(max, adapterCallRowModel.timestamp.get().getTime()-TOTAL_ANNIMATION_TIME);
		}
		for (final AdapterLaunchRowModel adapterLaunchRowModel: adapterOutputLaunch){
			if (!adapterToYPos.containsKey(adapterLaunchRowModel.adapterName.get())){
				adapterToYPos.put(adapterLaunchRowModel.adapterName.get(), adapterYPos);
				adapterYPos=adapterYPos+250;
			}
			min = Math.min(min, adapterLaunchRowModel.timestamp.get().getTime());
			max = Math.max(max, adapterLaunchRowModel.timestamp.get().getTime());
		}
		for (final AdapterNotifyRowModel adapterNotifyRowModel: adapterOutputNotify){
			if (!adapterToYPos.containsKey(adapterNotifyRowModel.adapterName.get())){
				adapterToYPos.put(adapterNotifyRowModel.adapterName.get(), adapterYPos);
				adapterYPos=adapterYPos+250;
			}
			min = Math.min(min, adapterNotifyRowModel.timestamp.get().getTime());
			max = Math.max(max, adapterNotifyRowModel.timestamp.get().getTime());
		}
		
		addStaticContent(adapterToYPos);
		
		
		positionSlider.setMax(max-min);

		ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>();//Performance optimisation adding single keyframes directly is too slow
		final LinkedList<TimeIdPair> inputEndPositions = new LinkedList<TimeIdPair>();
		for (final AdapterCallRowModel adapterCallRowModel: adapterInput){
			createInputAnnimationEvent(keyFrames,timeline, adapterCallRowModel.timestamp.get().getTime()-min,
					adapterCallRowModel.method.get(), adapterToYPos.get(adapterCallRowModel.adapterName.get())+20, inputEndPositions, adapterCallRowModel.workflowInstanceIdCaller.get());
		}
		final LinkedList<TimeIdPair> outputEndPositions = new LinkedList<TimeIdPair>();
		for (final AdapterLaunchRowModel adapterLaunchRowModel: adapterOutputLaunch){
			createOutputAnnimationEvent(keyFrames,timeline, adapterLaunchRowModel.timestamp.get().getTime()-min,
					adapterLaunchRowModel.workflowname.get(),adapterToYPos.get(adapterLaunchRowModel.adapterName.get())+80,outputEndPositions,ADAPTER_LAUNCH_COLOR);
		}
		for (final AdapterNotifyRowModel adapterNotifyRowModel: adapterOutputNotify){
			createOutputAnnimationEvent(keyFrames,timeline, adapterNotifyRowModel.timestamp.get().getTime()-min,
					adapterNotifyRowModel.correlationId.get(),adapterToYPos.get(adapterNotifyRowModel.adapterName.get())+80,outputEndPositions,ADAPTER_NOTIFY_COLOR);
		}
		timeline.getKeyFrames().addAll(keyFrames);
    }
    
	private void createLegend(){
		VBox pane = new VBox();
//		pane.setScaleX(0.5);
//		pane.setScaleY(0.5);
		pane.getChildren().add(new Label("Legend"));
		pane.getChildren().add(createLegendEntry("adapter", ADAPTER_COLOR));
		pane.getChildren().add(createLegendEntry("adapter method call", ADAPTER_CALL_COLOR));
		pane.getChildren().add(createLegendEntry("notify correlation id ", ADAPTER_NOTIFY_COLOR));
		pane.getChildren().add(createLegendEntry("workflow launch", ADAPTER_LAUNCH_COLOR));
		pane.getChildren().add(createLegendEntry("workflow instance", WORKFLOW_COLOR));
		pane.setStyle("-fx-border-color: black; -fx-border-width: 1;");
		pane.setTranslateX(3);
		pane.setTranslateY(annimationPane.getHeight()-150);
		annimationPane.getChildren().add(pane);
		
	}
	
	private Node createLegendEntry(String text, Color color){
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.setSpacing(3);
		hbox.getChildren().add(new Rectangle(15,15,color));
		hbox.getChildren().add(new Label(text));
		VBox.setMargin(hbox, new Insets(1.5,3,1.5,3));
		return hbox;
	}
    
    private void addStaticContent(HashMap<String,Double> adapterToYPos) {
    	createLegend();
    	
    	
		for (Entry<String,Double> entry: adapterToYPos.entrySet()){
			createAdapter(entry.getKey(),getAnnimationPaneWidth()/2,entry.getValue());
		}
		
		final Text inputText = new Text("Input");
		inputText.setFontSmoothingType(FontSmoothingType.LCD);
		inputText.setX(getAnnimationPaneWidth()/2-getAnnimationPaneWidth()/4-inputText.getBoundsInLocal().getWidth()/2);
		inputText.setY(20);
		annimationPane.getChildren().add(inputText);
		
		final Text outputText = new Text("Output");
		outputText.setFontSmoothingType(FontSmoothingType.LCD);
		outputText.setX(getAnnimationPaneWidth()/2+getAnnimationPaneWidth()/4-inputText.getBoundsInLocal().getWidth()/2);
		outputText.setY(20);
		annimationPane.getChildren().add(outputText);
		
		final Text adapterText = new Text("Adapter");
		adapterText.setFontSmoothingType(FontSmoothingType.LCD);
		adapterText.setX(getAnnimationPaneWidth()/2-inputText.getBoundsInLocal().getWidth()/2);
		adapterText.setTranslateY(20);
		annimationPane.getChildren().add(adapterText);
		
		Line lineInput = new Line();
		lineInput.getStrokeDashArray().addAll(5d);
		lineInput.setCache(true);
		lineInput.startXProperty().set(getAnnimationPaneWidth()/2-getAnnimationPaneWidth()/8);
		lineInput.endXProperty().set(lineInput.startXProperty().get());
		lineInput.startYProperty().set(0);
		lineInput.endYProperty().bind(annimationPane.heightProperty());
		annimationPane.getChildren().add(lineInput);
		
		Line lineOutput = new Line();
		lineOutput.getStrokeDashArray().addAll(5d);
		lineOutput.setCache(true);
		lineOutput.startXProperty().set(getAnnimationPaneWidth()/2+getAnnimationPaneWidth()/8);
		lineOutput.endXProperty().set(lineOutput.startXProperty().get());
		lineOutput.startYProperty().set(0);
		lineOutput.endYProperty().bind(annimationPane.heightProperty());
		annimationPane.getChildren().add(lineOutput);
		
	}
	private void createAdapter(String name, double xpos, double ypos) {
		final Rectangle adapterRectangle = new Rectangle(150,150);
		adapterRectangle.setFill(ADAPTER_COLOR);
		adapterRectangle.setX(xpos-adapterRectangle.getWidth()/2);
		adapterRectangle.setY(ypos);
		adapterRectangle.setArcHeight(25);
		adapterRectangle.setArcWidth(25);
		final Text adapterText = new Text(name);
		adapterText.setFontSmoothingType(FontSmoothingType.LCD);
		adapterText.setX(xpos-adapterText.getBoundsInLocal().getWidth()/2);
		adapterText.setY(ypos-5);
		annimationPane.getChildren().add(adapterRectangle);
		annimationPane.getChildren().add(adapterText);
	}

	private void createAnnimation(ArrayList<KeyFrame> keyFrames, long startTimeMs, double startx, double starty, double endx, double endy,
			final Rectangle rectangle, final Text text, final  Node... additionalNodes){
		KeyValue keyValueStartX = new KeyValue(rectangle.translateXProperty(), startx);
		KeyValue keyValueStartY = new KeyValue(rectangle.translateYProperty(), starty);
		KeyValue keyValueEndX = new KeyValue(rectangle.translateXProperty(),endx);
		KeyValue keyValueEndY = new KeyValue(rectangle.translateYProperty(), endy);
		
		text.opacityProperty().bind(rectangle.opacityProperty());
		for (Node node: additionalNodes){
			node.opacityProperty().bind(rectangle.opacityProperty());
		}
		
		KeyFrame keyFrame1 = new KeyFrame(Duration.millis(Math.max(0,startTimeMs)),
					new EventHandler<ActionEvent>(){
						@Override
						public void handle(ActionEvent event) {
							for (Node node: additionalNodes){
								annimationPane.getChildren().add(node);
							}
							annimationPane.getChildren().add(rectangle);
							annimationPane.getChildren().add(text);
						}
			        },new KeyValue(rectangle.opacityProperty(), 0));
		KeyFrame keyFrame2 = new KeyFrame(Duration.millis(startTimeMs), keyValueStartX, keyValueStartY);
		KeyFrame keyFrame3 = new KeyFrame(Duration.millis(startTimeMs+FADEDURATION), new KeyValue(rectangle.opacityProperty(), 1));
		KeyFrame keyFrame4 = new KeyFrame(Duration.millis(startTimeMs+FADEDURATION), keyValueStartX, keyValueStartY);
		KeyFrame keyFrame5 = new KeyFrame(Duration.millis(startTimeMs+FADEDURATION+MOVEDURATION),keyValueEndX,keyValueEndY);
		KeyFrame keyFrame6 = new KeyFrame(Duration.millis(startTimeMs+FADEDURATION+MOVEDURATION), new KeyValue(rectangle.opacityProperty(), 1));
		KeyFrame keyFrame7 = new KeyFrame(Duration.millis(startTimeMs+FADEDURATION+MOVEDURATION+FADEDURATION), 
				new EventHandler<ActionEvent>(){
					@Override
					public void handle(ActionEvent event) {
						for (Node node: additionalNodes){
							annimationPane.getChildren().remove(node);
						}
						annimationPane.getChildren().remove(rectangle);
						annimationPane.getChildren().remove(text);
					}
		        },new KeyValue(rectangle.opacityProperty(), 0));
		
		
		keyFrames.add(keyFrame1);
		keyFrames.add(keyFrame2);
		keyFrames.add(keyFrame3);
		keyFrames.add(keyFrame4);
		keyFrames.add(keyFrame5);
		keyFrames.add(keyFrame6);
		keyFrames.add(keyFrame7);
		
		timeline.statusProperty().addListener(new ChangeListener<Status>() {
			@Override
			public void changed(ObservableValue<? extends Status> observable, Status oldValue, Status newValue) {
				if (newValue==Status.STOPPED){//clean up when annimation stpped
					for (Node node: additionalNodes){
						annimationPane.getChildren().remove(node);
					}
					annimationPane.getChildren().remove(rectangle);
					annimationPane.getChildren().remove(text);
				}
			}
		});
	}

	private void createInputAnnimationEvent(ArrayList<KeyFrame> keyFrames, final Timeline timeline, long startTimeMs, final String displaytext, double yend, List<TimeIdPair> previousPositions, String workflowId) {		
		final Rectangle rectangle = new Rectangle(125,50);
		rectangle.setFill(ADAPTER_CALL_COLOR);
		rectangle.setArcHeight(25);
		rectangle.setArcWidth(25);
		final Text text = new Text(displaytext);
		text.setFontSmoothingType(FontSmoothingType.LCD);
		text.translateXProperty().bind(rectangle.translateXProperty());
		text.translateYProperty().bind(rectangle.translateYProperty().add(rectangle.getHeight()/2).add(text.getBoundsInLocal().getHeight()/4));

		double ystart= getYPositionWithExistingList(previousPositions,startTimeMs,displaytext);
		if (ystart==Double.MIN_VALUE){//limit
			return;
		}
		
		final Rectangle workflowRectangle = new Rectangle(150,80);
		workflowRectangle.setFill(WORKFLOW_COLOR);
		workflowRectangle.setX(getAnnimationPaneWidth()/2-getAnnimationPaneWidth()/4-workflowRectangle.getWidth()/2);
		workflowRectangle.setY(ystart);
		workflowRectangle.setArcHeight(25);
		workflowRectangle.setArcWidth(25);
		final Text workflowText = new Text(workflowId);
		workflowText.setFontSmoothingType(FontSmoothingType.LCD);
		workflowText.translateXProperty().bind(annimationPane.widthProperty().divide(2).subtract(annimationPane.widthProperty().divide(4)).subtract(workflowText.getBoundsInLocal().getWidth()/2));
		workflowText.translateYProperty().bind(workflowRectangle.yProperty().subtract(5));
		
		createAnnimation(keyFrames,startTimeMs-FADEDURATION-MOVEDURATION-FADEDURATION,
				getAnnimationPaneWidth()/2-(getAnnimationPaneWidth()/4)-(rectangle.getLayoutBounds().getWidth()/2),
				ystart+20,
				getAnnimationPaneWidth()/2-rectangle.getWidth()/2,
				yend,
				rectangle,
				text,
				workflowRectangle,workflowText);
	}
	
	private double getAnnimationPaneWidth(){
		final double width = annimationPane.getWidth();
		return width;
	}
	
	private double getYPositionWithExistingList(List<TimeIdPair> previousPositions,long startTimeMs, String displaytext){
		Iterator<TimeIdPair> iterator = previousPositions.iterator();
		while (iterator.hasNext()){
			if (iterator.next().time<startTimeMs-TOTAL_ANNIMATION_TIME){
				iterator.remove();
			}
		}
		TimeIdPair containing=null;
		for (int i = previousPositions.size()-1; i >=0 ; i--) {
			TimeIdPair imeIdPair = previousPositions.get(i);
			if (imeIdPair.id.equals(displaytext)){
				containing=imeIdPair;
				break;
			}
		}
		if (containing==null){
			previousPositions.add(new TimeIdPair(startTimeMs,displaytext));
		} else {
			if ((containing.time-startTimeMs)<20){
				return Double.MIN_VALUE;
			}
		}
		return 50+(previousPositions.size()-1)*150d;
	}

	private void createOutputAnnimationEvent(ArrayList<KeyFrame> keyFrames, final Timeline timeline, long startTimeMs,
			final String displaytext, double ystart, List<TimeIdPair> outputEndPositions, Color color) {		
		final Rectangle rectangle = new Rectangle(125,50);
		rectangle.setFill(color);
		rectangle.setArcHeight(25);
		rectangle.setArcWidth(25);
		final Text text = new Text(displaytext);
		text.translateXProperty().bind(rectangle.translateXProperty());
		text.translateYProperty().bind(rectangle.translateYProperty().add(rectangle.getHeight()/2).add(text.getBoundsInLocal().getHeight()/4));
		
		double yend= getYPositionWithExistingList(outputEndPositions,startTimeMs,displaytext);
		if (yend==Double.MIN_VALUE){//limit
			return;
		}
		createAnnimation(keyFrames,startTimeMs,
				getAnnimationPaneWidth()/2-rectangle.getWidth()/2,
				ystart,
				getAnnimationPaneWidth()/2+(getAnnimationPaneWidth()/4)-(rectangle.getLayoutBounds().getWidth()/2),
				yend,
				rectangle,
				text);
	}
	
	private static class TimeIdPair{
    	public final long time;
    	public final String id;
		public TimeIdPair(long time, String id) {
			super();
			this.time = time;
			this.id = id;
		}
    }

}
