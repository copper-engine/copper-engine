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
package org.copperengine.core.test.tranzient.simple;

import org.copperengine.core.Interrupt;

public class IssueClassCastExceptionWorkflow extends AbstractIssueClassCastExceptionWorkflow {

    private static final long serialVersionUID = 1L;

    @Override
    protected void callAbstractExceptionSimulation0(String partnerLink) {
        throw new RuntimeException("Simulate exception.");
    }

    @Override
    protected void callAbstractExceptionSimulation1() throws Interrupt {
        throw new RuntimeException("Simulate exception.");
    }

    @Override
    protected void callAbstractExceptionSimulation2(String partnerLink) {
        throw new RuntimeException("Simulate exception.");
    }

    @Override
    public void main() throws Interrupt {
        this.callPartner(100);
        getData().error = false;
        getData().done = true;
    }
}
