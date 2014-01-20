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
package org.copperengine.monitoring.client.ui.adaptermonitoring.result;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.animation.Animation.Status;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.DateStringConverter;
import javafx.util.converter.NumberStringConverter;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.filter.FilterResultControllerBase;
import org.copperengine.monitoring.client.ui.adaptermonitoring.fiter.AdapterMonitoringFilterModel;
import org.copperengine.monitoring.client.ui.adaptermonitoring.result.animation.AdapterAnimationCreator;
import org.copperengine.monitoring.client.util.TableColumnHelper;

public class AdapterMonitoringResultController extends FilterResultControllerBase<AdapterMonitoringFilterModel, AdapterMonitoringResultModel> implements
        Initializable {
    private static final String DATETIME_FORMAT = "dd.MM.yyyy HH:mm:ss,SSS";

    final GuiCopperDataProvider copperDataProvider;
    Timeline timeline;

    public AdapterMonitoringResultController(GuiCopperDataProvider copperDataProvider) {
        super();
        this.copperDataProvider = copperDataProvider;
    }

    @FXML
    // fx:id="adapterInputTable"
    private TableView<AdapterCallRowModel> adapterInputTable; // Value injected by FXMLLoader

    @FXML
    // fx:id="callsCol"
    private TableColumn<AdapterCallRowModel, String> callsCol; // Value injected by FXMLLoader

    @FXML
    // fx:id="parameterCol"
    private TableColumn<AdapterCallRowModel, String> parameterCol; // Value injected by FXMLLoader

    @FXML
    private TableColumn<AdapterCallRowModel, Date> timeCol;

    @FXML
    // fx:id="adapterOutputLaunchTable"
    private TableView<AdapterLaunchRowModel> adapterOutputLaunchTable; // Value injected by FXMLLoader

    @FXML
    // fx:id="adapterOutputNotifyTable"
    private TableView<AdapterNotifyRowModel> adapterOutputNotifyTable; // Value injected by FXMLLoader

    @FXML
    // fx:id="aunchWorkflowCol"
    private TableColumn<AdapterLaunchRowModel, String> launchWorkflowCol; // Value injected by FXMLLoader

    @FXML
    // fx:id="corrolelationIdCol"
    private TableColumn<AdapterNotifyRowModel, String> corrolelationIdCol; // Value injected by FXMLLoader

    @FXML
    // fx:id="mesageDetail"
    private TextArea mesageDetail; // Value injected by FXMLLoader

    @FXML
    // fx:id="messageCol"
    private TableColumn<AdapterNotifyRowModel, String> messageCol; // Value injected by FXMLLoader

    @FXML
    // fx:id="timeLaunchCol"
    private TableColumn<AdapterLaunchRowModel, Date> timeLaunchCol; // Value injected by FXMLLoader

    @FXML
    // fx:id="timeNotifyCol"
    private TableColumn<AdapterNotifyRowModel, Date> timeNotifyCol; // Value injected by FXMLLoader

    @FXML
    // fx:id="adapterNameLaunchOut"
    private TableColumn<AdapterLaunchRowModel, String> adapterNameLaunchOut; // Value injected by FXMLLoader

    @FXML
    // fx:id="adapterNameCorOut"
    private TableColumn<AdapterNotifyRowModel, String> adapterNameCorOut; // Value injected by FXMLLoader

    @FXML
    // fx:id="adapterNameCorOut"
    private TableColumn<AdapterCallRowModel, String> adapterNameIn; // Value injected by FXMLLoader

    @FXML
    // fx:id="callBorderPane"
    private BorderPane launchBorderPane; // Value injected by FXMLLoader

    @FXML
    // fx:id="inputBorderPane"
    private BorderPane inputBorderPane; // Value injected by FXMLLoader

    @FXML
    // fx:id="notifyBorderPane"
    private BorderPane notifyBorderPane; // Value injected by FXMLLoader

    @FXML
    // fx:id="annimationPane"
    private Pane annimationPane; // Value injected by FXMLLoader

    @FXML
    // fx:id="play"
    private ToggleButton play; // Value injected by FXMLLoader

    @FXML
    // fx:id="slider"
    private Slider slider; // Value injected by FXMLLoader

    @FXML
    // fx:id="speed"
    private Slider speed; // Value injected by FXMLLoader

    @FXML
    // fx:id="visualisationStackpane"
    private StackPane visualisationStackpane; // Value injected by FXMLLoader

    @FXML
    // fx:id="positionLabel"
    private Label positionLabel; // Value injected by FXMLLoader

    @FXML
    // fx:id="speedLabel"
    private Label speedLabel; // Value injected by FXMLLoader

    @FXML
    // fx:id="source"
    private TableColumn<AdapterCallRowModel, String> source; // Value injected by FXMLLo

    @FXML
    // fx:id="pause"
    private ToggleButton pause; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert adapterInputTable != null : "fx:id=\"adapterInputTable\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert adapterNameCorOut != null : "fx:id=\"adapterNameCorOut\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert adapterNameIn != null : "fx:id=\"adapterNameIn\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert adapterNameLaunchOut != null : "fx:id=\"adapterNameLaunchOut\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert adapterOutputLaunchTable != null : "fx:id=\"adapterOutputLaunchTable\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert adapterOutputNotifyTable != null : "fx:id=\"adapterOutputNotifyTable\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert annimationPane != null : "fx:id=\"annimationPane\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert callsCol != null : "fx:id=\"callsCol\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert corrolelationIdCol != null : "fx:id=\"corrolelationIdCol\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert inputBorderPane != null : "fx:id=\"inputBorderPane\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert launchBorderPane != null : "fx:id=\"launchBorderPane\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert launchWorkflowCol != null : "fx:id=\"launchWorkflowCol\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert mesageDetail != null : "fx:id=\"mesageDetail\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert messageCol != null : "fx:id=\"messageCol\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert notifyBorderPane != null : "fx:id=\"notifyBorderPane\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert parameterCol != null : "fx:id=\"parameterCol\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert pause != null : "fx:id=\"pause\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert play != null : "fx:id=\"play\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert positionLabel != null : "fx:id=\"positionLabel\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert slider != null : "fx:id=\"slider\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert source != null : "fx:id=\"source\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert speed != null : "fx:id=\"speed\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert speedLabel != null : "fx:id=\"speedLabel\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert timeCol != null : "fx:id=\"timeCol\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert timeLaunchCol != null : "fx:id=\"timeLaunchCol\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert timeNotifyCol != null : "fx:id=\"timeNotifyCol\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";
        assert visualisationStackpane != null : "fx:id=\"visualisationStackpane\" was not injected: check your FXML file 'AdapterMonitoringResult.fxml'.";

        launchBorderPane.setBottom(createTabelControlls(adapterOutputLaunchTable));
        inputBorderPane.setBottom(createTabelControlls(adapterInputTable));
        notifyBorderPane.setBottom(createTabelControlls(adapterOutputNotifyTable));

        source.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterCallRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<AdapterCallRowModel, String> param) {
                return param.getValue().workflowInstanceIdCaller.concat(":" + param.getValue().workflowClassCaller.get());
            }
        });

        callsCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterCallRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<AdapterCallRowModel, String> param) {
                return param.getValue().method;
            }
        });

        parameterCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterCallRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<AdapterCallRowModel, String> param) {
                return param.getValue().parameter;
            }
        });

        timeCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterCallRowModel, Date>, ObservableValue<Date>>() {
            @Override
            public ObservableValue<Date> call(CellDataFeatures<AdapterCallRowModel, Date> param) {
                return param.getValue().timestamp;
            }
        });
        TableColumnHelper.setConverterCellFactory(timeCol, new DateStringConverter(DATETIME_FORMAT));

        launchWorkflowCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterLaunchRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<AdapterLaunchRowModel, String> param) {
                return param.getValue().workflowname;
            }
        });
        timeLaunchCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterLaunchRowModel, Date>, ObservableValue<Date>>() {
            @Override
            public ObservableValue<Date> call(CellDataFeatures<AdapterLaunchRowModel, Date> param) {
                return param.getValue().timestamp;
            }
        });
        TableColumnHelper.setConverterCellFactory(timeLaunchCol, new DateStringConverter(DATETIME_FORMAT));

        corrolelationIdCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterNotifyRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<AdapterNotifyRowModel, String> param) {
                return param.getValue().correlationId;
            }
        });
        messageCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterNotifyRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<AdapterNotifyRowModel, String> param) {
                return param.getValue().message;
            }
        });
        timeNotifyCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterNotifyRowModel, Date>, ObservableValue<Date>>() {
            @Override
            public ObservableValue<Date> call(CellDataFeatures<AdapterNotifyRowModel, Date> param) {
                return param.getValue().timestamp;
            }
        });
        TableColumnHelper.setConverterCellFactory(timeNotifyCol, new DateStringConverter(DATETIME_FORMAT));

        adapterOutputNotifyTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<AdapterNotifyRowModel>() {
            @Override
            public void changed(ObservableValue<? extends AdapterNotifyRowModel> observable, AdapterNotifyRowModel oldValue,
                    AdapterNotifyRowModel newValue) {
                if (newValue != null) {
                    mesageDetail.setText(newValue.message.get());
                } else {
                    mesageDetail.setText("");
                }
            }
        });

        adapterNameCorOut.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterNotifyRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<AdapterNotifyRowModel, String> param) {
                return param.getValue().adapterName;
            }
        });
        TableColumnHelper.setTextOverrunCellFactory(adapterNameCorOut, OverrunStyle.LEADING_ELLIPSIS);
        adapterNameLaunchOut.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterLaunchRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<AdapterLaunchRowModel, String> param) {
                return param.getValue().adapterName;
            }
        });
        TableColumnHelper.setTextOverrunCellFactory(adapterNameLaunchOut, OverrunStyle.LEADING_ELLIPSIS);
        adapterNameIn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AdapterCallRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<AdapterCallRowModel, String> param) {
                return param.getValue().adapterName;
            }
        });
        TableColumnHelper.setTextOverrunCellFactory(adapterNameIn, OverrunStyle.LEADING_ELLIPSIS);

        adapterInputTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        adapterOutputLaunchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        adapterOutputNotifyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupAnnimationTab();
    }

    private void setupAnnimationTab() {
        timeline = new Timeline();
        timeline.setAutoReverse(false);
        timeline.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                slider.setValue(newValue.toMillis());
            }
        });
        timeline.rateProperty().bindBidirectional(speed.valueProperty());
        pause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (pause.isSelected()) {
                    timeline.pause();
                } else {
                    timeline.play();
                }
            }
        });

        slider.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (timeline.getStatus() == Status.RUNNING) {
                    timeline.stop();
                    timeline.jumpTo(timeline.getTotalDuration().multiply(event.getX() / slider.getWidth()));
                    timeline.play();
                }
            }
        });

        speedLabel.textProperty().bindBidirectional(speed.valueProperty(), new NumberStringConverter("0.0 x"));

        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                play.setSelected(false);
            }
        });

        play.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (timeline.getStatus() != Status.RUNNING) {
                    // lazy init cause else annimation pane is not layouted
                    timeline.getKeyFrames().clear();
                    annimationPane.getChildren().clear();
                    final AdapterAnimationCreator adapterAnimationCreator = new AdapterAnimationCreator(annimationPane, timeline);
                    adapterAnimationCreator.create(adapterInputTable.getItems(), adapterOutputLaunchTable.getItems(),
                            adapterOutputNotifyTable.getItems());

                    slider.setMax(timeline.getTotalDuration().toMillis());

                    Bindings.unbindBidirectional(positionLabel.textProperty(), slider.valueProperty());
                    positionLabel.textProperty().bindBidirectional(slider.valueProperty(), new StringConverter<Number>() {
                        private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATETIME_FORMAT);

                        @Override
                        public Number fromString(String string) {
                            return null;
                        }

                        @Override
                        public String toString(Number object) {
                            return simpleDateFormat.format(new Date(object.longValue() + adapterAnimationCreator.getMin()));
                        }
                    });
                    timeline.jumpTo(Duration.ZERO);
                    timeline.play();
                } else {
                    timeline.stop();
                    annimationPane.getChildren().clear();
                }
            }
        });
        pause.disableProperty().bind(play.selectedProperty().not());

        play.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/org/copperengine/gui/icon/play.png"))));
        pause.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/org/copperengine/gui/icon/pause.png"))));
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("AdapterMonitoringResult.fxml");
    }

    @Override
    public void showFilteredResult(List<AdapterMonitoringResultModel> filteredResult, AdapterMonitoringFilterModel usedFilter) {
        if (!filteredResult.isEmpty()) {
            adapterInputTable.setItems(filteredResult.get(0).adapterCalls);
            adapterOutputLaunchTable.setItems(filteredResult.get(0).adapterLaunches);
            adapterOutputNotifyTable.setItems(filteredResult.get(0).adapterNotifies);
        }
    }

    @Override
    public List<AdapterMonitoringResultModel> applyFilterInBackgroundThread(AdapterMonitoringFilterModel filter) {
        Date from = filter.fromToFilterModel.from.get();
        Date to = filter.fromToFilterModel.to.get();
        AdapterMonitoringResultModel result = new AdapterMonitoringResultModel(
                copperDataProvider.getAdapterCalls(from, to, filter.maxCountFilterModel.getMaxCount()),
                copperDataProvider.getAdapterNotifies(from, to, filter.maxCountFilterModel.getMaxCount()),
                copperDataProvider.getAdapterLaunches(from, to, filter.maxCountFilterModel.getMaxCount()));
        return Arrays.asList(result);
    }

    @Override
    public boolean supportsClear() {
        return true;
    }

    @Override
    public void clear() {
        adapterInputTable.getItems().clear();
        adapterOutputLaunchTable.getItems().clear();
        adapterOutputNotifyTable.getItems().clear();
        mesageDetail.clear();
    }

}
