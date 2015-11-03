package org.copperengine.core.persistent.cassandra;

public interface JsonMapper {

    String toJSON(Object x);

    <T> T fromJSON(String s, Class<T> c);

}
