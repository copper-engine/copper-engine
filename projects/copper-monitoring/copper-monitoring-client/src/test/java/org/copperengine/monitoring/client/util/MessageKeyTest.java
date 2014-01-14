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
package org.copperengine.monitoring.client.util;

import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Test;

public class MessageKeyTest {

    @Test
    public void testKeyInProperty() {
        MessageProvider messageProvider = new MessageProvider(ResourceBundle.getBundle("org.copperengine.gui.message"));
        for (MessageKey key : MessageKey.values()) {
            Assert.assertNotNull(messageProvider.getText(key));
        }
    }

}
