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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

import com.google.common.base.Optional;

public class AdapterAnnimationCreator {
	
	private static final int EVENT_HEIGHT = 35;
	private static final int ADAPTER_HEIGHT = 100;
	private static final int EVENT_WIDTH = 110;
	private static final int ADAPTER_WIDTH = 150;
	private static final Color WORKFLOW_COLOR = Color.GOLD;
	private static final Color ADAPTER_NOTIFY_COLOR = Color.CORNFLOWERBLUE;
	private static final Color ADAPTER_CALL_COLOR = Color.CORAL;
	private static final Color ADAPTER_COLOR = Color.LIGHTGREEN;
	private static final Color ADAPTER_LAUNCH_COLOR = Color.SADDLEBROWN; 
	public long FADEDURATION=300;
	public long MOVEDURATION=1600;
	public long DEFAULT_TOTAL_ANNIMATION_TIME=FADEDURATION+MOVEDURATION+FADEDURATION;
	
	private enum AnnimationContentTyp{
		ADAPTER,CALL,NOTIFY,LAUNCH, WORKFLOW
	}
	
	private class Annimation{
		public long startTime;
		public long endTime;
		public Node node;
		public String id;
		
		public double startx;
		public double starty;
		public double endx;
		public double endy;
		public AnnimationContentTyp contentTyp;

		
		public Annimation(long startTime, long endTime, Node node, String id, double startx, double starty, double endx, double endy,AnnimationContentTyp contentTyp) {
			super();
			this.startTime = startTime;
			this.endTime = endTime;
			this.node = node;
			this.id = id;
			this.startx = startx;
			this.starty = starty;
			this.endx = endx;
			this.endy = endy;
			this.contentTyp = contentTyp;
		}
	}
	
	private long min;
	
    public long getMin() {
		return min;
	}
	public void setMin(long min) {
		this.min = min;
	}
	
	Pane annimationPane;
	Timeline timeline;
	private ArrayList<Annimation> annimations;
	
	public AdapterAnnimationCreator(Pane annimationPane, Timeline timeline) {
		super();
		this.annimationPane = annimationPane;
		this.timeline = timeline;
	}
	
	
	private Optional<Annimation> searchAnnimationRunningAt( String id, long startTime, long endTime){
		Annimation result=null;
		for (Annimation annimation: annimations){
			if (annimation.id.equals(id) && !(endTime<annimation.startTime || startTime>annimation.endTime)){
				result=annimation;
				break;
			}
		}
		return Optional.fromNullable(result);
	}
	
	private List<Annimation> searchAnnimationWithType(AnnimationContentTyp contentTyp, long startTime, long endTime){
		List<Annimation> result=new ArrayList<Annimation>();
		for (Annimation annimation: annimations){
			if (annimation.contentTyp==contentTyp && !(endTime<annimation.startTime || startTime>annimation.endTime)){
				result.add(annimation);
			}
		}
		return result;
	}
	
	private void addAdapterAnnimation(String adapterName, long time){
		Optional<Annimation> annimationOpt =  searchAnnimationRunningAt(adapterName,time,time+DEFAULT_TOTAL_ANNIMATION_TIME);
		if (annimationOpt.isPresent()){
			annimationOpt.get().endTime=time+DEFAULT_TOTAL_ANNIMATION_TIME;
		} else {
			Pane pane = new Pane();
			final Rectangle adapterRectangle = new Rectangle(ADAPTER_WIDTH,ADAPTER_HEIGHT);
			adapterRectangle.setFill(ADAPTER_COLOR);
			adapterRectangle.setArcHeight(25);
			adapterRectangle.setArcWidth(25);
			final Text adapterText = new Text(adapterName);
			adapterText.setFontSmoothingType(FontSmoothingType.LCD);
			adapterText.xProperty().bind(adapterRectangle.xProperty().add(adapterRectangle.getWidth()/2).subtract(adapterText.getBoundsInLocal().getWidth()/2));
			adapterText.yProperty().bind(adapterRectangle.yProperty().subtract(5));
			pane.getChildren().add(adapterRectangle);
			pane.getChildren().add(adapterText);

			Optional<Double> ypos = getFreeYslot(time, time+DEFAULT_TOTAL_ANNIMATION_TIME,ADAPTER_HEIGHT+20,false,AnnimationContentTyp.ADAPTER);
			if (ypos.isPresent()){
				double xpos = annimationPane.getWidth()/2-adapterRectangle.getWidth()/2;
				
				annimations.add(new Annimation(time, time+DEFAULT_TOTAL_ANNIMATION_TIME, pane, adapterName,
						xpos,
						ypos.get(),
						xpos, 
						ypos.get(),
						AnnimationContentTyp.ADAPTER));
			}
		}
	}
	
	private Optional<Double> getFreeYslot(long starttime, long endtime,  double slotHeight, boolean useEndPos, AnnimationContentTyp... types){
		final List<Annimation> foundAnnimations = new ArrayList<Annimation>();
		for (AnnimationContentTyp type: types){
			foundAnnimations.addAll(searchAnnimationWithType(type, starttime, endtime));
		}
		for (int i=0;i<20;i++){
			double ypos = 65+(slotHeight)*i;
			boolean posInlist=false;
			for (Annimation annimation: foundAnnimations){
				if (useEndPos){
					if (Math.abs(annimation.endy-ypos)<0.0001){
						posInlist=true;
					}
				} else {
					if (Math.abs(annimation.starty-ypos)<0.0001){
						posInlist=true;
					}
				}
			}
			if (!posInlist){
				return Optional.of(ypos);
			} 
		}
		return Optional.absent();
	}
	
	private void addOutputEventAnnimation(long time, String id, AnnimationContentTyp annimationContentTyp, String adapterId){
		Optional<Double> ypos = getFreeYslot(time,time+DEFAULT_TOTAL_ANNIMATION_TIME,60,true,AnnimationContentTyp.NOTIFY,AnnimationContentTyp.LAUNCH);
		if (ypos.isPresent()){
			Optional<Annimation> adapterAnnimation =  searchAnnimationRunningAt(adapterId,time,time+DEFAULT_TOTAL_ANNIMATION_TIME);
			if (adapterAnnimation.isPresent()){
				addEventAnnimation(time,id,annimationContentTyp,
						adapterAnnimation.get().startx+ADAPTER_WIDTH/2,
						adapterAnnimation.get().starty+ADAPTER_HEIGHT-EVENT_HEIGHT-5,
						getAnnimationPaneWidth()/2+getAnnimationPaneWidth()/4,
						ypos.get());
			}
		}
	}
	
	private void addInputEventAnnimation(long time, String id, AnnimationContentTyp annimationContentTyp, String adapterId, String workflowInstanceId){
		double ypos = searchAnnimationRunningAt(workflowInstanceId, time, time+DEFAULT_TOTAL_ANNIMATION_TIME).get().starty+5;

		Optional<Annimation> adapterAnnimation =  searchAnnimationRunningAt(adapterId,time,time+DEFAULT_TOTAL_ANNIMATION_TIME);
		if (adapterAnnimation.isPresent()){
			addEventAnnimation(time,id,annimationContentTyp,
					getAnnimationPaneWidth()/2-getAnnimationPaneWidth()/4,
					ypos,
					adapterAnnimation.get().startx+ADAPTER_WIDTH/2,
					adapterAnnimation.get().starty+5);
		}
	}
	
	private void addEventAnnimation(long time, String id, AnnimationContentTyp annimationContentTyp,
			double startx, double starty, double endx, double endy){
		
		Pane pane = new Pane();
		final Rectangle rectangle = new Rectangle(EVENT_WIDTH,EVENT_HEIGHT);
		rectangle.setFill(getEventColor(annimationContentTyp));
		rectangle.setArcHeight(20);
		rectangle.setArcWidth(20);
		final Text text = new Text(id);
		text.setFontSmoothingType(FontSmoothingType.LCD);
		text.translateXProperty().bind(rectangle.translateXProperty());
		text.translateYProperty().bind(rectangle.translateYProperty().add(rectangle.getHeight()/2).add(text.getBoundsInLocal().getHeight()/4));
		pane.getChildren().add(rectangle);
		pane.getChildren().add(text);

		annimations.add(new Annimation(time, time+DEFAULT_TOTAL_ANNIMATION_TIME, pane, id,
				startx-rectangle.getWidth()/2, starty, endx-rectangle.getWidth()/2, endy,
					annimationContentTyp));
	}
	
	private Color getEventColor(AnnimationContentTyp annimationContentTyp){
		if (annimationContentTyp==AnnimationContentTyp.CALL){
			return ADAPTER_CALL_COLOR;
		}
		if (annimationContentTyp==AnnimationContentTyp.LAUNCH){
			return ADAPTER_LAUNCH_COLOR;
		}
		if (annimationContentTyp==AnnimationContentTyp.NOTIFY){
			return ADAPTER_NOTIFY_COLOR;
		}
		return null;
	}
	
	private class TimeValuePair<T>{
		long time;
		T value;
		public TimeValuePair(T value, long time) {
			super();
			this.time = time;
			this.value = value;
		}
	}
	
	public void create( 
    		ObservableList<AdapterCallRowModel> adapterInput,
    		ObservableList<AdapterLaunchRowModel> adapterOutputLaunch,
    		ObservableList<AdapterNotifyRowModel> adapterOutputNotify,
    		Slider positionSlider){
		
		annimations = new ArrayList<Annimation>();

    	min = Long.MAX_VALUE;
		for (final AdapterCallRowModel adapterCallRowModel: adapterInput){
			min = Math.min(min, adapterCallRowModel.timestamp.get().getTime()-DEFAULT_TOTAL_ANNIMATION_TIME);
		}
		for (final AdapterLaunchRowModel adapterLaunchRowModel: adapterOutputLaunch){
			min = Math.min(min, adapterLaunchRowModel.timestamp.get().getTime());
		}
		for (final AdapterNotifyRowModel adapterNotifyRowModel: adapterOutputNotify){
			min = Math.min(min, adapterNotifyRowModel.timestamp.get().getTime());
		}
		
		
		
		ArrayList<TimeValuePair<String>> timeAdapterPairs = new ArrayList<TimeValuePair<String>>();
		for (final AdapterCallRowModel adapterCallRowModel: adapterInput){
			final long time = adapterCallRowModel.timestamp.get().getTime()-DEFAULT_TOTAL_ANNIMATION_TIME;
			timeAdapterPairs.add(new TimeValuePair<String>(adapterCallRowModel.adapterName.get(),time));
			addWorkflowAnnimation(adapterCallRowModel.workflowClassCaller.get(), adapterCallRowModel.workflowInstanceIdCaller.get(), time);
		}
		for (final AdapterLaunchRowModel adapterLaunchRowModel: adapterOutputLaunch){
			final long time = adapterLaunchRowModel.timestamp.get().getTime();
			timeAdapterPairs.add(new TimeValuePair<String>(adapterLaunchRowModel.adapterName.get(),time));
		}
		for (final AdapterNotifyRowModel adapterNotifyRowModel: adapterOutputNotify){
			final long time = adapterNotifyRowModel.timestamp.get().getTime();
			timeAdapterPairs.add(new TimeValuePair<String>(adapterNotifyRowModel.adapterName.get(),time));
		}
		Collections.sort(timeAdapterPairs, new Comparator<TimeValuePair<String>>(){
			@Override
			public int compare(TimeValuePair<String> o1, TimeValuePair<String> o2) {
				//TODO replace when switch to java 1.7 with: Long.compare(o1.time, o2.time);
				return Long.valueOf(o1.time).compareTo(Long.valueOf(o2.time));
			}
		});
		for (TimeValuePair<String> timeAdapterPair: timeAdapterPairs){
			addAdapterAnnimation(timeAdapterPair.value,timeAdapterPair.time);
		}
		

		

		addStaticContent();
		
		
		for (final AdapterCallRowModel adapterCallRowModel: adapterInput){
			addInputEventAnnimation(
					adapterCallRowModel.timestamp.get().getTime()-DEFAULT_TOTAL_ANNIMATION_TIME,
					adapterCallRowModel.method.get(),
					AnnimationContentTyp.CALL,
					adapterCallRowModel.adapterName.get(),
					adapterCallRowModel.workflowInstanceIdCaller.get());
		}
		for (final AdapterLaunchRowModel adapterLaunchRowModel: adapterOutputLaunch){
			addOutputEventAnnimation(
					adapterLaunchRowModel.timestamp.get().getTime(),
					adapterLaunchRowModel.workflowname.get(),
					AnnimationContentTyp.LAUNCH,
					adapterLaunchRowModel.adapterName.get());
		}
		for (final AdapterNotifyRowModel adapterNotifyRowModel: adapterOutputNotify){
			addOutputEventAnnimation(
					adapterNotifyRowModel.timestamp.get().getTime(),
					adapterNotifyRowModel.correlationId.get(),
					AnnimationContentTyp.NOTIFY,
					adapterNotifyRowModel.adapterName.get());
		}
		
		ArrayList<TimeValuePair<Object>> outputsSorted = new ArrayList<TimeValuePair<Object>>();
		for (final AdapterLaunchRowModel adapterLaunchRowModel: adapterOutputLaunch){
			outputsSorted.add(new TimeValuePair<Object>(adapterLaunchRowModel, adapterLaunchRowModel.timestamp.get().getTime()));
		}
		for (final AdapterNotifyRowModel adapterNotifyRowModel: adapterOutputNotify){
			outputsSorted.add(new TimeValuePair<Object>(adapterNotifyRowModel, adapterNotifyRowModel.timestamp.get().getTime()));
		}
		Collections.sort(outputsSorted, new Comparator<TimeValuePair<Object>>(){
			@Override
			public int compare(TimeValuePair<Object> o1, TimeValuePair<Object> o2) {
				return Long.compare(o1.time, o2.time);
			}
		});
		for (TimeValuePair<Object> output: outputsSorted){
			if (output.value instanceof AdapterLaunchRowModel){
				AdapterLaunchRowModel adapterLaunchRowModel = (AdapterLaunchRowModel)output.value;
				addOutputEventAnnimation(
						adapterLaunchRowModel.timestamp.get().getTime(),
						adapterLaunchRowModel.workflowname.get(),
						AnnimationContentTyp.LAUNCH,
						adapterLaunchRowModel.adapterName.get());
			}
			if (output.value instanceof AdapterNotifyRowModel){
				AdapterNotifyRowModel adapterNotifyRowModel = (AdapterNotifyRowModel)output.value;
				addOutputEventAnnimation(
						adapterNotifyRowModel.timestamp.get().getTime(),
						adapterNotifyRowModel.correlationId.get(),
						AnnimationContentTyp.NOTIFY,
						adapterNotifyRowModel.adapterName.get());
			}
		}
		
		
		
		
		
		ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>();//Performance optimisation adding single keyframes directly is too slow
		for (Annimation annimation: annimations){
			addAnnimation(keyFrames, annimation, min);
		}
		timeline.getKeyFrames().addAll(keyFrames);
		
		positionSlider.setMax(timeline.getTotalDuration().toMillis());
    }
    
	private void addWorkflowAnnimation(String workflowClass, String workflowInstanceId, long time) {
		Optional<Annimation> annimationOpt = searchAnnimationRunningAt(workflowInstanceId,time,time+DEFAULT_TOTAL_ANNIMATION_TIME);
		if (annimationOpt.isPresent()){
			annimationOpt.get().endTime=time+DEFAULT_TOTAL_ANNIMATION_TIME;
		} else {
			Pane pane = new Pane();
			final Rectangle workflowRectangle = new Rectangle(EVENT_WIDTH+20,EVENT_HEIGHT+15);
			workflowRectangle.setFill(WORKFLOW_COLOR);
			workflowRectangle.setArcHeight(25);
			workflowRectangle.setArcWidth(25);
			final Text classText = new Text(workflowClass);
			classText.setFontSmoothingType(FontSmoothingType.LCD);
			classText.xProperty().bind(workflowRectangle.xProperty().add(workflowRectangle.getWidth()/2).subtract(classText.getBoundsInLocal().getWidth()/2));
			classText.yProperty().bind(workflowRectangle.yProperty().subtract(18));
			final Text instanceIdText = new Text(workflowInstanceId);
			instanceIdText.setFontSmoothingType(FontSmoothingType.LCD);
			instanceIdText.xProperty().bind(workflowRectangle.xProperty().add(workflowRectangle.getWidth()/2).subtract(classText.getBoundsInLocal().getWidth()/2));
			instanceIdText.yProperty().bind(workflowRectangle.yProperty().subtract(3));
			pane.getChildren().add(workflowRectangle);
			pane.getChildren().add(classText);
			pane.getChildren().add(instanceIdText);

			Optional<Double> ypos = getFreeYslot(time, time+DEFAULT_TOTAL_ANNIMATION_TIME,EVENT_HEIGHT+15+35,false,AnnimationContentTyp.WORKFLOW);
			if (ypos.isPresent()){
				double xpos = annimationPane.getWidth()/2-annimationPane.getWidth()/4-workflowRectangle.getWidth()/2;
				
				annimations.add(new Annimation(time, time+DEFAULT_TOTAL_ANNIMATION_TIME, pane, workflowInstanceId,
						xpos,
						ypos.get(),
						xpos, 
						ypos.get(),
						AnnimationContentTyp.WORKFLOW));
			}
		}
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
    
    private void addStaticContent() {
    	createLegend();
   
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

	private void addAnnimation(ArrayList<KeyFrame> keyFrames, final Annimation annimation, long minTime){
		long startTimeMs = annimation.startTime-minTime;
		long endTimeMs = annimation.endTime-minTime;
		
		KeyValue keyValueStartX = new KeyValue(annimation.node.translateXProperty(), annimation.startx);
		KeyValue keyValueStartY = new KeyValue(annimation.node.translateYProperty(), annimation.starty);
		KeyValue keyValueEndX = new KeyValue(annimation.node.translateXProperty(),annimation.endx);
		KeyValue keyValueEndY = new KeyValue(annimation.node.translateYProperty(), annimation.endy);
		

		KeyFrame keyFrame1 = new KeyFrame(Duration.millis(startTimeMs),
					new EventHandler<ActionEvent>(){
						@Override
						public void handle(ActionEvent event) {
							annimationPane.getChildren().add(annimation.node);
						}
			        },new KeyValue(annimation.node.opacityProperty(), 0));
		KeyFrame keyFrame2 = new KeyFrame(Duration.millis(startTimeMs), keyValueStartX, keyValueStartY);
		KeyFrame keyFrame3 = new KeyFrame(Duration.millis(startTimeMs+FADEDURATION),new KeyValue(annimation.node.opacityProperty(), 1));
		KeyFrame keyFrame4 = new KeyFrame(Duration.millis(startTimeMs+FADEDURATION), keyValueStartX, keyValueStartY);
		KeyFrame keyFrame5 = new KeyFrame(Duration.millis(endTimeMs-FADEDURATION),keyValueEndX,keyValueEndY);
		KeyFrame keyFrame6 = new KeyFrame(Duration.millis(endTimeMs-FADEDURATION), new KeyValue(annimation.node.opacityProperty(), 1));
		KeyFrame keyFrame7 = new KeyFrame(Duration.millis(endTimeMs), 
				new EventHandler<ActionEvent>(){
					@Override
					public void handle(ActionEvent event) {
						annimationPane.getChildren().remove(annimation.node);
					}
		        },new KeyValue(annimation.node.opacityProperty(), 0));
		
		
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
					annimationPane.getChildren().remove(annimation.node);
				}
			}
		});
	}

	private double getAnnimationPaneWidth(){
		final double width = annimationPane.getWidth();
		return width;
	}

}
