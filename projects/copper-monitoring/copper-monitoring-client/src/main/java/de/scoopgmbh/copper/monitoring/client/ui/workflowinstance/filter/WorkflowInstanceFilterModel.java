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
package de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.filter;

import java.util.Date;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitoring.client.form.filter.defaultfilter.MaxCountFilterModel;
import de.scoopgmbh.copper.monitoring.client.form.filter.enginefilter.EnginePoolFilterModel;
import de.scoopgmbh.copper.monitoring.client.util.WorkflowVersion;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;

public class WorkflowInstanceFilterModel extends EnginePoolFilterModel{
	
	public final SimpleObjectProperty<Date> from = new SimpleObjectProperty<Date>();
	public final SimpleObjectProperty<Date> to = new SimpleObjectProperty<Date>();
	
	public final SimpleObjectProperty<WorkflowInstanceState> state = new SimpleObjectProperty<WorkflowInstanceState>();
	public final SimpleStringProperty priority = new SimpleStringProperty();
	public final WorkflowVersion version = new WorkflowVersion();
	
	public final MaxCountFilterModel maxCountFilterModel = new MaxCountFilterModel();
}
