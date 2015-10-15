/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.util;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import org.copperengine.monitoring.core.model.WorkflowClassMetaData;

public class WorkflowVersion {
    public final SimpleStringProperty classname = new SimpleStringProperty();
    public final SimpleStringProperty alias = new SimpleStringProperty();
    public final SimpleObjectProperty<Long> versionMajor = new SimpleObjectProperty<Long>();
    public final SimpleObjectProperty<Long> versionMinor = new SimpleObjectProperty<Long>();
    public final SimpleObjectProperty<Long> patchlevel = new SimpleObjectProperty<Long>();
    public final SimpleStringProperty source = new SimpleStringProperty();

    public WorkflowVersion() {

    }

    public WorkflowVersion(String classname, String alias, long versionMajor, long versionMinor, long patchlevel, String source) {
        this.classname.setValue(classname);
        this.alias.setValue(alias);
        this.versionMajor.setValue(versionMajor);
        this.versionMinor.setValue(versionMinor);
        this.patchlevel.setValue(patchlevel);
        this.source.setValue(source);
    }

    public WorkflowVersion(WorkflowClassMetaData workflowClassesInfo) {
        this.classname.set(workflowClassesInfo.getClassname());
        this.alias.set(workflowClassesInfo.getAlias());
        this.versionMajor.set(workflowClassesInfo.getMajorVersion());
        this.versionMinor.set(workflowClassesInfo.getMinorVersion());
        this.patchlevel.set(workflowClassesInfo.getPatchLevel());
        this.source.set(workflowClassesInfo.getSource());
    }

    public void setAllFrom(WorkflowVersion workflowVersion) {
        classname.set(workflowVersion.classname.get());
        versionMajor.set(workflowVersion.versionMajor.get());
        versionMinor.set(workflowVersion.versionMinor.get());
        patchlevel.set(workflowVersion.patchlevel.get());
    }

    public WorkflowClassMetaData convert() {
        return new WorkflowClassMetaData(
                classname.get(),
                alias.get(),
                versionMajor.get(),
                versionMinor.get(),
                patchlevel.get(),
                source.get());
    }

    @Override
    public String toString() {
        return "WorkflowVersion [classname=" + classname.get() + ", alias=" + alias.get() + ", versionMajor=" + versionMajor.get() + ", versionMinor="
                + versionMinor.get() + ", patchlevel=" + patchlevel.get() + "]";
    }

}
