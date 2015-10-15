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
package org.copperengine.monitoring.client.form.filter;

import java.util.List;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import org.copperengine.monitoring.client.form.Form;
import org.copperengine.monitoring.client.form.issuereporting.IssueReporter;

public class BackgroundFilterService<F, R> extends Service<FilterAbleForm.ResultFilterPair<F, R>> {
    private final FilterResultController<F, R> filterResultController;
    private final Form<FilterController<F>> filterForm;
    private final IssueReporter exceptionHandler;

    public BackgroundFilterService(FilterResultController<F, R> filterResultControllerParam, Form<FilterController<F>> filterForm, IssueReporter exceptionHandlerParam) {
        super();
        this.filterResultController = filterResultControllerParam;
        this.filterForm = filterForm;
        this.exceptionHandler = exceptionHandlerParam;

        setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    @SuppressWarnings("unchecked")
                    FilterAbleForm.ResultFilterPair<F, R> result = (FilterAbleForm.ResultFilterPair<F, R>) event.getSource().getValue();
                    filterResultController.showFilteredResult(result.result, result.usedFilter);
                } catch (Exception e) {
                    exceptionHandler.reportError(e);
                }
            }
        });
    }

    @Override
    protected Task<FilterAbleForm.ResultFilterPair<F, R>> createTask() {
        return new Task<FilterAbleForm.ResultFilterPair<F, R>>() {
            @Override
            protected FilterAbleForm.ResultFilterPair<F, R> call() throws Exception {
                try {
                    final List<R> result = filterResultController.applyFilterInBackgroundThread(filterForm.getController().getFilter());
                    return new FilterAbleForm.ResultFilterPair<F, R>(result, filterForm.getController().getFilter());
                } catch (Exception e) {
                    exceptionHandler.reportError(e);
                    throw new RuntimeException(e);
                }
            }
        };
    }

}