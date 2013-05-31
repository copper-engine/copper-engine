package de.scoopgmbh.copper.monitoring.client.form.filter;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public abstract class BaseFilterController<T> implements FilterController<T> {

	protected ListProperty<ActionsWithFilterForm> actionsWithFilterForm = 
			new SimpleListProperty<ActionsWithFilterForm>(FXCollections.<ActionsWithFilterForm>observableArrayList());

	@Override
	public ListProperty<ActionsWithFilterForm> getActionsWithFilterForm() {
		return actionsWithFilterForm;
	}

}
