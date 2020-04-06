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
package org.copperengine.regtest.test;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Workflow;

public class PerformanceTestWF extends Workflow<String> {

    private static final long serialVersionUID = 1L;

    private int idx;
    private MockAdapter mockAdapter;
    private long createTS = System.currentTimeMillis();

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @Override
    public void main() throws Interrupt {
        for (idx = 0; idx < 10; idx++) {
            waitForAll(mockAdapter.foo("foo"));
            // resubmit();
        }
        TransientPerformanceTestInputChannel.addMP(System.currentTimeMillis() - createTS);
        TransientPerformanceTestInputChannel.increment();
    }

}
