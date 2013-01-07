package de.scoopgmbh.copper.gui.form.filter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.ShowFormStrategy;
import de.scoopgmbh.copper.gui.util.MessageProvider;

/**
 *
 * @param <F> Filter
 */
public class FilterAbleForm<F> extends Form<Object>{
	private final Form<FilterController<F>> filterForm;
	private final Form<FilterResultController<F>> resultForm;
	private final GuiCopperDataProvider copperDataProvider;

	public FilterAbleForm(String menueItemtextKey, MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie,
			Form<FilterController<F>> filterForm, Form<FilterResultController<F>> resultForm, GuiCopperDataProvider copperDataProvider ) {
		super(menueItemtextKey, messageProvider, showFormStrategie, null);
		this.filterForm = filterForm;
		this.resultForm = resultForm;
		this.copperDataProvider = copperDataProvider;
	}

	@Override
	public Node createContent() {
		BorderPane borderPane = new BorderPane();
		HBox filterbox = new HBox();
		filterbox.setAlignment(Pos.CENTER);
		filterbox.setSpacing(5);
		
		Node filter = filterForm.createContent();
		HBox.setHgrow(filter, Priority.ALWAYS);
		filterbox.getChildren().add(filter);
		
		final Button refreshButton = new Button(messageProvider.getText("FilterAbleForm.button.refresh"));
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	resultForm.getController().applyFilter(filterForm.getController().getFilter());
		    }
		});
		filterbox.getChildren().add(refreshButton);
		TextField maxCountTextField = new TextField();
		maxCountTextField.setPrefWidth(70);
		maxCountTextField.textProperty().bindBidirectional(copperDataProvider.getMaxResultCount(), new IntegerStringConverter());
		Label label = new Label("Limit rows:");
		label.setLabelFor(maxCountTextField);
		filterbox.getChildren().add(label);
		filterbox.getChildren().add(maxCountTextField);
		borderPane.setTop(filterbox);
		borderPane.setCenter(resultForm.createContent());
		
		
		final ProgressIndicator progressIndicator = new ProgressBar();
		final Timeline refresh = new Timeline(new KeyFrame(Duration.millis(200), new EventHandler<ActionEvent>() {
			private static final int REFRESH_RATE = 1000;
			long lasttime = System.currentTimeMillis();
			@Override
			public void handle(ActionEvent event) {
				progressIndicator.setProgress((System.currentTimeMillis()-lasttime)/(double)REFRESH_RATE);
				if (lasttime+REFRESH_RATE<System.currentTimeMillis()){
					resultForm.getController().applyFilter(filterForm.getController().getFilter());
					lasttime=System.currentTimeMillis();
					progressIndicator.setProgress(0);
				}
			}
		}));
		refresh.setCycleCount(Timeline.INDEFINITE);
		final ToggleButton toggleButton = new ToggleButton("autorefresh");
		HBox.setMargin(toggleButton, new Insets(0, 5, 0, 0));
		BorderPane.setMargin(refreshButton, new Insets(5));
		progressIndicator.setPrefWidth(30);
		progressIndicator.setVisible(false);
		
		toggleButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue){
					refresh.play();
					progressIndicator.setVisible(true);
					refreshButton.setDisable(true);
				} else {
					refresh.stop();
					progressIndicator.setVisible(false);
					refreshButton.setDisable(false);
				}
			}
		});
	
		filterbox.getChildren().add(progressIndicator);
		filterbox.getChildren().add(toggleButton);
		
		return borderPane;
	}

}
