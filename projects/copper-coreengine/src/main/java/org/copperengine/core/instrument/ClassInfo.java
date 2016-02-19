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
package org.copperengine.core.instrument;

import java.io.Serializable;
import java.util.List;

public class ClassInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    final List<MethodInfo> methodInfos;
    ClassInfo superClassInfo;
    byte[] sourceCode;

    public ClassInfo(List<MethodInfo> methodInfos) {
        this.methodInfos = methodInfos;
    }

    public void setSuperClassInfo(ClassInfo superClassInfo) {
        this.superClassInfo = superClassInfo;
    }

    public void setSourceCode(byte[] sourceCode) {
        this.sourceCode = sourceCode;
    }

    public List<MethodInfo> getMethodInfos() {
        return methodInfos;
    }

    public ClassInfo getSuperClassInfo() {
        return superClassInfo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(2000);
        sb.append("methods: \n");
        for (MethodInfo info : methodInfos) {
            sb.append("\t").append(info).append('\n');
        }
        return sb.toString();
    }

    public byte[] getSourceCode() {
        return sourceCode;
    }

}