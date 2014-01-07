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
package org.copperengine.monitoring.client.ui.adaptermonitoring.result.animation;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import org.copperengine.monitoring.client.ui.adaptermonitoring.result.AdapterCallRowModel;
import org.copperengine.monitoring.client.ui.adaptermonitoring.result.AdapterLaunchRowModel;
import org.copperengine.monitoring.client.ui.adaptermonitoring.result.AdapterNotifyRowModel;

import com.google.common.base.Optional;

public class AdapterAnimationCreator {

    public long FADEDURATION = 300;
    public long MOVEDURATION = 1600;
    public long DEFAULT_TOTAL_ANNIMATION_TIME = FADEDURATION + MOVEDURATION + FADEDURATION;

    private long min;

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    Pane animationPane;
    Timeline timeline;
    private ArrayList<AnimationPartBase> animations;

    public AdapterAnimationCreator(Pane animationPane, Timeline timeline) {
        super();
        this.animationPane = animationPane;
        this.timeline = timeline;
    }

    private Optional<AnimationPartBase> searchAnimationRunningAt(String id, long startTime, long endTime) {
        AnimationPartBase result = null;
        for (AnimationPartBase animation : animations) {
            if (animation.id.equals(id) && !(endTime < animation.startTime || startTime > animation.endTime)) {
                result = animation;
                break;
            }
        }
        return Optional.fromNullable(result);
    }

    private List<AnimationPartBase> searchAnimationWithType(Class<? extends AnimationPartBase> clazz, long startTime, long endTime) {
        List<AnimationPartBase> result = new ArrayList<AnimationPartBase>();
        for (AnimationPartBase animation : animations) {
            if (animation.getClass() == clazz && !(endTime < animation.startTime || startTime > animation.endTime)) {
                result.add(animation);
            }
        }
        return result;
    }

    private void addAdapterAnimation(String adapterName, long time) {
        Optional<AnimationPartBase> animationOpt = searchAnimationRunningAt(adapterName, time, time + DEFAULT_TOTAL_ANNIMATION_TIME);
        if (animationOpt.isPresent()) {
            animationOpt.get().endTime = time + DEFAULT_TOTAL_ANNIMATION_TIME;
        } else {
            Optional<Double> ypos = getFreeYslot(time, time + DEFAULT_TOTAL_ANNIMATION_TIME, AdapterAnimation.ADAPTER_HEIGHT + 20, false, Arrays.<Class<? extends AnimationPartBase>>asList(AdapterAnimation.class));
            if (ypos.isPresent()) {
                double xpos = animationPane.getWidth() / 2 - AdapterAnimation.ADAPTER_WIDTH / 2;

                animations.add(new AdapterAnimation(new AnimationPartParameter(time, time + DEFAULT_TOTAL_ANNIMATION_TIME, adapterName,
                        xpos,
                        ypos.get(),
                        xpos,
                        ypos.get())));
            }
        }
    }

    private Optional<Double> getFreeYslot(long starttime, long endtime, double slotHeight, boolean useEndPos, List<Class<? extends AnimationPartBase>> types) {
        final List<AnimationPartBase> foundAnimations = new ArrayList<AnimationPartBase>();
        for (Class<? extends AnimationPartBase> type : types) {
            foundAnimations.addAll(searchAnimationWithType(type, starttime, endtime));
        }
        for (int i = 0; i < 20; i++) {
            double ypos = 65 + (slotHeight) * i;
            boolean posInlist = false;
            for (AnimationPartBase animation : foundAnimations) {
                if (useEndPos) {
                    if (Math.abs(animation.endy - ypos) < 0.0001) {
                        posInlist = true;
                    }
                } else {
                    if (Math.abs(animation.starty - ypos) < 0.0001) {
                        posInlist = true;
                    }
                }
            }
            if (!posInlist) {
                return Optional.of(ypos);
            }
        }
        return Optional.absent();
    }

    private void addNotifyEventAnimation(long time, String id, String adapterId) {
        Optional<Double> ypos = getFreeYslot(time, time + DEFAULT_TOTAL_ANNIMATION_TIME, 60, true, Arrays.<Class<? extends AnimationPartBase>>asList(NotifyAnimation.class, LaunchAnimation.class));
        if (ypos.isPresent()) {
            Optional<AnimationPartBase> adapterAnimation = searchAnimationRunningAt(adapterId, time, time + DEFAULT_TOTAL_ANNIMATION_TIME);
            if (adapterAnimation.isPresent()) {
                animations.add(new NotifyAnimation(createOutputParameter(time, id, ypos, adapterAnimation)));
            }
        }
    }

    private AnimationPartParameter createOutputParameter(long time, String id, Optional<Double> ypos,
            Optional<AnimationPartBase> adapterAnimation) {
        return new AnimationPartParameter(time, time + DEFAULT_TOTAL_ANNIMATION_TIME, id,
                adapterAnimation.get().startx + AdapterAnimation.ADAPTER_WIDTH / 2 - EventAnimationBase.EVENT_WIDTH / 2,
                adapterAnimation.get().starty + AdapterAnimation.ADAPTER_HEIGHT - EventAnimationBase.EVENT_HEIGHT - 5,
                getAnimationPaneWidth() / 2 + getAnimationPaneWidth() / 4 - EventAnimationBase.EVENT_WIDTH / 2,
                ypos.get());
    }

    private void addLaunchEventAnimation(long time, String id, String adapterId) {
        Optional<Double> ypos = getFreeYslot(time, time + DEFAULT_TOTAL_ANNIMATION_TIME, 60, true, Arrays.<Class<? extends AnimationPartBase>>asList(NotifyAnimation.class, LaunchAnimation.class));
        if (ypos.isPresent()) {
            Optional<AnimationPartBase> adapterAnimation = searchAnimationRunningAt(adapterId, time, time + DEFAULT_TOTAL_ANNIMATION_TIME);
            if (adapterAnimation.isPresent()) {
                animations.add(new LaunchAnimation(createOutputParameter(time, id, ypos, adapterAnimation)));
            }
        }
    }

    private void addCallEventAnimation(long time, String id, String adapterId, String workflowInstanceId) {
        double ypos = searchAnimationRunningAt(workflowInstanceId, time, time + DEFAULT_TOTAL_ANNIMATION_TIME).get().starty + 5;

        Optional<AnimationPartBase> adapterAnimation = searchAnimationRunningAt(adapterId, time, time + DEFAULT_TOTAL_ANNIMATION_TIME);
        Optional<AnimationPartBase> sameCallAnimation = searchAnimationRunningAt(id, time, time + 20);
        if (adapterAnimation.isPresent()) {
            if (sameCallAnimation.isPresent()) {
                ((CallAnimation) sameCallAnimation.get()).count++;
            } else {
                animations.add(new CallAnimation(new AnimationPartParameter(time, time + DEFAULT_TOTAL_ANNIMATION_TIME, id,
                        getAnimationPaneWidth() / 2 - getAnimationPaneWidth() / 4,
                        ypos,
                        adapterAnimation.get().startx + AdapterAnimation.ADAPTER_WIDTH / 2 - EventAnimationBase.EVENT_WIDTH / 2,
                        adapterAnimation.get().starty + 5)));
            }
        }
    }

    private class TimeValuePair<T> {
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
            ObservableList<AdapterNotifyRowModel> adapterOutputNotify) {

        animations = new ArrayList<AnimationPartBase>();

        min = Long.MAX_VALUE;
        for (final AdapterCallRowModel adapterCallRowModel : adapterInput) {
            min = Math.min(min, adapterCallRowModel.timestamp.get().getTime() - DEFAULT_TOTAL_ANNIMATION_TIME);
        }
        for (final AdapterLaunchRowModel adapterLaunchRowModel : adapterOutputLaunch) {
            min = Math.min(min, adapterLaunchRowModel.timestamp.get().getTime());
        }
        for (final AdapterNotifyRowModel adapterNotifyRowModel : adapterOutputNotify) {
            min = Math.min(min, adapterNotifyRowModel.timestamp.get().getTime());
        }

        ArrayList<TimeValuePair<String>> timeAdapterPairs = new ArrayList<TimeValuePair<String>>();
        for (final AdapterCallRowModel adapterCallRowModel : adapterInput) {
            final long time = adapterCallRowModel.timestamp.get().getTime() - DEFAULT_TOTAL_ANNIMATION_TIME;
            timeAdapterPairs.add(new TimeValuePair<String>(adapterCallRowModel.adapterName.get(), time));
            addWorkflowAnimation(adapterCallRowModel.workflowClassCaller.get(), adapterCallRowModel.workflowInstanceIdCaller.get(), time);
        }
        for (final AdapterLaunchRowModel adapterLaunchRowModel : adapterOutputLaunch) {
            final long time = adapterLaunchRowModel.timestamp.get().getTime();
            timeAdapterPairs.add(new TimeValuePair<String>(adapterLaunchRowModel.adapterName.get(), time));
        }
        for (final AdapterNotifyRowModel adapterNotifyRowModel : adapterOutputNotify) {
            final long time = adapterNotifyRowModel.timestamp.get().getTime();
            timeAdapterPairs.add(new TimeValuePair<String>(adapterNotifyRowModel.adapterName.get(), time));
        }
        Collections.sort(timeAdapterPairs, new Comparator<TimeValuePair<String>>() {
            @Override
            public int compare(TimeValuePair<String> o1, TimeValuePair<String> o2) {
                // TODO replace when switch to java 1.7 with: Long.compare(o1.time, o2.time);
                return Long.valueOf(o1.time).compareTo(Long.valueOf(o2.time));
            }
        });
        for (TimeValuePair<String> timeAdapterPair : timeAdapterPairs) {
            addAdapterAnimation(timeAdapterPair.value, timeAdapterPair.time);
        }

        addStaticContent();

        for (final AdapterCallRowModel adapterCallRowModel : adapterInput) {
            addCallEventAnimation(
                    adapterCallRowModel.timestamp.get().getTime() - DEFAULT_TOTAL_ANNIMATION_TIME,
                    adapterCallRowModel.method.get(),
                    adapterCallRowModel.adapterName.get(),
                    adapterCallRowModel.workflowInstanceIdCaller.get());
        }

        ArrayList<TimeValuePair<Object>> outputsSorted = new ArrayList<TimeValuePair<Object>>();
        for (final AdapterLaunchRowModel adapterLaunchRowModel : adapterOutputLaunch) {
            outputsSorted.add(new TimeValuePair<Object>(adapterLaunchRowModel, adapterLaunchRowModel.timestamp.get().getTime()));
        }
        for (final AdapterNotifyRowModel adapterNotifyRowModel : adapterOutputNotify) {
            outputsSorted.add(new TimeValuePair<Object>(adapterNotifyRowModel, adapterNotifyRowModel.timestamp.get().getTime()));
        }
        Collections.sort(outputsSorted, new Comparator<TimeValuePair<Object>>() {
            @Override
            public int compare(TimeValuePair<Object> o1, TimeValuePair<Object> o2) {
                // TODO replace when switch to java 1.7 with: Long.compare(o1.time, o2.time);
                return Long.valueOf(o1.time).compareTo(Long.valueOf(o2.time));
            }
        });
        for (TimeValuePair<Object> output : outputsSorted) {
            if (output.value instanceof AdapterLaunchRowModel) {
                AdapterLaunchRowModel adapterLaunchRowModel = (AdapterLaunchRowModel) output.value;
                addLaunchEventAnimation(
                        adapterLaunchRowModel.timestamp.get().getTime(),
                        adapterLaunchRowModel.workflowname.get(),
                        adapterLaunchRowModel.adapterName.get());
            }
            if (output.value instanceof AdapterNotifyRowModel) {
                AdapterNotifyRowModel adapterNotifyRowModel = (AdapterNotifyRowModel) output.value;
                addNotifyEventAnimation(
                        adapterNotifyRowModel.timestamp.get().getTime(),
                        adapterNotifyRowModel.correlationId.get(),
                        adapterNotifyRowModel.adapterName.get());
            }
        }

        ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>();// Performance optimization adding single keyframes
        // directly is too slow
        for (AnimationPartBase animation : animations) {
            addAnimation(keyFrames, animation, min);
        }
        timeline.getKeyFrames().addAll(keyFrames);
    }

    private void addWorkflowAnimation(String workflowClass, String workflowInstanceId, long time) {
        Optional<AnimationPartBase> animationOpt = searchAnimationRunningAt(workflowInstanceId, time, time + DEFAULT_TOTAL_ANNIMATION_TIME);
        if (animationOpt.isPresent()) {
            animationOpt.get().endTime = time + DEFAULT_TOTAL_ANNIMATION_TIME;
        } else {
            Optional<Double> ypos = getFreeYslot(time, time + DEFAULT_TOTAL_ANNIMATION_TIME, EventAnimationBase.EVENT_HEIGHT + 15 + 35, false, Arrays.<Class<? extends AnimationPartBase>>asList(WorkflowAnimation.class));
            if (ypos.isPresent()) {
                double xpos = animationPane.getWidth() / 2 - animationPane.getWidth() / 4 - WorkflowAnimation.WIDTH / 2;

                animations.add(new WorkflowAnimation(workflowClass, new AnimationPartParameter(time, time + DEFAULT_TOTAL_ANNIMATION_TIME, workflowInstanceId,
                        xpos,
                        ypos.get(),
                        xpos,
                        ypos.get())));
            }
        }
    }

    private void createLegend() {
        VBox pane = new VBox();
        // pane.setScaleX(0.5);
        // pane.setScaleY(0.5);
        pane.getChildren().add(new Label("Legend"));
        pane.getChildren().add(createLegendEntry("adapter", AdapterAnimation.ADAPTER_COLOR));
        pane.getChildren().add(createLegendEntry("adapter method call", CallAnimation.ADAPTER_CALL_COLOR));
        pane.getChildren().add(createLegendEntry("notify correlation id ", NotifyAnimation.ADAPTER_NOTIFY_COLOR));
        pane.getChildren().add(createLegendEntry("workflow launch", LaunchAnimation.ADAPTER_LAUNCH_COLOR));
        pane.getChildren().add(createLegendEntry("workflow instance", WorkflowAnimation.WORKFLOW_COLOR));
        pane.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        pane.setTranslateX(3);
        pane.setTranslateY(animationPane.getHeight() - 150);
        animationPane.getChildren().add(pane);

    }

    private Node createLegendEntry(String text, Color color) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(3);
        hbox.getChildren().add(new Rectangle(15, 15, color));
        hbox.getChildren().add(new Label(text));
        VBox.setMargin(hbox, new Insets(1.5, 3, 1.5, 3));
        return hbox;
    }

    private void addStaticContent() {
        createLegend();

        final Text inputText = new Text("Input");
        inputText.setX(getAnimationPaneWidth() / 2 - getAnimationPaneWidth() / 4 - inputText.getBoundsInLocal().getWidth() / 2);
        inputText.setY(20);
        inputText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, Font.getDefault().getSize()));
        inputText.setFontSmoothingType(FontSmoothingType.LCD);
        animationPane.getChildren().add(inputText);

        final Text outputText = new Text("Output");
        outputText.setX(getAnimationPaneWidth() / 2 + getAnimationPaneWidth() / 4 - outputText.getBoundsInLocal().getWidth() / 2);
        outputText.setY(20);
        outputText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, Font.getDefault().getSize()));
        outputText.setFontSmoothingType(FontSmoothingType.LCD);
        animationPane.getChildren().add(outputText);

        final Text adapterText = new Text("Adapter");
        adapterText.setX(getAnimationPaneWidth() / 2 - adapterText.getBoundsInLocal().getWidth() / 2);
        adapterText.setTranslateY(20);
        adapterText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, Font.getDefault().getSize()));
        adapterText.setFontSmoothingType(FontSmoothingType.LCD);
        animationPane.getChildren().add(adapterText);

        Line lineInput = new Line();
        lineInput.getStrokeDashArray().addAll(5d);
        lineInput.setCache(true);
        lineInput.startXProperty().set(getAnimationPaneWidth() / 2 - getAnimationPaneWidth() / 8);
        lineInput.endXProperty().set(lineInput.startXProperty().get());
        lineInput.startYProperty().set(0);
        lineInput.endYProperty().bind(animationPane.heightProperty());
        animationPane.getChildren().add(lineInput);

        Line lineOutput = new Line();
        lineOutput.getStrokeDashArray().addAll(5d);
        lineOutput.setCache(true);
        lineOutput.startXProperty().set(getAnimationPaneWidth() / 2 + getAnimationPaneWidth() / 8);
        lineOutput.endXProperty().set(lineOutput.startXProperty().get());
        lineOutput.startYProperty().set(0);
        lineOutput.endYProperty().bind(animationPane.heightProperty());
        animationPane.getChildren().add(lineOutput);

    }

    private void addAnimation(ArrayList<KeyFrame> keyFrames, final AnimationPartBase annimation, long minTime) {
        long startTimeMs = annimation.startTime - minTime;
        long endTimeMs = annimation.endTime - minTime;

        final Node node = annimation.createVisualRepresentation();

        KeyValue keyValueStartX = new KeyValue(node.translateXProperty(), annimation.startx);
        KeyValue keyValueStartY = new KeyValue(node.translateYProperty(), annimation.starty);
        KeyValue keyValueEndX = new KeyValue(node.translateXProperty(), annimation.endx);
        KeyValue keyValueEndY = new KeyValue(node.translateYProperty(), annimation.endy);

        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(startTimeMs),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        animationPane.getChildren().add(node);
                    }
                }, new KeyValue(node.opacityProperty(), 0));
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(startTimeMs), keyValueStartX, keyValueStartY);
        KeyFrame keyFrame3 = new KeyFrame(Duration.millis(startTimeMs + FADEDURATION), new KeyValue(node.opacityProperty(), 1));
        KeyFrame keyFrame4 = new KeyFrame(Duration.millis(startTimeMs + FADEDURATION), keyValueStartX, keyValueStartY);
        KeyFrame keyFrame5 = new KeyFrame(Duration.millis(endTimeMs - FADEDURATION), keyValueEndX, keyValueEndY);
        KeyFrame keyFrame6 = new KeyFrame(Duration.millis(endTimeMs - FADEDURATION), new KeyValue(node.opacityProperty(), 1));
        KeyFrame keyFrame7 = new KeyFrame(Duration.millis(endTimeMs),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        animationPane.getChildren().remove(node);
                    }
                }, new KeyValue(node.opacityProperty(), 0));

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
                if (newValue == Status.STOPPED) {// clean up when animation stopped
                    animationPane.getChildren().remove(node);
                }
            }
        });
    }

    private double getAnimationPaneWidth() {
        return animationPane.getWidth();
    }

}
