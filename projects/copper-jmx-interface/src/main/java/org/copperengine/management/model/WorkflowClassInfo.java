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
package org.copperengine.management.model;

import java.io.Serializable;

public class WorkflowClassInfo implements Serializable {

    private static final long serialVersionUID = -2322677004796387402L;

    private String classname;
    private String alias;
    private Long majorVersion;
    private Long minorVersion;
    private Long patchLevel;
    private String sourceCode;

    public WorkflowClassInfo() {
    }

    public WorkflowClassInfo(String classname, String alias, Long majorVersion, Long minorVersion, Long patchLevel, String sourceCode) {
        this.classname = classname;
        this.alias = alias;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchLevel = patchLevel;
        this.sourceCode = sourceCode;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Long getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(Long majorVersion) {
        this.majorVersion = majorVersion;
    }

    public Long getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(Long minorVersion) {
        this.minorVersion = minorVersion;
    }

    public Long getPatchLevel() {
        return patchLevel;
    }

    public void setPatchLevel(Long patchLevel) {
        this.patchLevel = patchLevel;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "WorkflowClassInfo [classname=" + classname + ", alias=" + alias
                + ", majorVersion=" + majorVersion + ", minorVersion="
                + minorVersion + ", patchLevel=" + patchLevel + "]";
    }

}
