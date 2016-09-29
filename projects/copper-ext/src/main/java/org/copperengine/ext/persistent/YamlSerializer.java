package org.copperengine.ext.persistent;

import java.io.IOException;

import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.persistent.SerializedWorkflow;
import org.copperengine.core.persistent.Serializer;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.yaml.snakeyaml.Yaml;

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
        return new Yaml();
    }

    @Override
    protected String serializeData(Workflow<?> o) throws IOException {
        return yaml.get().dump(o.getData());
    }

    @Override
    protected Object deserializeData(SerializedWorkflow sw) throws Exception {
        return yaml.get().load(sw.getData());
    }

}
