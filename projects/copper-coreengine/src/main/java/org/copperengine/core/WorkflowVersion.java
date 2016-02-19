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
package org.copperengine.core;

public class WorkflowVersion {

    public static final class Comparator implements java.util.Comparator<WorkflowVersion> {
        @Override
        public int compare(WorkflowVersion o1, WorkflowVersion o2) {
            if (!o1.isLargerThan(o2))
                return -1;
            if (o1.isLargerThan(o2))
                return 1;
            return 0;
        }
    }

    final long majorVersion;
    final long minorVersion;
    final long patchLevel;
    final String formatted;

    public WorkflowVersion(long majorVersion, long minorVersion, long patchLevel) {
        super();
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchLevel = patchLevel;
        this.formatted = majorVersion + "." + minorVersion + "." + patchLevel;
    }

    public long getMajorVersion() {
        return majorVersion;
    }

    public long getMinorVersion() {
        return minorVersion;
    }

    public long getPatchLevel() {
        return patchLevel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (majorVersion ^ (majorVersion >>> 32));
        result = prime * result + (int) (minorVersion ^ (minorVersion >>> 32));
        result = prime * result + (int) (patchLevel ^ (patchLevel >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WorkflowVersion other = (WorkflowVersion) obj;
        if (majorVersion != other.majorVersion)
            return false;
        if (minorVersion != other.minorVersion)
            return false;
        if (patchLevel != other.patchLevel)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return format();
    }

    public String format() {
        return formatted;
    }

    public boolean isLargerThan(WorkflowVersion other) {
        if (this.majorVersion > other.majorVersion)
            return true;
        if (this.majorVersion == other.majorVersion && this.minorVersion > other.minorVersion)
            return true;
        if (this.majorVersion == other.majorVersion && this.minorVersion == other.minorVersion && this.patchLevel > other.patchLevel)
            return true;
        return false;
    }

}
