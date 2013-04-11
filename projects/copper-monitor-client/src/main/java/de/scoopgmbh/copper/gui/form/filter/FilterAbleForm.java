/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.gui.form.filter;

import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.util.converter.IntegerStringConverter;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.ShowFormStrategy;
import de.scoopgmbh.copper.gui.util.ComponentUtil;
import de.scoopgmbh.copper.gui.util.MessageKey;
import de.scoopgmbh.copper.gui.util.MessageProvider;
import de.scoopgmbh.copper.gui.util.NumerOnlyTextField;

/**
 * @param <F> FilterModel
 * @param <R> ResultModel
 */
public class FilterAbleForm<F,R> extends Form<Object>{
	protected final Form<FilterController<F>> filterForm;
	protected final Form<FilterResultController<F,R>> resultForm;
	protected final GuiCopperDataProvider copperDataProvider;
	private FilterService<F,R> filterService;
	private RepeatFilterService<F,R> repeatFilterService;
	private final MessageProvider messageProvider;

	public FilterAbleForm(MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie,
			Form<FilterController<F>> filterForm, final Form<FilterResultController<F,R>> resultForm, GuiCopperDataProvider copperDataProvider ) {
		super("", showFormStrategie, null);
		this.messageProvider = messageProvider;
		this.filterForm = filterForm;
		this.resultForm = resultForm;
		this.copperDataProvider = copperDataProvider;
		filterService = new FilterService<F,R>(resultForm.getController(), filterForm);
		filterService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@SuppressWarnings("unchecked")
			@Override
            public void handle(WorkerStateEvent t) {
				try {
	            	ResultFilterPair<F, R> result = (ResultFilterPair<F,R>)t.getSource().getValue();
					resultForm.getController().showFilteredResult(result.result, result.usedFilter);
				} catch (Exception e){
					e.printStackTrace(); //Future swollows Exceptions
					if (e instanceof RuntimeException){
						throw (RuntimeException)e;
					} else {
						throw new RuntimeException(e);
					}
				}
            }
        });
		repeatFilterService = new RepeatFilterService<F,R>(resultForm.getController(), filterForm);
	}
	
	public FilterController<F> getFilterController(){
		return filterForm.getController();
	}
	
	public F getFilter(){
		return filterForm.getController().getFilter();
	}

	protected void beforFilterHook(HBox filterbox){}
	
	
	@Override
	public Node createContent() {
		final StackPane stackPane = new StackPane();
		filterService.stateProperty().addListener(new ChangeListener<Worker.State>() {
			ProgressIndicator indicator = ComponentUtil.createProgressIndicator();
			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
				if (newValue==State.RUNNING){
					stackPane.getChildren().add(indicator);
				} else {
					stackPane.getChildren().remove(indicator);
				}
			}
		});
		
		
		BorderPane borderPane = new BorderPane();
		stackPane.getChildren().add(borderPane);
		
		HBox filterbox = new HBox();
		filterbox.setAlignment(Pos.CENTER);
		filterbox.setSpacing(5);
		
		beforFilterHook(filterbox);
		
		StackPane filterboxStackPane = new StackPane();
		filterbox.getChildren().add(filterboxStackPane);
		Node filterContent=null;
		if (filterForm.getController().supportsFiltering()){
			filterContent = filterForm.createContent();
			HBox.setHgrow(filterboxStackPane, Priority.ALWAYS);
			filterboxStackPane.getChildren().add(filterContent);
			filterbox.getChildren().add(new Separator(Orientation.VERTICAL));
		}
		
		final Button refreshButton = new Button("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/refresh.png"))));
		refreshButton.setTooltip(new Tooltip(messageProvider.getText(MessageKey.filterAbleForm_button_refresh)));
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	filterService.reset();
		    	filterService.start();
		    }
		});
		filterbox.getChildren().add(refreshButton);
		HBox.setMargin(refreshButton, new Insets(5));

		
		final Button clearButton = new Button("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/clear.png"))));
		clearButton.setTooltip(new Tooltip(messageProvider.getText(MessageKey.filterAbleForm_button_clear)));
		clearButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	resultForm.getController().clear();
		    }
		});
		filterbox.getChildren().add(clearButton);
		
		if (resultForm.getController().canLimitResult()){
			TextField maxCountTextField = new TextField();
			maxCountTextField.setPrefWidth(70);
			maxCountTextField.textProperty().bindBidirectional(copperDataProvider.getMaxResultCount(), new IntegerStringConverter());
			
			
			Label label = new Label("Limit rows:");
			label.setLabelFor(maxCountTextField);
			filterbox.getChildren().add(label);
			filterbox.getChildren().add(maxCountTextField);
		}
		
		BorderPane wrapper = new BorderPane();
		BorderPane.setAlignment(filterbox, Pos.CENTER_LEFT);
		wrapper.setCenter(filterbox);
		wrapper.setBottom(new Separator(Orientation.HORIZONTAL));
		borderPane.setTop(wrapper);
		borderPane.setCenter(resultForm.createContent());
		
		final ProgressIndicator repeatProgressIndicator = new ProgressBar();
		filterboxStackPane.getChildren().add(repeatProgressIndicator);
		final ToggleButton toggleButton = new ToggleButton("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/repeat.png"))));
		HBox.setMargin(toggleButton, new Insets(0, 5, 0, 0));
		BorderPane.setMargin(refreshButton, new Insets(5));
		repeatProgressIndicator.setVisible(false);
		repeatProgressIndicator.setPrefWidth(300);
		repeatProgressIndicator.progressProperty().bind(repeatFilterService.progressProperty());
		toggleButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue){
					repeatFilterService.setRefreshIntervall(Long.valueOf(refreshRateInMs.get()));
					repeatFilterService.reset();
					repeatFilterService.start();
					repeatProgressIndicator.setVisible(true);
				} else {
					repeatFilterService.cancel();
					repeatProgressIndicator.setVisible(false);
				}
			}
		});
	
		filterbox.getChildren().add(toggleButton);
	
		
		MenuButton settings = new MenuButton("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/settings.png"))));
		settings.setPrefWidth(20);
		CustomMenuItem customMenuItem = new CustomMenuItem();
		settings.getItems().add(customMenuItem);
		customMenuItem.getStyleClass().setAll("noSelectAnimationMenueItem","menu-item");
		HBox hbox = new HBox(3);
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.getChildren().add(new Label("Refresh Interval"));
		TextField interval = new NumerOnlyTextField();
		interval.setPrefWidth(100);
		interval.textProperty().bindBidirectional(refreshRateInMs);
		hbox.getChildren().add(interval);
		hbox.getChildren().add(new Label("ms"));
		customMenuItem.setContent(hbox);
		filterbox.getChildren().add(settings);
		
		
		refreshButton.disableProperty().bind(toggleButton.selectedProperty());
		clearButton.disableProperty().bind(toggleButton.selectedProperty());
		if (filterContent!=null){
			filterContent.disableProperty().bind(toggleButton.selectedProperty());
		}
		settings.disableProperty().bind(toggleButton.selectedProperty());
		
		
		filterService.reset();
		filterService.start();
		
		return stackPane;
	}
	
	SimpleStringProperty refreshRateInMs = new SimpleStringProperty("1000");

	public static class RepeatFilterService<F,R>  extends Service<Void> {
    	private long refreshRate=1000;
		long lasttime = System.currentTimeMillis();
        private final FilterResultController<F,R> filterResultController;
        private final Form<FilterController<F>> filterForm;
        
        public RepeatFilterService(FilterResultController<F,R> filterResultController, Form<FilterController<F>> filterForm) {
			super();
			this.filterResultController = filterResultController;
			this.filterForm = filterForm;
		}
        
		public void setRefreshIntervall(long refreshRate) {
			this.refreshRate = refreshRate;
		}

		@Override
		public void start() {
			lasttime = System.currentTimeMillis();
			super.start();
		}
        
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					while (!isCancelled()) {
						if (lasttime + refreshRate < System.currentTimeMillis()) {
							updateProgress(-1, 1);
							final List<R> result = filterResultController.applyFilterInBackgroundThread(filterForm.getController().getFilter());
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									try {
										filterResultController.showFilteredResult(result, filterForm.getController().getFilter());
									} catch (Exception e) {
										e.printStackTrace(); // Future swollows Exceptions
										if (e instanceof RuntimeException) {
											throw (RuntimeException) e;
										} else {
											throw new RuntimeException(e);
										}
									}
								}
							});
							lasttime = System.currentTimeMillis();
						}
						Thread.sleep(50);
						long progress = System.currentTimeMillis() - lasttime;
						updateProgress(progress <= refreshRate ? progress : refreshRate, refreshRate);
					}
					return null;
				}
			};
		}
    }

    public static class FilterService<F,R>  extends Service<ResultFilterPair<F,R>> {
        private final FilterResultController<F,R> filterResultController;
        private final Form<FilterController<F>> filterForm;
        
        public FilterService(FilterResultController<F,R> filterResultController, Form<FilterController<F>> filterForm) {
			super();
			this.filterResultController = filterResultController;
			this.filterForm = filterForm;
		}

		@Override
		protected Task<ResultFilterPair<F,R>> createTask() {
			return new Task<ResultFilterPair<F,R>>() {
                @Override
				protected ResultFilterPair<F,R> call() throws Exception {
					try {
						List<R> result = filterResultController.applyFilterInBackgroundThread(filterForm.getController().getFilter());
						return new ResultFilterPair<F, R>(result, filterForm.getController().getFilter());
					} catch (Exception e) {
						e.printStackTrace(); // Future swollows Exceptions
						if (e instanceof RuntimeException) {
							throw (RuntimeException) e;
						} else {
							throw new RuntimeException(e);
						}
					}
				}
            };
        }
    }
    
    public static class ResultFilterPair<F,R> {
        public List<R> result;
        public F usedFilter;
        
        public ResultFilterPair(List<R> result, F usedFilter) {
			super();
			this.result = result;
			this.usedFilter = usedFilter;
		}
    }
}
