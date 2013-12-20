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
package org.copperengine.spring;

import org.copperengine.core.AbstractDependencyInjector;
import org.copperengine.core.persistent.SavepointAware;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Connects SPRING to COPPER. Enables COPPER to inject dependencies into workflow instances using a spring
 * container/context.
 *
 * @author austermann
 */
public class SpringDependencyInjector extends AbstractDependencyInjector implements ApplicationContextAware {

    private ApplicationContext context;

    public SpringDependencyInjector() {
    }

    public SpringDependencyInjector(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String getType() {
        return "SPRING";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Override
    protected Object getBean(String beanId) {
        Object firsttry = context.getBean(beanId);
        if (firsttry instanceof SavepointAware) {
            Object secoundtry = context.getBean(beanId);
            if (firsttry == secoundtry) {
                throw new IllegalStateException(beanId + " scope is not prototype");
            }
        }
        return firsttry;

    }

}
