package de.scoopgmbh.copper.gui.form.filter;

import java.util.List;

import de.scoopgmbh.copper.gui.form.FxmlController;

/**
 *
 * @param <F>Filtermodel
 * @param <T>Resultmodel
 */
public interface FilterResultController<F,R> extends FxmlController{
	
	/**update gui
	 * executed in JavaFX Application Thread
	 */
	public void showFilteredResult(List<R> filteredResult, F usedFilter);
	public List<R> applyFilterInBackgroundThread(F filter);
}
