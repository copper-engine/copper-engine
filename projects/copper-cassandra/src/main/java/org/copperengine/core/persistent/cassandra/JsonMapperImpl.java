package org.copperengine.core.persistent.cassandra;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapperImpl implements JsonMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String toJSON(Object x) {
        try {
            return mapper.writeValueAsString(x);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T fromJSON(String s, Class<T> c) {
        try {

            return mapper.readValue(s, c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
