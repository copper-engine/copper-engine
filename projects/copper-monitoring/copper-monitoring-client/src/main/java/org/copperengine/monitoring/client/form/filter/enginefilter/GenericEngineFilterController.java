/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.form.filter.enginefilter;

import java.net.URL;
import java.util.List;

import javafx.scene.Node;

import org.copperengine.monitoring.client.form.filter.FilterController;
import org.copperengine.monitoring.client.form.filter.GenericFilterController;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;

public class GenericEngineFilterController<T extends EnginePoolFilterModel> extends BaseEngineFilterController<T> {

    private final T filter;
    private long refereshIntervall;

    public GenericEngineFilterController(T filter, long refereshIntervall, List<ProcessingEngineInfo> availableEngines) {
        super(availableEngines, filter);
        this.filter = filter;
        this.refereshIntervall = refereshIntervall;
    }

    public GenericEngineFilterController(T filter, List<ProcessingEngineInfo> availableEngines) {
        this(filter, FilterController.DEFAULT_REFRESH_INTERVALL, availableEngines);
    }

    public GenericEngineFilterController(long refereshIntervall, List<ProcessingEngineInfo> availableEngines) {
        this(null, refereshIntervall, availableEngines);
    }

    @Override
    public URL getFxmlResource() {
        return GenericFilterController.EMPTY_DUMMY_URL;
    }

    @Override
    public boolean supportsFiltering() {
        return filter != null;
    }

    @Override
    public long getDefaultRefreshInterval() {
        return refereshIntervall;
    }

    @Override
    public Node createAdditionalFilter() {
        return null;
    }

}
