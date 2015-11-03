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

import java.util.Comparator;

class QueueElementComparator implements Comparator<QueueElement> {
    @Override
    public int compare(QueueElement o1, QueueElement o2) {
        if (o1.prio != o2.prio) {
            return o1.prio - o2.prio;
        } else {
            if (o1.enqueueTS == o2.enqueueTS) {
                return o1.wfId.compareTo(o2.wfId);
            }
            else if (o1.enqueueTS > o2.enqueueTS) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }
}
