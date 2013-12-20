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
package org.copperengine.monitoring.core.debug;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

public class Member implements Serializable, DisplayableNode {
    private static final long serialVersionUID = 1L;

    final String name;
    final String declaredType;
    final Data value;

    public Member(String name, String declaredType, Data value) {
        this.name = name.intern();
        this.value = value;
        this.declaredType = declaredType.intern();
    }

    public String getName() {
        return name;
    }

    public Data getValue() {
        return value;
    }

    @Override
    public Collection<DisplayableNode> getChildren() {
        return Arrays.<DisplayableNode> asList(value);
    }

    @Override
    public String getDisplayValue() {
        return name + " : " + declaredType;
    }

    @Override
    public NodeTyp getTyp() {
        return null;
    }

}
