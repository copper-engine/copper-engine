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
package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.annimation;

import java.util.ArrayList;
import java.util.Arrays;
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

import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.AdapterCallRowModel;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.AdapterLaunchRowModel;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.AdapterNotifyRowModel;

public class AdapterAnnimationCreator {
	
	public long FADEDURATION=300;
	public long MOVEDURATION=1600;
	public long DEFAULT_TOTAL_ANNIMATION_TIME=FADEDURATION+MOVEDURATION+FADEDURATION;
	

	private long min;
	
    public long getMin() {
		return min;
	}
	public void setMin(long min) {
		this.min = min;
	}
	
	Pane annimationPane;
	Timeline timeline;
	private ArrayList<AnnimationPartBase> annimations;
	
	public AdapterAnnimationCreator(Pane annimationPane, Timeline timeline) {
		super();
		this.annimationPane = annimationPane;
		this.timeline = timeline;
	}
	
	
	private Optional<AnnimationPartBase> searchAnnimationRunningAt( String id, long startTime, long endTime){
		AnnimationPartBase result=null;
		for (AnnimationPartBase annimation: annimations){
			if (annimation.id.equals(id) && !(endTime<annimation.startTime || startTime>annimation.endTime)){
				result=annimation;
				break;
			}
		}
		return Optional.fromNullable(result);
	}
	
	private List<AnnimationPartBase> searchAnnimationWithType(Class<? extends AnnimationPartBase> clazz, long startTime, long endTime){
		List<AnnimationPartBase> result=new ArrayList<AnnimationPartBase>();
		for (AnnimationPartBase annimation: annimations){
			if (annimation.getClass()==clazz && !(endTime<annimation.startTime || startTime>annimation.endTime)){
				result.add(annimation);
			}
		}
		return result;
	}
	
	private void addAdapterAnnimation(String adapterName, long time){
		Optional<AnnimationPartBase> annimationOpt =  searchAnnimationRunningAt(adapterName,time,time+DEFAULT_TOTAL_ANNIMATION_TIME);
		if (annimationOpt.isPresent()){
			annimationOpt.get().endTime=time+DEFAULT_TOTAL_ANNIMATION_TIME;
		} else {
			Optional<Double> ypos = getFreeYslot(time, time+DEFAULT_TOTAL_ANNIMATION_TIME,AdapterAnnimation.ADAPTER_HEIGHT+20,false,Arrays.<Class<? extends AnnimationPartBase>>asList(AdapterAnnimation.class));
			if (ypos.isPresent()){
				double xpos = annimationPane.getWidth()/2-AdapterAnnimation.ADAPTER_WIDTH/2;
				
				annimations.add(new AdapterAnnimation(new AnnimationPartParameter(time, time+DEFAULT_TOTAL_ANNIMATION_TIME, adapterName,
						xpos,
						ypos.get(),
						xpos, 
						ypos.get())));
			}
		}
	}
	
	private Optional<Double> getFreeYslot(long starttime, long endtime,  double slotHeight, boolean useEndPos, List<Class<? extends AnnimationPartBase>>  types){
		final List<AnnimationPartBase> foundAnnimations = new ArrayList<AnnimationPartBase>();
		for (Class<? extends AnnimationPartBase> type: types){
			foundAnnimations.addAll(searchAnnimationWithType(type, starttime, endtime));
		}
		for (int i=0;i<20;i++){
			double ypos = 65+(slotHeight)*i;
			boolean posInlist=false;
			for (AnnimationPartBase annimation: foundAnnimations){
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

	
	private void addNotifyEventAnnimation(long time, String id, String adapterId){
		Optional<Double> ypos = getFreeYslot(time,time+DEFAULT_TOTAL_ANNIMATION_TIME,60,true,Arrays.<Class<? extends AnnimationPartBase>>asList(NotifyAnnimation.class,LaunchAnnimation.class));
		if (ypos.isPresent()){
			Optional<AnnimationPartBase> adapterAnnimation =  searchAnnimationRunningAt(adapterId,time,time+DEFAULT_TOTAL_ANNIMATION_TIME);
			if (adapterAnnimation.isPresent()){
				annimations.add(new NotifyAnnimation(createOutputParameter(time, id, ypos, adapterAnnimation)));
			}
		}
	}
	private AnnimationPartParameter createOutputParameter(long time, String id, Optional<Double> ypos,
			Optional<AnnimationPartBase> adapterAnnimation) {
		return new AnnimationPartParameter(time, time+DEFAULT_TOTAL_ANNIMATION_TIME,id,
				adapterAnnimation.get().startx+AdapterAnnimation.ADAPTER_WIDTH/2-EventAnnimationBase.EVENT_WIDTH/2,
				adapterAnnimation.get().starty+AdapterAnnimation.ADAPTER_HEIGHT-EventAnnimationBase.EVENT_HEIGHT-5,
				getAnnimationPaneWidth()/2+getAnnimationPaneWidth()/4-EventAnnimationBase.EVENT_WIDTH/2,
				ypos.get());
	}
	
	private void addLaunchEventAnnimation(long time, String id, String adapterId){
		Optional<Double> ypos = getFreeYslot(time,time+DEFAULT_TOTAL_ANNIMATION_TIME,60,true,Arrays.<Class<? extends AnnimationPartBase>>asList(NotifyAnnimation.class,LaunchAnnimation.class));
		if (ypos.isPresent()){
			Optional<AnnimationPartBase> adapterAnnimation =  searchAnnimationRunningAt(adapterId,time,time+DEFAULT_TOTAL_ANNIMATION_TIME);
			if (adapterAnnimation.isPresent()){
				annimations.add(new LaunchAnnimation(createOutputParameter(time, id, ypos, adapterAnnimation)));
			}
		}
	}
	
	private void addCallEventAnnimation(long time, String id, String adapterId, String workflowInstanceId){
		double ypos = searchAnnimationRunningAt(workflowInstanceId, time, time+DEFAULT_TOTAL_ANNIMATION_TIME).get().starty+5;

		Optional<AnnimationPartBase> adapterAnnimation = searchAnnimationRunningAt(adapterId,time,time+DEFAULT_TOTAL_ANNIMATION_TIME);
		Optional<AnnimationPartBase> sameCallAnnimation = searchAnnimationRunningAt(id,time,time+20);
		if (adapterAnnimation.isPresent()){
			if (sameCallAnnimation.isPresent()){
				((CallAnnimation)sameCallAnnimation.get()).count++;
			} else {
				annimations.add(new CallAnnimation(new AnnimationPartParameter(time, time+DEFAULT_TOTAL_ANNIMATION_TIME,id,
						getAnnimationPaneWidth()/2-getAnnimationPaneWidth()/4,
						ypos,
						adapterAnnimation.get().startx+AdapterAnnimation.ADAPTER_WIDTH/2-EventAnnimationBase.EVENT_WIDTH/2,
						adapterAnnimation.get().starty+5)));
			}
		}
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
		
		annimations = new ArrayList<AnnimationPartBase>();

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
			addCallEventAnnimation(
					adapterCallRowModel.timestamp.get().getTime()-DEFAULT_TOTAL_ANNIMATION_TIME,
					adapterCallRowModel.method.get(),
					adapterCallRowModel.adapterName.get(),
					adapterCallRowModel.workflowInstanceIdCaller.get());
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
				//TODO replace when switch to java 1.7 with: Long.compare(o1.time, o2.time);
				return Long.valueOf(o1.time).compareTo(Long.valueOf(o2.time));
			}
		});
		for (TimeValuePair<Object> output: outputsSorted){
			if (output.value instanceof AdapterLaunchRowModel){
				AdapterLaunchRowModel adapterLaunchRowModel = (AdapterLaunchRowModel)output.value;
				addLaunchEventAnnimation(
						adapterLaunchRowModel.timestamp.get().getTime(),
						adapterLaunchRowModel.workflowname.get(),
						adapterLaunchRowModel.adapterName.get());
			}
			if (output.value instanceof AdapterNotifyRowModel){
				AdapterNotifyRowModel adapterNotifyRowModel = (AdapterNotifyRowModel)output.value;
				addNotifyEventAnnimation(
						adapterNotifyRowModel.timestamp.get().getTime(),
						adapterNotifyRowModel.correlationId.get(),
						adapterNotifyRowModel.adapterName.get());
			}
		}
		
		
		
		
		
		ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>();//Performance optimization adding single keyframes directly is too slow
		for (AnnimationPartBase annimation: annimations){
			addAnnimation(keyFrames, annimation, min);
		}
		timeline.getKeyFrames().addAll(keyFrames);
		
		positionSlider.setMax(timeline.getTotalDuration().toMillis());
    }
    
	private void addWorkflowAnnimation(String workflowClass, String workflowInstanceId, long time) {
		Optional<AnnimationPartBase> annimationOpt = searchAnnimationRunningAt(workflowInstanceId,time,time+DEFAULT_TOTAL_ANNIMATION_TIME);
		if (annimationOpt.isPresent()){
			annimationOpt.get().endTime=time+DEFAULT_TOTAL_ANNIMATION_TIME;
		} else {
			Optional<Double> ypos = getFreeYslot(time, time+DEFAULT_TOTAL_ANNIMATION_TIME,EventAnnimationBase.EVENT_HEIGHT+15+35,false,Arrays.<Class<? extends AnnimationPartBase>>asList(WorkflowAnnimation.class));
			if (ypos.isPresent()){
				double xpos = annimationPane.getWidth()/2-annimationPane.getWidth()/4-WorkflowAnnimation.WIDTH/2;
				
				annimations.add(new WorkflowAnnimation(workflowClass,new AnnimationPartParameter(time,time+DEFAULT_TOTAL_ANNIMATION_TIME, workflowInstanceId,
						xpos,
						ypos.get(),
						xpos, 
						ypos.get())));
			}
		}
	}
	private void createLegend(){
		VBox pane = new VBox();
//		pane.setScaleX(0.5);
//		pane.setScaleY(0.5);
		pane.getChildren().add(new Label("Legend"));
		pane.getChildren().add(createLegendEntry("adapter", AdapterAnnimation.ADAPTER_COLOR));
		pane.getChildren().add(createLegendEntry("adapter method call", CallAnnimation.ADAPTER_CALL_COLOR));
		pane.getChildren().add(createLegendEntry("notify correlation id ", NotifyAnnimation.ADAPTER_NOTIFY_COLOR));
		pane.getChildren().add(createLegendEntry("workflow launch", LaunchAnnimation.ADAPTER_LAUNCH_COLOR));
		pane.getChildren().add(createLegendEntry("workflow instance", WorkflowAnnimation.WORKFLOW_COLOR));
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
		outputText.setX(getAnnimationPaneWidth()/2+getAnnimationPaneWidth()/4-outputText.getBoundsInLocal().getWidth()/2);
		outputText.setY(20);
		annimationPane.getChildren().add(outputText);
		
		final Text adapterText = new Text("Adapter");
		adapterText.setFontSmoothingType(FontSmoothingType.LCD);
		adapterText.setX(getAnnimationPaneWidth()/2-adapterText.getBoundsInLocal().getWidth()/2);
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

	private void addAnnimation(ArrayList<KeyFrame> keyFrames, final AnnimationPartBase annimation, long minTime){
		long startTimeMs = annimation.startTime-minTime;
		long endTimeMs = annimation.endTime-minTime;
		
		final Node node = annimation.createVisualRepresentaion();
		
		KeyValue keyValueStartX = new KeyValue(node.translateXProperty(), annimation.startx);
		KeyValue keyValueStartY = new KeyValue(node.translateYProperty(), annimation.starty);
		KeyValue keyValueEndX = new KeyValue(node.translateXProperty(),annimation.endx);
		KeyValue keyValueEndY = new KeyValue(node.translateYProperty(), annimation.endy);
		

		KeyFrame keyFrame1 = new KeyFrame(Duration.millis(startTimeMs),
					new EventHandler<ActionEvent>(){
						@Override
						public void handle(ActionEvent event) {
							annimationPane.getChildren().add(node);
						}
			        },new KeyValue(node.opacityProperty(), 0));
		KeyFrame keyFrame2 = new KeyFrame(Duration.millis(startTimeMs), keyValueStartX, keyValueStartY);
		KeyFrame keyFrame3 = new KeyFrame(Duration.millis(startTimeMs+FADEDURATION),new KeyValue(node.opacityProperty(), 1));
		KeyFrame keyFrame4 = new KeyFrame(Duration.millis(startTimeMs+FADEDURATION), keyValueStartX, keyValueStartY);
		KeyFrame keyFrame5 = new KeyFrame(Duration.millis(endTimeMs-FADEDURATION),keyValueEndX,keyValueEndY);
		KeyFrame keyFrame6 = new KeyFrame(Duration.millis(endTimeMs-FADEDURATION), new KeyValue(node.opacityProperty(), 1));
		KeyFrame keyFrame7 = new KeyFrame(Duration.millis(endTimeMs), 
				new EventHandler<ActionEvent>(){
					@Override
					public void handle(ActionEvent event) {
						annimationPane.getChildren().remove(node);
					}
		        },new KeyValue(node.opacityProperty(), 0));
		
		
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
					annimationPane.getChildren().remove(node);
				}
			}
		});
	}

	private double getAnnimationPaneWidth(){
		final double width = annimationPane.getWidth();
		return width;
	}

}
