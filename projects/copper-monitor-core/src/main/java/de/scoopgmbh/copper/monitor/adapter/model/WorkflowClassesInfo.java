/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;


public class WorkflowClassesInfo implements Serializable{
	private static final long serialVersionUID = -1189606285407748364L;
	
	private String classname;
	private long majorVersion;
	private long minorVersion;
	
	private long patchLevel;
	public WorkflowClassesInfo(String classname, long majorVersion, long minorVersion, long patchLevel) {
		super();
		this.classname = classname;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.patchLevel = patchLevel;
	}
	
	
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
	public long getMajorVersion() {
		return majorVersion;
	}
	public void setMajorVersion(long majorVersion) {
		this.majorVersion = majorVersion;
	}
	public long getMinorVersion() {
		return minorVersion;
	}
	public void setMinorVersion(long minorVersion) {
		this.minorVersion = minorVersion;
	}
	public long getPatchLevel() {
		return patchLevel;
	}
	public void setPatchLevel(long patchLevel) {
		this.patchLevel = patchLevel;
	}
	
}
