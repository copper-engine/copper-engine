package de.scoopgmbh.copper.gui.form.filter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
		Node filter = filterForm.createContent();
		filterbox.getChildren().add(filter);
		HBox.setHgrow(filter, Priority.ALWAYS);
		Button refreshButton = new Button(messageProvider.getText("FilterAbleForm.button.refresh"));
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	resultForm.getController().applyFilter(filterForm.getController().getFilter());
		    }
		});
		filterbox.getChildren().add(refreshButton);
		TextField maxCountTextField = new TextField();
		maxCountTextField.textProperty().bindBidirectional(copperDataProvider.getMaxResultCount(), new IntegerStringConverter());
		filterbox.getChildren().add(maxCountTextField);
		borderPane.setTop(filterbox);
		borderPane.setCenter(resultForm.createContent());
		return borderPane;
	}

}
