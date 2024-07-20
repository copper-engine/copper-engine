/**
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
package org.copperengine.ext.persistent;

import java.io.IOException;

import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.persistent.SerializedWorkflow;
import org.copperengine.core.persistent.Serializer;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 * COPPER {@link Serializer} using YAML for data serialization. For some applications using YAML instead of Java
 * serialization might have the advantage that the data is more or less human readable in the underlying database.
 * <p>
 * This class extends {@link StandardJavaSerializer} because the workflows {@link SerializedWorkflow#getObjectState
 * object state} and {@link Response responses} are still serialized using the standard Java serialization mechanism.
 * <p>
 * 
 * 
 * @author Michael Austermann
 *
 */
public class YamlSerializer extends StandardJavaSerializer implements Serializer {

    private final ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>() {
        @Override
        protected Yaml initialValue() {
            return initialYaml();
        }
    };

    protected Yaml initialYaml() {
        DumperOptions dO = new DumperOptions();
        dO.setAllowReadOnlyProperties(true);
        Yaml yaml = new Yaml(dO);
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml;
    }

    @Override
    protected String serializeData(Workflow<?> o) throws IOException {
        return serialize(o.getData());
    }

    @Override
    protected Object deserializeData(SerializedWorkflow sw) throws Exception {
        return deserialize(sw.getData());
    }


    String serialize(final Object workflowData) {
        return yaml.get().dump(workflowData);
    }

    Object deserialize(String workflowData) {
        return yaml.get().load(workflowData);
    }

}
