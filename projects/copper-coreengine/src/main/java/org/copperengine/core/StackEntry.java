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
package org.copperengine.core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * For internal use only.
 *
 * @author austermann
 */
public class StackEntry implements Externalizable {

    private static final long serialVersionUID = 1L;

    public int jumpNo;
    public Object[] locals;
    public Object[] stack;

    public StackEntry(Object[] stack, int jumpNo, Object[] locals) {
        this.jumpNo = jumpNo;
        this.locals = locals;
        this.stack = stack;
    }

    public StackEntry(int jumpNo) {
        this.jumpNo = jumpNo;
    }

    // Externalizable interface requires public default constructor to be given.
    public StackEntry() {
        this(0);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(jumpNo);
        out.writeInt(locals == null ? 0 : locals.length);
        out.writeInt(stack == null ? 0 : stack.length);
        if (locals != null) {
            for (int i = 0; i < locals.length; ++i)
                out.writeObject(locals[i]);
        }
        if (stack != null) {
            for (int i = 0; i < stack.length; ++i)
                out.writeObject(stack[i]);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        jumpNo = in.readInt();
        int numLocals = in.readInt();
        int numStack = in.readInt();
        if (numLocals > 0)
            locals = new Object[numLocals];
        if (numStack > 0)
            stack = new Object[numStack];
        for (int i = 0; i < numLocals; ++i)
            locals[i] = in.readObject();
        for (int i = 0; i < numStack; ++i)
            stack[i] = in.readObject();
    }

    @Override
    public String toString() {
        // return "StackEntry [jumpNo=" + jumpNo + ", locals="
        // + Arrays.toString(locals) + ", stack=" + Arrays.toString(stack)
        // + "]";
        return "StackEntry@" + hashCode() + " [jumpNo=" + jumpNo + "]";
    }

}
