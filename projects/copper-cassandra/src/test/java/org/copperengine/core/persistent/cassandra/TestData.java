package org.copperengine.core.persistent.cassandra;

import java.io.Serializable;

public class TestData implements Serializable {

    private static final long serialVersionUID = 1L;

    public String id;
    public String someData;

    public TestData() {
    }

    public TestData(String id, String someData) {
        this.id = id;
        this.someData = someData;
    }

}
