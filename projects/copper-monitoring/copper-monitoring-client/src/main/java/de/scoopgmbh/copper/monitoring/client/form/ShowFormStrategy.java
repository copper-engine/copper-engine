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
package de.scoopgmbh.copper.monitoring.client.form;

import javafx.scene.Node;

public abstract class ShowFormStrategy<E extends Node> {
    protected E component;

    public ShowFormStrategy(E component) {
        this.component = component;
    }

    public abstract void show(Form<?> form);

    protected CloseListener onCloseListener;

    /**
     * called if form is closed
     * 
     * @param closeListner
     */
    public void setOnCloseListener(CloseListener closeListner) {
        onCloseListener = closeListner;
    }

    public static interface CloseListener {
        public void closed(Form<?> form);
    }
}