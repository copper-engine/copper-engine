package org.copperengine.management.model;

import java.io.Serializable;
import java.util.Date;

public class ErrorData implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Date errorTS;
    private String exceptionStackTrace;
    
    public ErrorData() {
    }

    public ErrorData(Date errorTS, String exceptionStackTrace) {
        this.errorTS = errorTS;
        this.exceptionStackTrace = exceptionStackTrace;
    }

    public Date getErrorTS() {
        return errorTS;
    }

    public void setErrorTS(Date errorTS) {
        this.errorTS = errorTS;
    }

    public String getExceptionStackTrace() {
        return exceptionStackTrace;
    }

    public void setExceptionStackTrace(String exceptionStackTrace) {
        this.exceptionStackTrace = exceptionStackTrace;
    }

    @Override
    public String toString() {
        return "ErrorData [errorTS=" + errorTS + ", exceptionStackTrace=" + exceptionStackTrace + "]";
    }
    
}
