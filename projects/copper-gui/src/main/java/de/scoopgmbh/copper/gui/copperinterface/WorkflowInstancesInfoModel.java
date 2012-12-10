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
package de.scoopgmbh.copper.gui.copperinterface;

import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class WorkflowInstancesInfoModel {
	
	private final SimpleStringProperty id;
	private final  SimpleStringProperty state;
	private final  SimpleIntegerProperty priority;
	private final  SimpleStringProperty processorPoolId;
	private final  ObjectProperty<Date> timeout;
	
	public WorkflowInstancesInfoModel(String id, String state, int priority, String processorPoolId, Date timeout) {
		super();
		this.id=new SimpleStringProperty(id);
		this.state = new SimpleStringProperty(state);
		this.priority = new SimpleIntegerProperty(priority);
		this.processorPoolId = new SimpleStringProperty(processorPoolId);
		this.timeout = new SimpleObjectProperty<>(timeout);
	}

	public SimpleStringProperty getId() {
		return id;
	}

	public SimpleStringProperty getState() {
		return state;
	}

	public SimpleIntegerProperty getPriority() {
		return priority;
	}

	public SimpleStringProperty getProcessorPoolId() {
		return processorPoolId;
	}

	public ObjectProperty<Date> getTimeout() {
		return timeout;
	}

}