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

class QueueElement {

    public final String wfId;
    public final int prio;
    public final long enqueueTS = System.currentTimeMillis();

    public QueueElement(String wfId, int prio) {
        this.wfId = wfId;
        this.prio = prio;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((wfId == null) ? 0 : wfId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QueueElement other = (QueueElement) obj;
        if (wfId == null) {
            if (other.wfId != null)
                return false;
        } else if (!wfId.equals(other.wfId))
            return false;
        return true;
    }

}
