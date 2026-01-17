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
package org.copperengine.core;

import java.io.Serializable;
import java.util.List;

import org.slf4j.LoggerFactory;

/**
 * COPPER Auditor, that is called in workflow processing at several situations.
 * <p>
 * If you want to use an Auditor in a Workflow, then you have to implement this
 * interface.
 * <p>
 * A typical use case to log to the AuditTrail in these situations enriched with
 * more information got from the Workflow instance.
 */
public interface Auditor extends Serializable {

    /**
     * Is called before first continuation of Workflow.
     */
    default void start() {
        LoggerFactory.getLogger(this.getClass()).trace("start");
    }

    /**
     * Is called before continuation of Workflow is interrupted.
     *
     * @param jumpNos list of jumps to the next continuation of Workflow
     */
    default void interrupt(final List<Integer> jumpNos) {
        LoggerFactory.getLogger(this.getClass()).trace("interrupt {}", jumpNos);
    }

    /**
     * Is called before continuation of Workflow is resumed.
     *
     * @param jumpNos list of jumps to the next continuation of Workflow
     */
    default void resume(final List<Integer> jumpNos) {
        LoggerFactory.getLogger(this.getClass()).trace("resume {}", jumpNos);
    }

    /**
     * Is called after last continuation of Workflow.
     */
    default void end() {
        LoggerFactory.getLogger(this.getClass()).trace("end");
    }

    /**
     * Is called before an exception in Workflow
     * that leads to Workflow instance state "ERROR".
     *
     * @param jumpNos list of jumps to the next continuation of Workflow
     */
    default void exception(final List<Integer> jumpNos) {
        LoggerFactory.getLogger(this.getClass()).warn("exception {}", jumpNos);
    }
}
