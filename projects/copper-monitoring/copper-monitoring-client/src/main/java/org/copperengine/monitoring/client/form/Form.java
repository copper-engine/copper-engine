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
package org.copperengine.monitoring.client.form;

import javafx.beans.property.SimpleStringProperty;

/**
 * @param <C>
 *            target component to display the form
 */
public abstract class Form<C> implements Widget {

    private final SimpleStringProperty displayTitle;
    protected SimpleStringProperty staticTitle;
    private final ShowFormStrategy<?> showFormStrategy;
    protected final C controller;

    public Form(String staticTitle, ShowFormStrategy<?> showFormStrategy, C controller) {
        super();
        this.staticTitle = new SimpleStringProperty(staticTitle);
        this.displayTitle = new SimpleStringProperty(staticTitle);
        this.showFormStrategy = showFormStrategy;
        this.controller = controller;
    }

    public SimpleStringProperty displayedTitleProperty() {
        return displayTitle;
    }

    public SimpleStringProperty staticTitleProperty() {
        return staticTitle;
    }

    public void show() {
        showFormStrategy.show(this);
    }

    public C getController() {
        return controller;
    }

    public void setStaticTitle(String staticTitle) {
        this.staticTitle.set(staticTitle);
    }

}
