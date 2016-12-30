/**
 * Copyright 2002-2017 SCOOP Software GmbH
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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortedResponseList extends AbstractList<Response<?>> {

    private boolean sorted = false;
    private List<Response<?>> data = new ArrayList<>();

    private void makeSureListIsSorted() {
        if (sorted)
            return;
        Collections.sort(data, ResponseComparator.INSTANCE);
        sorted = true;
    }

    @Override
    public boolean add(Response<?> e) {
        return data.add(e);
    }

    @Override
    public Response<?> get(int index) {
        makeSureListIsSorted();
        return data.get(index);
    }

    @Override
    public Response<?> remove(int index) {
        makeSureListIsSorted();
        return data.remove(index);
    }

    @Override
    public int size() {
        return data.size();
    }

}
