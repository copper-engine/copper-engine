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
package org.copperengine.monitoring.client.context;

import org.copperengine.monitoring.client.form.FxmlForm;
import org.copperengine.monitoring.client.form.ShowFormStrategy;
import org.copperengine.monitoring.client.form.filter.FilterAbleForm;
import org.copperengine.monitoring.client.form.filter.FilterController;
import org.copperengine.monitoring.client.form.filter.FilterResultController;
import org.copperengine.monitoring.client.form.filter.enginefilter.BaseEngineFilterController;
import org.copperengine.monitoring.client.form.filter.enginefilter.EngineFilterAbleForm;
import org.copperengine.monitoring.client.form.filter.enginefilter.EnginePoolFilterModel;
import org.copperengine.monitoring.client.form.issuereporting.IssueReporter;
import org.copperengine.monitoring.client.util.MessageProvider;

/**
 * a form builder.
 * 
 * @param <FM>
 *            filter model
 * @param <RM>
 *            result model
 * @param <R>
 *            result controller
 * @param <F>
 *            filter controller
 */
public class FormBuilder<FM, RM, F extends FilterController<FM>, R extends FilterResultController<FM, RM>> {
    protected final F filterController;
    protected final R resultController;
    protected final MessageProvider messageProvider;
    protected final ShowFormStrategy<?> showFormStrategy;
    protected final IssueReporter exceptionHandler;

    public FormBuilder(F filterController, R resultController, MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategy, IssueReporter exceptionHandler) {
        this.filterController = filterController;
        this.resultController = resultController;
        this.messageProvider = messageProvider;
        this.showFormStrategy = showFormStrategy;
        this.exceptionHandler = exceptionHandler;
    }

    public FormBuilder(F filterController, R resultController, FormContext formContext) {
        this(filterController, resultController, formContext.messageProvider, formContext.getDefaultShowFormStrategy(), formContext.issueReporter);
    }

    public FilterAbleForm<FM, RM> build() {
        FxmlForm<FilterController<FM>> filterForm = new FxmlForm<FilterController<FM>>(filterController, messageProvider);
        FxmlForm<FilterResultController<FM, RM>> resultForm = new FxmlForm<FilterResultController<FM, RM>>(resultController, messageProvider);
        return new FilterAbleForm<FM, RM>(messageProvider, showFormStrategy, filterForm, resultForm, exceptionHandler);
    }

    public static class EngineFormBuilder<FM extends EnginePoolFilterModel, RM, F extends BaseEngineFilterController<FM>, R extends FilterResultController<FM, RM>>
            extends FormBuilder<FM, RM, F, R> {

        public EngineFormBuilder(F filterController, R resultController, FormContext formContext) {
            super(filterController, resultController, formContext);
        }

        public EngineFormBuilder(F filterController, R resultController, MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategy, String title, IssueReporter exceptionHandler) {
            super(filterController, resultController, messageProvider, showFormStrategy, exceptionHandler);
        }

        @Override
        public EngineFilterAbleForm<FM, RM> build() {
            FxmlForm<FilterController<FM>> filterForm = new FxmlForm<FilterController<FM>>(filterController, messageProvider);
            FxmlForm<FilterResultController<FM, RM>> resultForm = new FxmlForm<FilterResultController<FM, RM>>(resultController, messageProvider);
            return new EngineFilterAbleForm<FM, RM>(messageProvider, showFormStrategy, filterForm, resultForm, exceptionHandler);
        }
    }
}