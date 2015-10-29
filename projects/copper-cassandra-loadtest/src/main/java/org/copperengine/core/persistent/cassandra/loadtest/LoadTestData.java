package org.copperengine.core.persistent.cassandra.loadtest;

import java.io.Serializable;

public class LoadTestData implements Serializable {

    private static final long serialVersionUID = 1L;

    public String id;
    public String someData;

    public LoadTestData() {
    }

    public LoadTestData(String id, String someData) {
        this.id = id;
        this.someData = someData;
    }

}
