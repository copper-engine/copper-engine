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
package org.copperengine.monitoring.client.form.filter;

import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.copperengine.monitoring.client.form.Form;
import org.copperengine.monitoring.client.form.issuereporting.IssueReporter;

public class BackgroundRepeatFilterService<F, R> extends Service<Void> {
    private long refreshRate = 1000;
    long lasttime = System.currentTimeMillis();
    private final FilterResultController<F, R> filterResultController;
    private final Form<FilterController<F>> filterForm;
    private final IssueReporter exceptionHandler;

    public BackgroundRepeatFilterService(FilterResultController<F, R> filterResultController, Form<FilterController<F>> filterForm, IssueReporter exceptionHandler) {
        super();
        this.filterResultController = filterResultController;
        this.filterForm = filterForm;
        this.exceptionHandler = exceptionHandler;
    }

    public void setRefreshIntervall(long refreshRate) {
        this.refreshRate = refreshRate;
    }

    @Override
    public void start() {
        lasttime = System.currentTimeMillis();
        super.start();
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!isCancelled()) {
                    if (lasttime + refreshRate < System.currentTimeMillis()) {
                        updateProgress(-1, 1);
                        final List<R> result;
                        try {
                            result = filterResultController.applyFilterInBackgroundThread(filterForm.getController().getFilter());
                        } catch (Exception e1) {
                            exceptionHandler.reportError(e1);
                            cancel();
                            throw new RuntimeException(e1);
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    filterResultController.showFilteredResult(result, filterForm.getController().getFilter());
                                } catch (Exception e) {
                                    exceptionHandler.reportError(e);
                                    cancel();
                                }
                            }
                        });
                        lasttime = System.currentTimeMillis();
                    }
                    Thread.sleep(Math.min(50, refreshRate / 10));
                    long progress = System.currentTimeMillis() - lasttime;
                    progress = progress <= refreshRate ? progress : refreshRate;
                    if (refreshRate <= 500) {
                        progress = -1;
                    }
                    updateProgress(progress, refreshRate);
                }
                return null;
            }
        };
    }
}