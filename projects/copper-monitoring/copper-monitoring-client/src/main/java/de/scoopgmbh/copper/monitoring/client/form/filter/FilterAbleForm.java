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
package de.scoopgmbh.copper.monitoring.client.form.filter;

import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.Form;
import de.scoopgmbh.copper.monitoring.client.form.ShowFormStrategy;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterController.ActionsWithFilterForm;
import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;
import de.scoopgmbh.copper.monitoring.client.util.MessageKey;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;
import de.scoopgmbh.copper.monitoring.client.util.NumberOnlyTextField;

/**
 * A Form with a filter form and a result form
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
	
	public static final String REFRESH_BUTTON_ID = "refreshbutton";

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
					e.printStackTrace(); //Future swallows Exceptions
					if (Thread.getDefaultUncaughtExceptionHandler()!=null){
						Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
					}
					throw new RuntimeException(e);
				}
            }
        });
		repeatFilterService = new RepeatFilterService<F,R>(resultForm.getController(), filterForm);
		
		filterForm.getController().getActionsWithFilterForm().addListener(new ListChangeListener<ActionsWithFilterForm>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends ActionsWithFilterForm> c) {
				c.next();
				for (ActionsWithFilterForm actionsWithFilterForm: c.getAddedSubList()){
					actionsWithFilterForm.run(FilterAbleForm.this);
				}
			}
		});
	}
	
	boolean verticalRightButton=false;
	public void useVerticalRightButton(){
		verticalRightButton=true;
	}
	
	public FilterController<F> getFilterController(){
		return filterForm.getController();
	}
	
	public F getFilter(){
		return filterForm.getController().getFilter();
	}
	
	@Override
	public Node createContent() {
		
		final StackPane masterStackPane = new StackPane();
		masterStackPane.setOnKeyReleased(new EventHandler<javafx.scene.input.KeyEvent>() {
			@Override
			public void handle(javafx.scene.input.KeyEvent event) {
				KeyCodeCombination keyCombination = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN);
				if (keyCombination.match(event)){
					refresh();
				}
			}
		});
		
		filterService.stateProperty().addListener(new ChangeListener<Worker.State>() {
			Node indicator = ComponentUtil.createProgressIndicator();
			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
				if (newValue==State.RUNNING){
					masterStackPane.getChildren().add(indicator);
				} else {
					masterStackPane.getChildren().remove(indicator);
				}
			}
		});
		
		
		BorderPane masterBorderPane = new BorderPane();
		masterStackPane.getChildren().add(masterBorderPane);
		
		BorderPane filterBorderPane = new BorderPane();
		final Node leftFilterPart = createLeftFilterPart();
		filterBorderPane.setLeft(leftFilterPart);
		Node filterContent=this.createFilterContent();
		StackPane filterContentStackPane = new StackPane();
		filterBorderPane.setCenter(filterContentStackPane);
		if (filterForm.getController().supportsFiltering()){
			filterContentStackPane.getChildren().add(filterContent);
		}
		Node rightButtons = createRightFilterButtons(filterContent, leftFilterPart, filterContentStackPane);
		BorderPane.setMargin(rightButtons, new Insets(0,3,0,3));
		filterBorderPane.setRight(rightButtons);
		filterBorderPane.setBottom(new Separator(Orientation.HORIZONTAL));

		masterBorderPane.setTop(filterBorderPane);
		masterBorderPane.setCenter(resultForm.createContent());
	

		filterService.reset();
		filterService.start();
		refreshRateInMs.set(Long.toString(filterForm.getController().getDefaultRefreshIntervall()));
		
		return masterStackPane;
	}
	
	private Node createRightFilterButtons(Node filterContent, Node leftFilterPart, Pane progressbarDisplayTarget){
		Pane buttonsPane;
		if (verticalRightButton){
			VBox vbox = new VBox();
			vbox.setAlignment(Pos.TOP_LEFT);
			vbox.setSpacing(3);
			Region spacer = new Region();
			VBox.setMargin(spacer, new Insets(1.5));
			vbox.getChildren().add(spacer);
			buttonsPane=vbox;;
		} else {
			HBox hbox = new HBox();
			hbox.setAlignment(Pos.CENTER);
			hbox.setSpacing(3);
			buttonsPane=hbox;
			buttonsPane.getChildren().add(new Separator(Orientation.VERTICAL));
		}
		
		
	
		final Button clearButton = new Button("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/clear.png"))));
		clearButton.setTooltip(new Tooltip(messageProvider.getText(MessageKey.filterAbleForm_button_clear)));
		clearButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	resultForm.getController().clear();
		    }
		});
		buttonsPane.getChildren().add(clearButton);
		
		if (resultForm.getController().canLimitResult()){
			TextField maxCountTextField = new TextField();
			maxCountTextField.setPrefWidth(70);
			maxCountTextField.textProperty().bindBidirectional(resultForm.getController().maxResultCountProperty(), new IntegerStringConverter());
			
			Label label = new Label("Limit rows:");
			label.setLabelFor(maxCountTextField);
			buttonsPane.getChildren().add(label);
			buttonsPane.getChildren().add(maxCountTextField);
		}
		
		final Button refreshButton = new Button("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/refresh.png"))));
		refreshButton.setId(REFRESH_BUTTON_ID);
		HBox.setMargin(refreshButton, new Insets(4,0,4,0));
		refreshButton.setTooltip(new Tooltip(messageProvider.getText(MessageKey.filterAbleForm_button_refresh)));
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent e) {
		    	refresh();
		    }
		});
		buttonsPane.getChildren().add(refreshButton);
		
		final ProgressIndicator repeatProgressIndicator = new ProgressBar();
		progressbarDisplayTarget.getChildren().add(repeatProgressIndicator);
		final ToggleButton toggleButton = new ToggleButton("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/repeat.png"))));
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
		buttonsPane.getChildren().add(toggleButton);
		
		
		MenuButton settings = new MenuButton("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/settings.png"))));
		settings.setPrefWidth(20);
		CustomMenuItem customMenuItem = new CustomMenuItem();
		settings.getItems().add(customMenuItem);
		customMenuItem.getStyleClass().setAll("noSelectAnimationMenueItem","menu-item");
		HBox hbox = new HBox(3);
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.getChildren().add(new Label("Refresh Interval"));
		TextField interval = new NumberOnlyTextField();
		interval.setPrefWidth(100);
		interval.textProperty().bindBidirectional(refreshRateInMs);
		hbox.getChildren().add(interval);
		hbox.getChildren().add(new Label("ms"));
		customMenuItem.setContent(hbox);
		buttonsPane.getChildren().add(settings);
		
		
		refreshButton.disableProperty().bind(toggleButton.selectedProperty());
		clearButton.disableProperty().bind(toggleButton.selectedProperty());
		filterContent.disableProperty().bind(toggleButton.selectedProperty());
		leftFilterPart.disableProperty().bind(toggleButton.selectedProperty());
		settings.disableProperty().bind(toggleButton.selectedProperty());
		return buttonsPane;
	}
	
	private Node createFilterContent(){
		final Node content = filterForm.createContent();
		if (content==null){
			return new Pane();
		}
		return content;
	}
	
	public void refresh() {
		filterService.reset();
    	filterService.start();
	}

	
	/**
	 * hook for child classes default is empty 
	 */
	protected Node createLeftFilterPart(){
		return new Pane();
	}
	
	SimpleStringProperty refreshRateInMs = new SimpleStringProperty();

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
							final List<R> result;
							try {
								result = filterResultController.applyFilterInBackgroundThread(filterForm.getController().getFilter());
							} catch (Exception e1) {
								throw new RuntimeException(e1);
							}
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									try {
										filterResultController.showFilteredResult(result, filterForm.getController().getFilter());
									} catch (Exception e) {
										e.printStackTrace(); // Future swallows Exceptions
										if (Thread.getDefaultUncaughtExceptionHandler()!=null){
											Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
										}
										throw new RuntimeException(e);
									}
								}
							});
							lasttime = System.currentTimeMillis();
						}
						Thread.sleep(Math.min(50,refreshRate/10));
						long progress = System.currentTimeMillis() - lasttime;
						progress = progress <= refreshRate ? progress : refreshRate;
						if (refreshRate<=500){
							progress=-1;
						}
						updateProgress(progress, refreshRate);
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
						final List<R> result = filterResultController.applyFilterInBackgroundThread(filterForm.getController().getFilter());
						return new ResultFilterPair<F, R>(result, filterForm.getController().getFilter());
					} catch (Exception e) {
						e.printStackTrace(); // Future swollows Exceptions
						if (Thread.getDefaultUncaughtExceptionHandler()!=null){
							Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
						}
						throw new RuntimeException(e);
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
