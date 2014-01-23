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
package org.copperengine.monitoring.client.form.filter;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.copperengine.monitoring.client.form.Form;
import org.copperengine.monitoring.client.form.ShowFormsStrategy;
import org.copperengine.monitoring.client.form.filter.FilterController.ActionsWithFilterForm;
import org.copperengine.monitoring.client.form.issuereporting.IssueReporter;
import org.copperengine.monitoring.client.util.ComponentUtil;
import org.copperengine.monitoring.client.util.MessageKey;
import org.copperengine.monitoring.client.util.MessageProvider;
import org.copperengine.monitoring.client.util.NumberOnlyTextField;

/**
 * A Form with a filter form and a result form
 * 
 * @param <F>
 *            FilterModel
 * @param <R>
 *            ResultModel
 */
public class FilterAbleForm<F, R> extends Form<Object> {
    protected final Form<FilterController<F>> filterForm;
    protected final Form<FilterResultController<F, R>> resultForm;
    private final BackgroundFilterService<F, R> filterService;
    private final BackgroundRepeatFilterService<F, R> repeatFilterService;
    private final MessageProvider messageProvider;

    public static final String REFRESH_BUTTON_ID = "refreshbutton";

    public FilterAbleForm(MessageProvider messageProvider, ShowFormsStrategy<?> showFormStrategie,
            Form<FilterController<F>> filterForm, final Form<FilterResultController<F, R>> resultForm, IssueReporter exceptionHandlerParm) {
        super("", showFormStrategie, null);
        this.messageProvider = messageProvider;
        this.filterForm = filterForm;
        this.resultForm = resultForm;
        filterService = new BackgroundFilterService<F, R>(resultForm.getController(), filterForm, exceptionHandlerParm);
        repeatFilterService = new BackgroundRepeatFilterService<F, R>(resultForm.getController(), filterForm, exceptionHandlerParm);

        filterForm.getController().getActionsWithFilterForm().addListener(new ListChangeListener<ActionsWithFilterForm>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends ActionsWithFilterForm> c) {
                c.next();
                for (ActionsWithFilterForm actionsWithFilterForm : c.getAddedSubList()) {
                    actionsWithFilterForm.run(FilterAbleForm.this);
                }
            }
        });
    }

    public FilterController<F> getFilterController() {
        return filterForm.getController();
    }

    public FilterResultController<F,R> getResultController() {
        return resultForm.getController();
    }

    public F getFilter() {
        return filterForm.getController().getFilter();
    }

    @Override
    public Node createContent() {

        final StackPane masterStackPane = new StackPane();
        masterStackPane.setOnKeyReleased(new EventHandler<javafx.scene.input.KeyEvent>() {
            @Override
            public void handle(javafx.scene.input.KeyEvent event) {
                KeyCodeCombination keyCombination = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN);
                if (keyCombination.match(event)) {
                    refresh();
                }
            }
        });

        filterService.stateProperty().addListener(new ChangeListener<Worker.State>() {
            final Node indicator = ComponentUtil.createProgressIndicator();

            @Override
            public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
                if (newValue == State.RUNNING) {
                    masterStackPane.getChildren().add(indicator);
                } else {
                    masterStackPane.getChildren().remove(indicator);
                }
            }
        });

        BorderPane masterBorderPane = new BorderPane();
        masterStackPane.getChildren().add(masterBorderPane);


        final StackPane centerStackpane = new StackPane();
        centerStackpane.setAlignment(Pos.TOP_LEFT);


        final HBox allFilterParent = createFilter();
        final Pane allFilterParentWrapper = new Pane();
        allFilterParentWrapper.setPickOnBounds(false);
        allFilterParentWrapper.getChildren().add(allFilterParent);

        ToolBar formToolbar = new ToolBar();
        formToolbar.setOrientation(Orientation.VERTICAL);
        final HBox leftPane= new HBox();
        formToolbar.getItems().addAll(createDefaultFormToolbar(allFilterParentWrapper,centerStackpane));
        leftPane.getChildren().add(formToolbar);
        masterBorderPane.setLeft(formToolbar);
        final Node formcontent = resultForm.createContent();
        centerStackpane.getChildren().add(formcontent);
        masterBorderPane.setCenter(centerStackpane);


        filterService.reset();
        filterService.start();
        refreshRateInMs.set(Long.toString(filterForm.getController().getDefaultRefreshInterval()));

        return masterStackPane;
    }

    private class FilterFadeHandler {
        public static final double MIN_OPACITY = 0.75;
        final Node target ;
        private final FadeTransition ftIn;
        private final FadeTransition ftOut;

        private FilterFadeHandler(Node target) {
            this.target = target;

            ftIn = new FadeTransition(Duration.millis(400), target);
            ftIn.setFromValue(MIN_OPACITY);
            ftIn.setToValue(1);

            ftOut = new FadeTransition(Duration.millis(400), target);
            ftOut.setFromValue(1.0);
            ftOut.setToValue(MIN_OPACITY);
        }

        public EventHandler<MouseEvent> getEnter(){
            return new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (enabled && target.getOpacity()!=1){
                        ftIn.playFromStart();
                        ftOut.pause();
                    }
                }
            };
        }

        public EventHandler<MouseEvent> getExit(){
            return new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (enabled && target.getOpacity()!=MIN_OPACITY){
                        ftOut.playFromStart();
                        ftIn.pause();
                    }
                }
            };
        }

        boolean enabled=true;
        public void disable() {
            enabled=false;
        }

        public void enable() {
            enabled=true;
        }
    }

    private HBox createFilter(){
        final HBox allFilterParent = new HBox();
        final VBox filterAreaPanes = new VBox();


        Node customFormFilteContent = this.createFilterContent();
        final BorderPane customFormFilter = new BorderPane();
        customFormFilter.getStyleClass().add("filter-pane");
        customFormFilter.setCenter(customFormFilteContent);

        final FilterFadeHandler filderFadeHandler = new FilterFadeHandler(allFilterParent);
        allFilterParent.setOnMouseEntered(filderFadeHandler.getEnter());
        allFilterParent.setOnMouseExited(filderFadeHandler.getExit());
        filterAreaPanes.setPickOnBounds(false);

        allFilterParent.setOpacity(1);
        HBox.setHgrow(customFormFilter, Priority.NEVER);
        if (filterForm.getController().supportsFiltering()) {
            filterAreaPanes.getChildren().add(customFormFilter);
        }

        allFilterParent.getChildren().add(filterAreaPanes);
        final Pane divider = new Pane();
        divider.getStyleClass().add("filter-resizer");
        divider.getStyleClass().add("button");
        divider.setMaxWidth(8);
        divider.setMinWidth(8);

//        divider.prefHeightProperty().bind(customFormFilter.prefHeightProperty());
        divider.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                filderFadeHandler.disable();
            }
        });
        divider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                filderFadeHandler.enable();
            }
        });
        divider.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                filterAreaPanes.setPrefWidth(allFilterParent.getParent().sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY()).getX());
            }
        });
        divider.setCursor(Cursor.H_RESIZE);
        allFilterParent.getChildren().add(divider);
        allFilterParent.setPickOnBounds(false);//transparent area is mousetransparent


        final BorderPane defaultFilter = new BorderPane();
        defaultFilter.getStyleClass().add("filter-pane");
        final Node defaultFilterContent = filterForm.getController().createDefaultFilter();
        defaultFilter.setCenter(defaultFilterContent);
        if (defaultFilterContent!=null){
            filterAreaPanes.getChildren().add(defaultFilter);
        }

        final BorderPane settings = new BorderPane();
        settings.getStyleClass().add("filter-pane");
        HBox settingsPane = new HBox(3);
        settingsPane.setAlignment(Pos.CENTER_LEFT);
        settingsPane.getChildren().add(new Label("Refresh Interval"));
        TextField interval = new NumberOnlyTextField();
        interval.setPrefWidth(100);
        interval.textProperty().bindBidirectional(refreshRateInMs);
        settingsPane.getChildren().add(interval);
        settingsPane.getChildren().add(new Label("ms"));
        settings.setCenter(settingsPane);
        filterAreaPanes.getChildren().add(settings);

        return allFilterParent;
    }

    private List<Node> createDefaultFormToolbar(final Node wrapperFilter, final StackPane centerStackpane) {
        ArrayList<Node> result = new ArrayList<Node>();

        final Button refreshButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream("/org/copperengine/gui/icon/refresh.png"))));
        refreshButton.setId(REFRESH_BUTTON_ID);
        HBox.setMargin(refreshButton, new Insets(4, 0, 4, 0));
        refreshButton.setTooltip(new Tooltip(messageProvider.getText(MessageKey.filterAbleForm_button_refresh)));
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                refresh();
            }
        });
        result.add(refreshButton);
        result.add(new Separator(Orientation.HORIZONTAL));

        final ProgressIndicator repeatProgressIndicator = new ProgressIndicator();
        repeatProgressIndicator.setVisible(false);
        repeatProgressIndicator.progressProperty().bind(repeatFilterService.progressProperty());
        final ToggleButton repeatToggleButton = new ToggleButton("", new ImageView(new Image(getClass().getResourceAsStream("/org/copperengine/gui/icon/repeat.png"))));
//        repeatProgressIndicator.setMaxWidth(refreshButton.getWidth());
        repeatProgressIndicator.setPrefWidth(refreshButton.getWidth());

        repeatFilterService.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                repeatProgressIndicator.setVisible(false);
            }
        });
        repeatFilterService.stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
                if (newValue == State.CANCELLED || newValue == State.FAILED || newValue == State.SUCCEEDED) {
                    repeatProgressIndicator.setVisible(false);
                } else {
                    repeatProgressIndicator.setVisible(true);
                }

            }
        });

        repeatFilterService.runningProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue != null) {
                    repeatToggleButton.setSelected(newValue);
                }
            }
        });
        result.add(repeatToggleButton);

        final Button clearButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream("/org/copperengine/gui/icon/clear.png"))));
        clearButton.setTooltip(new Tooltip(messageProvider.getText(MessageKey.filterAbleForm_button_clear)));
        clearButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                resultForm.getController().clear();
            }
        });
        if (resultForm.getController().supportsClear()) {
            result.add(clearButton);
        }



        result.add(new Separator(Orientation.HORIZONTAL));
        final ToggleButton filterButton = new ToggleButton("", new ImageView(new Image(getClass().getResourceAsStream("/org/copperengine/gui/icon/filter.png"))));
        filterButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue!=null && newValue) {
                    centerStackpane.getChildren().add(wrapperFilter);
                } else {
                    centerStackpane.getChildren().remove(wrapperFilter);
                }
            }
        });
        filterButton.setPrefWidth(20);
        result.add(filterButton);
//        if (filterForm.getController().createDefaultFilter() != null) {
//            defaultFilterContent.setContent(filterForm.getController().createDefaultFilter());
//        } else {
//            defaultFilterButton.setDisable(true);
//        }


        result.add(new Separator(Orientation.HORIZONTAL));
        final List<? extends Node> contributedButtons = resultForm.getController().getContributedButtons(messageProvider);
        result.addAll(contributedButtons);
        if (contributedButtons != null && contributedButtons.size()>0){
            result.add(new Separator(Orientation.HORIZONTAL));
        }

        result.add(repeatProgressIndicator);

        repeatToggleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (repeatToggleButton.isSelected()) {
                    repeatFilterService.setRefreshIntervall(Long.valueOf(refreshRateInMs.get()));
                    repeatFilterService.reset();
                    repeatFilterService.start();
                    filterButton.setSelected(false);
                } else {
                    repeatFilterService.cancel();
                }
            }
        });

        refreshButton.disableProperty().bind(repeatToggleButton.selectedProperty());
        clearButton.disableProperty().bind(repeatToggleButton.selectedProperty());
        filterButton.disableProperty().bind(repeatToggleButton.selectedProperty());

        return result;
    }

    private Node createFilterContent() {
        final Node content = filterForm.createContent();
        if (content == null) {
            return new Pane();
        }
        return content;
    }

    public void refresh() {
        filterService.reset();
        filterService.start();
    }

    final SimpleStringProperty refreshRateInMs = new SimpleStringProperty();

    public static class ResultFilterPair<F, R> {
        public final List<R> result;
        public final F usedFilter;

        public ResultFilterPair(List<R> result, F usedFilter) {
            super();
            this.result = result;
            this.usedFilter = usedFilter;
        }
    }

    @Override
    public void close() {
        getResultController().onClose();
        super.close();
    }
}
