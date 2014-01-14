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
package org.copperengine.examples.orchestration.adapter;

import java.io.Serializable;

public class ResetMailboxResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final int returnCode;

    public ResetMailboxResponse(boolean success, int returnCode) {
        this.success = success;
        this.returnCode = returnCode;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "ResetMailboxResponse [success=" + success + ", returnCode="
                + returnCode + "]";
    }

}
