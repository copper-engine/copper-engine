package de.scoopgmbh.copper.gui.form.filter;

import de.scoopgmbh.copper.gui.form.FxmlController;

public interface FilterResultController<F> extends FxmlController{
	public void applyFilter(F filter);
}
