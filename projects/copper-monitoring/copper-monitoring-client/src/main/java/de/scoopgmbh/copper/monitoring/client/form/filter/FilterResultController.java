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

import javafx.scene.Node;

import de.scoopgmbh.copper.monitoring.client.form.FxmlController;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;

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
	public void clear();
	public boolean supportsClear();
	public List<? extends Node> getContributedButtons(MessageProvider messageProvider);

	
	public void onClose();

}
