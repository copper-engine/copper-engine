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
package de.scoopgmbh.copper.monitoring.core.debug;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StackFrame implements Serializable, DisplayableNode {

    private static final long serialVersionUID = 1L;

    final Method method;
    final Integer line;
    final byte[] sourceCode;
    final List<Member> locals;
    final List<Member> stack;

    public StackFrame(Method method, Integer line, byte[] sourceCode) {
        this.method = method;
        this.sourceCode = sourceCode;
        this.line = line;
        this.locals = new ArrayList<Member>(1);
        this.stack = new ArrayList<Member>(0);
    }

    public List<Member> getLocals() {
        return locals;
    }

    public List<Member> getStack() {
        return stack;
    }

    @Override
    public String getDisplayValue() {
        return method.declaration + " : " + line;
    }

    @Override
    public Collection<? extends DisplayableNode> getChildren() {
        return locals;
    }

    @Override
    public NodeTyp getTyp() {
        return NodeTyp.STACKFRAME;
    }

    public Integer getLine() {
        return line;
    }

    public byte[] getSourceCode() {
        return sourceCode;
    }

}
