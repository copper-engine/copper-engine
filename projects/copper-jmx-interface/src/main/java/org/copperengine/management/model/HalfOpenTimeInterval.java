package org.copperengine.management.model;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Date;

public class HalfOpenTimeInterval implements Serializable {
    
    private static final long serialVersionUID = -3362183236879486427L;
    
    private Date from;
    private Date to;

    public HalfOpenTimeInterval() {
    }

    @ConstructorProperties({"from", "to"})
    public HalfOpenTimeInterval(Date from, Date to) {
        this.from = from;
        this.to = to;
    }

    public Date getFrom() {
        return from;
    }
    public void setFrom(Date from) {
        this.from = from;
    }
    public Date getTo() {
        return to;
    }
    public void setTo(Date to) {
        this.to = to;
    }
    @Override
    public String toString() {
        return "HalfOpenTimeInterval [from=" + from + ", to=" + to + "]";
    }
    
    
}
