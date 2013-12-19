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
package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;

public class AdapterCallInfo extends AdapterEventBase implements Serializable {
    private static final long serialVersionUID = 6386090675365126626L;

    String method;
    String parameter;
    WorkflowInstanceInfo workflow;

    public AdapterCallInfo() {
        super();
    }

    public AdapterCallInfo(String method, String parameter, Date timestamp, String adapterName, WorkflowInstanceInfo workflow) {
        super(adapterName, timestamp);
        this.method = method;
        this.parameter = parameter;
        this.workflow = workflow;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public WorkflowInstanceInfo getWorkflow() {
        return workflow;
    }

    public void setWorkflow(WorkflowInstanceInfo workflow) {
        this.workflow = workflow;
    }

}
