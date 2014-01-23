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
package org.copperengine.monitoring.client.form;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.scene.Node;

public abstract class ShowFormsStrategy<E extends Node> {
    public static final int HISTORY_LIMIT = 10;
    protected final E component;

    public ShowFormsStrategy(E component) {
        this.component = component;
    }

    protected abstract void showImpl(Form<?>  form);
    protected abstract void closeImpl(Form<?>  form);

    Form<?> currentForm;
    public void show(Form<?> form){
        if (currentForm!=null && !calledFromBack){
            addPrevious(currentForm);
        }
        currentForm=form;
        showImpl(form);
    }

    public Form<?> getCurrentForm(){
        return currentForm;
    }

    protected CloseListener onCloseListener;

    public void close(Form<?> form){
        addPrevious(form);
        closeImpl(form);

        if (onCloseListener!=null){
            onCloseListener.closed(form);
        }
    }

    protected void addPrevious(Form<?> form) {
        if (previous.size()>=HISTORY_LIMIT){
            previous.removeFirst();
        }
        previous.offerLast(form);
    }

    /**
     * called if form is closed
     *
     * @param closeListener
     */
    public void setOnCloseListener(CloseListener closeListener) {
        onCloseListener = closeListener;
    }

    public static interface CloseListener {
        public void closed(Form<?> form);
    }

    boolean calledFromBack=false;
    final Deque<Form<?>> previous = new ArrayDeque<Form<?>>(HISTORY_LIMIT);
    public void back(){
        Form<?> last = previous.pollLast();
        if (last!=null){
            if (currentForm!=null){
                if (next.size()>=HISTORY_LIMIT){
                    next.removeFirst();
                }
                next.offerLast(currentForm);
            }
            calledFromBack=true;
            show(last);
            calledFromBack=false;
        }
    }

    final Deque<Form<?>> next = new ArrayDeque<Form<?>>(HISTORY_LIMIT);
    public void forward(){
        Form<?> forward = next.pollLast();
        if (forward!=null){
            show(forward);
        }
    }


}