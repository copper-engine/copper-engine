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
package org.copperengine.monitoring.client.form;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;

/**
 * @param <C> Controller for the Form
 */
public abstract class Form<C> implements Widget {

    private final SimpleStringProperty displayTitle;
    protected String initialTitle;
    protected ShowFormsStrategy<?> showFormStrategy;
    protected final C controller;

    public Form(String title, ShowFormsStrategy<?> showFormStrategy, C controller) {
        super();
        this.initialTitle = title;
        this.displayTitle = new SimpleStringProperty(title);
        this.showFormStrategy = showFormStrategy;
        this.controller = controller;
    }

    /**
     * normally showFormStrategy should be passed with constructor but sometimes the target Node is not available during creation
     * than you can use {@link EmptyShowFormStrategy} in constructor and set later
     * @param showFormStrategy
     */
    protected void setShowFormStrategy(ShowFormsStrategy<?> showFormStrategy){
        this.showFormStrategy = showFormStrategy;
    }

    public SimpleStringProperty displayedTitleProperty() {
        return displayTitle;
    }

    public void show() {
        showFormStrategy.show(this);
    }

    public C getController() {
        return controller;
    }

    /**
     * the initial titel could be used to append dynamic Title parts
     */
    public String getInitialTitle() {
        return this.initialTitle;
    }

    /**
     * the titel
     */
    public void setAllTitle(String title) {
        this.initialTitle = title;
        this.displayTitle.set(title);
    }

    public void setTitle(String title) {
        this.displayTitle.set(title);
    }

    Node cachedContent;

    Node getCachedContent(){
       if (cachedContent==null){
           cachedContent=createContent();
       }
       return cachedContent;
    }

    public void close() {
        showFormStrategy.close(this);
    }

    DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    /**
     *
     * @param <C> Controller
     */
    public static class NonDisplayableForm<C>{
        private final Form<C> form;

        protected NonDisplayableForm(Form<C> form) {
            this.form = form;
        }

        public Form<C> convertToDisplayAble(ShowFormsStrategy <?> showFormsStrategy){
            form.setShowFormStrategy(showFormsStrategy);
            return form;
        }

    }

}
