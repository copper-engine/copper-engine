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
package org.copperengine.core.tranzient;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;

/**
 * Internally used class.
 * 
 * @author austermann
 */
class CorrelationSet {

    private String workflowId;
    private List<String> correlationIds;
    private List<String> missingCorrelationIds;
    private WaitMode mode;
    private Long timeoutTS;

    public CorrelationSet(Workflow<?> workflow, List<String> missingCorrelationIds, WaitMode mode, Long timeoutTS) {
        this.workflowId = workflow.getId();
        this.missingCorrelationIds = new LinkedList<String>(missingCorrelationIds);
        this.correlationIds = new ArrayList<String>(this.missingCorrelationIds);
        this.mode = mode;
        this.timeoutTS = timeoutTS;
    }

    public CorrelationSet(Workflow<?> workflow, String[] missingCorrelationIds, WaitMode mode, Long timeoutTS) {
        this.workflowId = workflow.getId();
        this.missingCorrelationIds = new LinkedList<String>();
        for (String s : missingCorrelationIds) {
            this.missingCorrelationIds.add(s);
        }
        this.correlationIds = new ArrayList<String>(this.missingCorrelationIds);
        this.mode = mode;
        this.timeoutTS = timeoutTS;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public List<String> getMissingCorrelationIds() {
        return missingCorrelationIds;
    }

    public WaitMode getMode() {
        return mode;
    }

    public Long getTimeoutTS() {
        return timeoutTS;
    }

    public List<String> getCorrelationIds() {
        return correlationIds;
    }

}
