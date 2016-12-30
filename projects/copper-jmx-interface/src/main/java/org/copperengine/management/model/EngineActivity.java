/*
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

package org.copperengine.management.model;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Date;

public class EngineActivity implements Serializable {

    private static final long serialVersionUID = 8133235408154659096L;

    private Date lastActivityTS;
    private Date startupTS;
    private long countWfiLastNMinutes;
    
    public EngineActivity() {
    }
    
    @ConstructorProperties({"lastActivityTS", "startupTS", "countWfiLastNMinutes"})
    public EngineActivity(Date lastActivityTS, Date startupTS, long countWfiLastNMinutes) {
        this.lastActivityTS = lastActivityTS;
        this.startupTS = startupTS;
        this.countWfiLastNMinutes = countWfiLastNMinutes;
    }

    /**
     * Timestamp of the last activity in this engine, e.g. starting a new workflow instance, processing or finishing an existing one.
     */
    public Date getLastActivityTS() {
        return lastActivityTS;
    }
    
    public void setLastActivityTS(Date lastActivityTS) {
        this.lastActivityTS = lastActivityTS;
    }

    /**
     * StartUp TS of this engine or null if the engine is not yet started or down.
     * @return
     */
    public Date getStartupTS() {
        return startupTS;
    }
    
    public void setStartupTS(Date startupTS) {
        this.startupTS = startupTS;
    }
    
    /**
     * Number of finished Workflow instances within the last N minutes.
     */
    public long getCountWfiLastNMinutes() {
        return countWfiLastNMinutes;
    }
    
    public void setCountWfiLastNMinutes(long countWfiLastNMinutes) {
        this.countWfiLastNMinutes = countWfiLastNMinutes;
    }

    @Override
    public String toString() {
        return "EngineActivity [lastActivityTS=" + lastActivityTS + ", startupTS=" + startupTS + ", countWfiLastNMinutes=" + countWfiLastNMinutes + "]";
    }
    
    
}
