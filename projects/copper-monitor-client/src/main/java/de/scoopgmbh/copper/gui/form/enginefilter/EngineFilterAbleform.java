package de.scoopgmbh.copper.gui.form.enginefilter;

import java.util.List;

import javafx.geometry.Orientation;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.ShowFormStrategy;
import de.scoopgmbh.copper.gui.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.gui.form.filter.FilterController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.util.EngineFilter;
import de.scoopgmbh.copper.gui.util.MessageProvider;
import de.scoopgmbh.copper.monitor.adapter.model.EngineDiscriptor;

public class EngineFilterAbleform<F extends EngineFilter, R> extends FilterAbleForm<F, R> {

	public EngineFilterAbleform(String menueItemtextKey, MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie,
			Form<FilterController<F>> filterForm, Form<FilterResultController<F, R>> resultForm, GuiCopperDataProvider copperDataProvider) {
		super(menueItemtextKey, messageProvider, showFormStrategie, filterForm, resultForm, copperDataProvider);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void beforFilterHook(HBox filterbox){
		ChoiceBox<EngineDiscriptor> choicebox = new ChoiceBox<>();
		choicebox.setConverter(new StringConverter<EngineDiscriptor>() {
			@Override
			public String toString(EngineDiscriptor object) {
				return object.getProcessingEngineId()+"("+object.getTyp()+")";
			}
			
			@Override
			public EngineDiscriptor fromString(String string) {
				return null;
			}
		});
		filterForm.getController().getFilter().engine.bind(choicebox.getSelectionModel().selectedItemProperty());
		List<EngineDiscriptor> engineList = copperDataProvider.getEngineList();
		for (EngineDiscriptor engineFilter: engineList){
			choicebox.getItems().add(engineFilter);
		}
		filterbox.getChildren().add(choicebox);
		filterbox.getChildren().add(new Separator(Orientation.VERTICAL));
	}

}
