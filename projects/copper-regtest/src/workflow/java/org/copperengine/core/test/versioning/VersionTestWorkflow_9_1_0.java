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
package org.copperengine.core.test.versioning;

import org.copperengine.core.Interrupt;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowDescription;
import org.copperengine.core.util.BlockingResponseReceiver;

@WorkflowDescription(alias = VersionTestWorkflowDef.NAME, majorVersion = 9, minorVersion = 1, patchLevelVersion = 0)
public class VersionTestWorkflow_9_1_0 extends Workflow<BlockingResponseReceiver<String>> {

    private static final long serialVersionUID = 1L;

    @Override
    public void main() throws Interrupt {
        getData().setResponse(this.getClass().getName());
    }

}
