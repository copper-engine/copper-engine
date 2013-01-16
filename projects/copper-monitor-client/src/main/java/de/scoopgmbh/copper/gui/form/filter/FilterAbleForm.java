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
import javafx.scene.control.Label;
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
import de.scoopgmbh.copper.gui.util.MessageProvider;

/**
 * @param <F> FilterModel
 * @param <R> ResultModel
 */
public class FilterAbleForm<F,R> extends Form<Object>{
	private final Form<FilterController<F>> filterForm;
	private final Form<FilterResultController<F,R>> resultForm;
	private final GuiCopperDataProvider copperDataProvider;
	private FilterService<F,R> filterService;
	private RepeatFilterService<F,R> repeatFilterService;

	public FilterAbleForm(String menueItemtextKey, MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie,
			Form<FilterController<F>> filterForm, final Form<FilterResultController<F,R>> resultForm, GuiCopperDataProvider copperDataProvider ) {
		super(menueItemtextKey, messageProvider, showFormStrategie, null);
		this.filterForm = filterForm;
		this.resultForm = resultForm;
		this.copperDataProvider = copperDataProvider;
		filterService = new FilterService<>(resultForm.getController(), filterForm);
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
		repeatFilterService = new RepeatFilterService<>(resultForm.getController(), filterForm);
	}
	
	public FilterController<F> getFilterController(){
		return filterForm.getController();
	}
	
	public F getFilter(){
		return filterForm.getController().getFilter();
	}

	@Override
	public Node createContent() {
		final StackPane stackPane = new StackPane();
		filterService.stateProperty().addListener(new ChangeListener<Worker.State>() {
			ProgressIndicator indicator = new ProgressIndicator();
			{
				indicator.setStyle("-fx-background-color: rgba(230,230,230,0.7);");
			}
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
		
		Node filter = filterForm.createContent();
		HBox.setHgrow(filter, Priority.ALWAYS);
		filterbox.getChildren().add(filter);
		filterbox.getChildren().add(new Separator(Orientation.VERTICAL));
		
		final Button refreshButton = new Button("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/refresh.png"))));
		refreshButton.setTooltip(new Tooltip(messageProvider.getText("FilterAbleForm.button.refresh")));
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	filterService.reset();
		    	filterService.start();
		    }
		});
		filterbox.getChildren().add(refreshButton);

		
		final Button clearButton = new Button("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/clear.png"))));
		clearButton.setTooltip(new Tooltip(messageProvider.getText("FilterAbleForm.button.clear")));
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
		wrapper.setCenter(filterbox);
		wrapper.setBottom(new Separator(Orientation.HORIZONTAL));
		borderPane.setTop(wrapper);
		borderPane.setCenter(resultForm.createContent());
		
		final ProgressIndicator repeatProgressIndicator = new ProgressBar();
		final ToggleButton toggleButton = new ToggleButton("",new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/repeat.png"))));
		HBox.setMargin(toggleButton, new Insets(0, 5, 0, 0));
		BorderPane.setMargin(refreshButton, new Insets(5));
		repeatProgressIndicator.setPrefWidth(30);
		repeatProgressIndicator.setVisible(false);
		repeatProgressIndicator.progressProperty().bind(repeatFilterService.progressProperty());
		
		toggleButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue){
					repeatFilterService.reset();
					repeatFilterService.start();
					repeatProgressIndicator.setVisible(true);
				} else {
					repeatFilterService.cancel();
					repeatProgressIndicator.setVisible(false);
				}
			}
		});
	
		filterbox.getChildren().add(repeatProgressIndicator);
		filterbox.getChildren().add(toggleButton);
		
		refreshButton.disableProperty().bind(toggleButton.selectedProperty());
		clearButton.disableProperty().bind(toggleButton.selectedProperty());
		
		filterService.reset();
		filterService.start();
		
		return stackPane;
	}
	
    public static class RepeatFilterService<F,R>  extends Service<Void> {
    	private static final int REFRESH_RATE = 1000;
		long lasttime = System.currentTimeMillis();
        private final FilterResultController<F,R> filterResultController;
        private final Form<FilterController<F>> filterForm;
        
        public RepeatFilterService(FilterResultController<F,R> filterResultController, Form<FilterController<F>> filterForm) {
			super();
			this.filterResultController = filterResultController;
			this.filterForm = filterForm;
		}
        
		@Override
		public void start() {
			lasttime = System.currentTimeMillis();
			super.start();
		}
        
		protected Task<Void> createTask() {
			return new Task<Void>() {
				protected Void call() throws Exception {
					while (!isCancelled()) {
						if (lasttime + REFRESH_RATE < System.currentTimeMillis()) {
							updateProgress(-1, 1);
							final List<R> result = filterResultController.applyFilterInBackgroundThread(filterForm.getController().getFilter());
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									filterResultController.showFilteredResult(result, filterForm.getController().getFilter());
								}
							});
							lasttime = System.currentTimeMillis();
						}
						Thread.sleep(50);
						long progress = System.currentTimeMillis() - lasttime;
						updateProgress(progress <= REFRESH_RATE ? progress : REFRESH_RATE, REFRESH_RATE);
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

		protected Task<ResultFilterPair<F,R>> createTask() {
			return new Task<ResultFilterPair<F,R>>() {
                protected ResultFilterPair<F,R> call() throws Exception {
                	List<R> result = filterResultController.applyFilterInBackgroundThread(filterForm.getController().getFilter());
                	return new ResultFilterPair<F,R>(result,filterForm.getController().getFilter());
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
