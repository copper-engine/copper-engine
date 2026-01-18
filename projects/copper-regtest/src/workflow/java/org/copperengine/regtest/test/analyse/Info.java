/*
 * Copyright 2002-2017 SCOOP Software GmbH
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
package org.copperengine.regtest.test.analyse;

import java.util.Stack;

import org.copperengine.core.StackEntry;

final class Info {

    static void appendInfo(
            final String message,
            final Stack<StackEntry> stack,
            final int stackPosition,
            final StringBuffer analyseString
    ) {
        analyseString.append(message).append("__stackPosition=").append(stackPosition).append("\n\t__stack=");
        appendStack(stack, analyseString);
        analyseString.append("\n");
    }

    static void appendStack(final Stack<StackEntry> stack, final StringBuffer analyseString) {
        analyseString.append("[");
        for (int i = 0; i < stack.size(); i++) {
            analyseString.append("[");
            StackEntry entry = stack.get(i);
            analyseString.append("\n\tjumpNo=").append(entry.jumpNo).append("\n\tlocals=[");
            for (int j = 0; j < entry.locals.length; j++) {
                if (j > 0) {
                    analyseString.append(",");
                }
                analyseString.append(entry.locals[j]);
            }
            analyseString.append("]\n\tstack=[");
            for (int j = 0; j < entry.stack.length; j++) {
                if (j > 0) {
                    analyseString.append(",");
                }
                analyseString.append(entry.stack[j]);
            }
            analyseString.append("]");
            analyseString.append("]");
        }
        analyseString.append("]");
    }

    private Info() {
    }
}