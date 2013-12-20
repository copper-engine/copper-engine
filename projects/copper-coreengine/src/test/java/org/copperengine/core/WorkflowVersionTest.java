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
package org.copperengine.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WorkflowVersionTest {

    @Test
    public void testIsLargerThan() {
        final WorkflowVersion a = new WorkflowVersion(1, 2, 3);
        assertTrue(a.equals(a));
        assertFalse(a.isLargerThan(a));

        final WorkflowVersion b = new WorkflowVersion(1, 3, 0);
        assertFalse(a.equals(b));
        assertTrue(b.isLargerThan(a));
    }

}
