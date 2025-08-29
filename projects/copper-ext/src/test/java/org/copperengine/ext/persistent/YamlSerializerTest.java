/*
 * Copyright 2002-2024 SCOOP Software GmbH
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
package org.copperengine.ext.persistent;

import org.junit.Test;

import java.io.Serializable;
import java.util.Objects;

import static junit.framework.TestCase.assertEquals;

public class YamlSerializerTest {

    private final YamlSerializer serializer = new YamlSerializer();

    public static class User implements Serializable {
        @SuppressWarnings("unused")
        User() {}

        User(String name) {
            this.name = name;
        }
        public String name;

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof User)) return false;
            final User user = (User) o;
            return Objects.equals(name, user.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }
    }

    @Test
    public void string() throws Exception {
        final String workflowData = "Test";


        final String serialized = serializer.serialize(workflowData);
        final Object deserialized = serializer.deserialize(serialized);


        assertEquals(workflowData, deserialized);
    }

    @Test
    public void userClass() throws Exception {
        final User workflowData = new User( "Wolf");


        final String serialized = serializer.serialize(workflowData);
        final Object deserialized = serializer.deserialize(serialized);


        assertEquals(workflowData, deserialized);
    }

    @Test
    public void compatibilityUserClass() throws Exception {
        final User workflowData = new User( "Wolf");


        // taken from snakeyaml 1.33
        final String serialized = "!!org.copperengine.ext.persistent.YamlSerializerTest$User {name: Wolf}";
        final Object deserialized = serializer.deserialize(serialized);


        assertEquals(workflowData, deserialized);
    }
}