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
package org.copperengine.core.test.versioning.compatibility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

import org.apache.commons.codec.binary.Base64;
import org.copperengine.core.Response;
import org.copperengine.core.Workflow;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.persistent.Compressor;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.persistent.SerializedWorkflow;
import org.copperengine.core.persistent.StandardJavaSerializer;

public class TestJavaSerializer extends StandardJavaSerializer {

    private boolean compress = true;
    private int compressThresholdSize = 250;
    private int compressorMaxSize = 128 * 1024;
    private String classNameReplacement;

    public void setClassNameReplacement(String classNameReplacement) {
        this.classNameReplacement = classNameReplacement;
    }

    private ThreadLocal<Compressor> compressorTL = new ThreadLocal<Compressor>() {
        @Override
        protected Compressor initialValue() {
            return new Compressor(Deflater.BEST_COMPRESSION, compressorMaxSize);
        }
    };

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public void setCompressorMaxSize(int compressorMaxSize) {
        this.compressorMaxSize = compressorMaxSize;
        compressorTL = new ThreadLocal<Compressor>() {
            @Override
            protected Compressor initialValue() {
                return new Compressor(Deflater.BEST_COMPRESSION, TestJavaSerializer.this.compressorMaxSize);
            }
        };
    }

    public void setCompressThresholdSize(int compressThresholdSize) {
        this.compressThresholdSize = compressThresholdSize;
    }

    private String serialize(final Object o) throws IOException {
        if (o == null)
            return null;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(o);
        oos.close();
        baos.close();
        byte[] data = baos.toByteArray();
        final ByteArrayOutputStream baos2 = new ByteArrayOutputStream(1024);
        baos2.write(data, 0, 8);
        baos2.write(classNameReplacement.getBytes("UTF-8"));
        int offset = classNameReplacement.length() + 8;
        baos2.write(data, offset, data.length - offset);
        baos2.flush();
        baos2.close();
        data = baos2.toByteArray();
        boolean isCompressed = false;
        if (compress && compressThresholdSize <= data.length && data.length <= compressorMaxSize) {
            data = compressorTL.get().compress(data);
            isCompressed = true;
        }
        final String encoded = new Base64().encodeToString(data);
        final StringBuilder sb = new StringBuilder(encoded.length() + 4);
        sb.append(isCompressed ? 'C' : 'U').append(encoded);
        return sb.toString();
    }

    private Serializable deserialize(String _data, final WorkflowRepository wfRepo) throws IOException, ClassNotFoundException, DataFormatException {
        if (_data == null)
            return null;
        boolean isCompressed = _data.charAt(0) == 'C';
        byte[] data = Base64.decodeBase64(_data.substring(1));
        if (isCompressed) {
            data = compressorTL.get().uncompress(data);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = wfRepo != null ? new ObjectInputStream(bais) {
            @Override
            protected java.lang.Class<?> resolveClass(java.io.ObjectStreamClass desc) throws java.io.IOException, ClassNotFoundException {
                return wfRepo.resolveClass(desc.getName());
            };
        } : new ObjectInputStream(bais);
        Serializable o = (Serializable) ois.readObject();
        ois.close();
        return o;
    }

    @Override
    public SerializedWorkflow serializeWorkflow(Workflow<?> o) throws Exception {
        SerializedWorkflow sw = new SerializedWorkflow();
        sw.setData(serializeData(o));
        sw.setObjectState(serialize(o));
        return sw;
    }

    protected String serializeData(Workflow<?> o) throws IOException {
        return serialize(o.getData());
    }

    @Override
    public Workflow<?> deserializeWorkflow(SerializedWorkflow sw, WorkflowRepository wfRepo) throws Exception {
        PersistentWorkflow<?> wf = (PersistentWorkflow<?>) deserialize(sw.getObjectState(), wfRepo);
        wf.setDataAsObject(deserializeData(sw));
        return wf;
    }

    protected Object deserializeData(SerializedWorkflow sw) throws Exception {
        return deserializeObject(sw.getData());
    }

    @Override
    public String serializeResponse(Response<?> r) throws Exception {
        return serialize(r);
    }

    @Override
    public Response<?> deserializeResponse(String _data) throws Exception {
        return (Response<?>) deserialize(_data, null);
    }

    public String serializeObject(Object o) throws Exception {
        return serialize(o);
    }

    public Serializable deserializeObject(String _data) throws Exception {
        return deserialize(_data, null);
    }

}
