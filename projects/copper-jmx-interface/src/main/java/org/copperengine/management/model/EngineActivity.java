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
