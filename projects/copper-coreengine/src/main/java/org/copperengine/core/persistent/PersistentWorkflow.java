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
package org.copperengine.core.persistent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.Acknowledge.DefaultAcknowledge;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.Workflow;

/**
 * Abstract base class for persistent workflows.
 * It is safe to run a PersistentWorkflow in a transient engine. So if your want to keep it open to decide later whether
 * your
 * workflow needs persistence or not, it is OK to inherit from PersistentWorkflow.
 *
 * @param <E>
 * @author austermann
 */
public abstract class PersistentWorkflow<E extends Serializable> extends Workflow<E> implements Serializable, SavepointAware {

    private static final long serialVersionUID = 3232137844188440549L;

    transient RegisterCall registerCall;
    transient Set<String> waitCidList;
    transient List<String> responseIdList;
    transient String rowid;
    transient String oldProcessorPoolId;
    transient int oldPrio;
    transient ArrayList<Acknowledge.DefaultAcknowledge> checkpointAcknowledges = null;
    transient ArrayList<SavepointAware> savepointAwares = null;

    void addWaitCorrelationId(final String cid) {
        if (waitCidList == null)
            waitCidList = new HashSet<String>();
        waitCidList.add(cid);
    }

    void addResponseId(final String responseId) {
        if (responseIdList == null)
            responseIdList = new ArrayList<String>();
        responseIdList.add(responseId);
    }

    public RegisterCall getRegisterCall() {
        return registerCall;
    }

    /**
     * Used internally
     */
    @SuppressWarnings("unchecked")
    public void setDataAsObject(Object data) {
        setData((E) data);
    }

    public void onLoad(PersistenceContext pc) {
    }

    @Override
    public void onSave(PersistenceContext pc) {
        if (savepointAwares != null) {
            for (SavepointAware sa : savepointAwares) {
                sa.onSave(pc);
            }
        }
    }

    public void onDelete(PersistenceContext pc) {
    }

    @Override
    protected Acknowledge createCheckpointAcknowledge() {
        return new Acknowledge.DefaultAcknowledge();
    }

    @Override
    protected void registerCheckpointAcknowledge(Acknowledge ack) {
        if (ack instanceof Acknowledge.DefaultAcknowledge) {
            if (checkpointAcknowledges == null)
                checkpointAcknowledges = new ArrayList<Acknowledge.DefaultAcknowledge>();
            checkpointAcknowledges.add((DefaultAcknowledge) ack);
        }
    }

    @Override
    protected void registerSavepointAware(SavepointAware sa) {
        if (savepointAwares == null)
            savepointAwares = new ArrayList<SavepointAware>();
        savepointAwares.add(sa);
    }

    public boolean flushCheckpointAcknowledges() {
        if (checkpointAcknowledges == null)
            return true;
        for (Acknowledge.DefaultAcknowledge ack : checkpointAcknowledges) {
            try {
                ack.waitForAcknowledge();
            } catch (CopperRuntimeException ce) {
                return false;
            }
        }
        return true;
    }

}
