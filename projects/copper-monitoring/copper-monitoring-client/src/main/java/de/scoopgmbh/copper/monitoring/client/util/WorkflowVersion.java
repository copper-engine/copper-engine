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
package de.scoopgmbh.copper.monitoring.client.util;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowClassVersionInfo;

public class WorkflowVersion {
	public final SimpleStringProperty classname = new SimpleStringProperty(); 
	public final SimpleStringProperty alias = new SimpleStringProperty(); 
	public final SimpleObjectProperty<Long> versionMajor = new SimpleObjectProperty<Long>(); 
	public final SimpleObjectProperty<Long> versionMinor = new SimpleObjectProperty<Long>(); 
	public final SimpleObjectProperty<Long> patchlevel = new SimpleObjectProperty<Long>(); 
	
	public WorkflowVersion(){

	} 
	
	public WorkflowVersion(String classname, String alias, long versionMajor,long versionMinor, long patchlevel){
		this.classname.setValue(classname);
		this.alias.setValue(alias);
		this.versionMajor.setValue(versionMajor);
		this.versionMinor.setValue(versionMinor);
		this.patchlevel.setValue(patchlevel);
	}
	
	public WorkflowVersion(WorkflowClassVersionInfo workflowClassesInfo){
		this.classname.setValue(workflowClassesInfo.getClassname());
		this.alias.setValue(workflowClassesInfo.getAlias());
		this.versionMajor.setValue(workflowClassesInfo.getMajorVersion());
		this.versionMinor.setValue(workflowClassesInfo.getMinorVersion());
		this.patchlevel.setValue(workflowClassesInfo.getPatchLevel());
	}
	
	public void setAllFrom(WorkflowVersion workflowVersion){
		classname.set(workflowVersion.classname.get());
		versionMajor.set(workflowVersion.versionMajor.get());
		versionMinor.set(workflowVersion.versionMinor.get());
		patchlevel.set(workflowVersion.patchlevel.get());
	}
	
	public WorkflowClassVersionInfo convert(){
		return new WorkflowClassVersionInfo(classname.getValue(),alias.getValue(),versionMajor.getValue(),versionMinor.getValue(),patchlevel.getValue());
	}
	
	
}
