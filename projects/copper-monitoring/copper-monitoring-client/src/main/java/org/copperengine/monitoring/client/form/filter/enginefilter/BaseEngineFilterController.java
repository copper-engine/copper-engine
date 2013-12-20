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
package org.copperengine.monitoring.client.form.filter.enginefilter;

import java.util.List;

import javafx.scene.Node;

import org.copperengine.monitoring.client.form.filter.BaseFilterController;
import org.copperengine.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;

public abstract class BaseEngineFilterController<T extends EnginePoolFilterModel> extends BaseFilterController<T> {

    protected final T model;
    protected final List<ProcessingEngineInfo> availableEngines;

    public BaseEngineFilterController(List<ProcessingEngineInfo> availableEngines, T model) {
        super();
        this.model = model;
        this.availableEngines = availableEngines;

        getFilter().selectedEngine.set(availableEngines.get(0));
        getFilter().selectedPool.set(availableEngines.get(0).getPools().get(0));
    }

    @Override
    public Node createDefaultFilter() {
        DefaultFilterFactory defaultFilterFactory = new DefaultFilterFactory();
        final Node createAdditionalFilter = createAdditionalFilter();
        final Node engineFilterUI = defaultFilterFactory.createEngineFilterUI(getFilter(), availableEngines);

        if (createAdditionalFilter != null) {
            return defaultFilterFactory.createVerticalMultiFilter(createAdditionalFilter, engineFilterUI);
        } else {
            return defaultFilterFactory.createVerticalMultiFilter(engineFilterUI);
        }
    }

    public abstract Node createAdditionalFilter();

    @Override
    public T getFilter() {
        return model;
    }

}
