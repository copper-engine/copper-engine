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
package org.copperengine.core.persistent.adapter;

import java.lang.reflect.Method;

import org.copperengine.core.persistent.PersistentEntity;

public class AdapterCall extends PersistentEntity {

    private static final long serialVersionUID = 1L;

    String workflowId;
    long priority;

    final String entityId;
    final String adapterId;
    final Method method;
    final Object[] args;

    public AdapterCall(String adapterId, String entityId, Method method, Object[] args) {
        this.entityId = entityId;
        this.adapterId = adapterId;
        this.method = method;
        this.args = args;
    }

    public void setWorkflowData(String workflowId, long priority) {
        this.workflowId = workflowId;
        this.priority = priority;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getEntityId() {
        return entityId;
    }

    public long getPriority() {
        return priority;
    }

    public Method getMethod() {
        return method;
    }

    public String getAdapterId() {
        return adapterId;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return adapterId + ": " + method;
    }

}
