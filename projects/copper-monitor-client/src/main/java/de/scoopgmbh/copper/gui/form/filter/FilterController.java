package de.scoopgmbh.copper.gui.form.filter;

import de.scoopgmbh.copper.gui.form.FxmlController;

public interface FilterController<F> extends FxmlController {
	public F getFilter();
}
