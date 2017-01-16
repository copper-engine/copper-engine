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
package org.copperengine.core.persistent.hybrid;

import java.util.Date;
import java.util.Map;

import org.copperengine.core.ProcessingState;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.SerializedWorkflow;

/**
 * DTO representation of a copper workflow instance used in {@link Storage}
 * 
 * @author austermann
 *
 */
public class WorkflowInstance {

    public WorkflowInstance() {
    }

    public String id;
    public String ppoolId;
    public int prio;
    public Date creationTS;
    public SerializedWorkflow serializedWorkflow;
    public Map<String, String> cid2ResponseMap;
    public WaitMode waitMode;
    public Date timeout;
    public ProcessingState state;
    public Date lastModTS;
    public String classname;

    @Override
    public String toString() {
        return "WorkflowInstance [id=" + id + ", ppoolId=" + ppoolId + ", prio=" + prio + ", creationTS=" + creationTS + ", serializedWorkflow=" + serializedWorkflow + ", cid2ResponseMap=" + cid2ResponseMap + ", waitMode=" + waitMode + ", timeout=" + timeout + ", state=" + state + ", lastModTS=" + lastModTS + ", classname=" + classname + "]";
    }

}
