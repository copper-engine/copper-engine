package org.copperengine.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StateMapper {

    private static final Logger logger = LoggerFactory.getLogger(StateMapper.class);

    public StateMapper() {}

    public static Map<String, Object> mapState(Object state) {
        if (state == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        Field[] fields = state.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++ ) {
            fields[i].setAccessible(true);
            String name = "\"" + fields[i].getName() + "\"";
            Object value = null;

            try {
                value = fields[i].get(state);
                if (fields[i].getType().equals(Map.class) || fields[i].getType().equals(HashMap.class)) {
                    value = formatMap(value);
                }
                else if (fields[i].getType().isArray()) {
                    value = formatArray((Object[])value);
                }
                else if (fields[i].getType().equals(java.util.List.class) || fields[i].getType().equals(java.util.ArrayList.class)) {
                    value = formatList((List<Object>)value);
                }
            } catch (Exception e) {
                logger.error("decoding of state failed: " + e.toString(), e);
            }

            if (!Modifier.isTransient(fields[i].getModifiers())) {
                if (value != null) {
                    if (fields[i].getType().isPrimitive()
                            || fields[i].getType().equals(String.class)
                            || fields[i].getType().equals(Map.class)
                            || fields[i].getType().equals(HashMap.class)
                            || fields[i].getType().isArray()
                            || fields[i].getType().equals(java.util.List.class)
                            || fields[i].getType().equals(java.util.ArrayList.class)
                            || objectIsDateType(value)) {
                        if (fields[i].getType().equals(String.class)) {
                            value = "\"" + value + "\"";
                        } else if (objectIsDateType(value)) {
                            value = "\"" + value.toString() + "\"";
                        }
                        map.put(name, value);
                    } else {
                        map.put(name, mapState(value));
                    }
                } else {
                    map.put(name, value);
                }
            }

        }

        return map;
    }

    private static Object formatMap(Object map) {
        Map<String, Object> newMap = new HashMap<>();
        for (Object entry : ((Map) map).entrySet()) {
            String key = ((Map.Entry<String, Object>) entry).getKey();
            Object value = ((Map.Entry<String, Object>) entry).getValue();
            String newKey = "\"" + key + "\"";
            Object newValue = value;
            if (value.getClass().isPrimitive() || value.getClass().equals(String.class) || objectIsDateType(value)) {
                if (value.getClass().equals(String.class)) {
                    newValue = "\"" + value + "\"";
                } else if (objectIsDateType(value)) {
                    newValue = "\"" + value.toString() + "\"";
                }
                newMap.put(newKey, newValue);
            } else if (value.getClass().equals(Map.class) || value.getClass().equals(HashMap.class)) {
                newValue = formatMap(value);
                newMap.put(newKey, newValue);
            } else if (value.getClass().isArray()) {
                newValue = formatArray((Object[]) value);
                newMap.put(newKey, newValue);
            } else if (value.getClass().equals(java.util.List.class) || value.getClass().equals(java.util.ArrayList.class)) {
                newValue = formatList((List<Object>) value);
                newMap.put(newKey, newValue);
            } else {
                newMap.put(newKey, formatMap(newValue));
            }
        }
        return newMap;
    }

    private static Object formatArray(Object[] array) {
        Map<String, Object> newMap = new HashMap<>();
        try {
            for (int i = 0; i < array.length; i++) {
                newMap.put("Array[" + i + "]", array[i]);
            }
        } catch (Exception e) {
            return null;
        }

        return formatMap(newMap);
    }

    private static Object formatList(List<Object> list) {
        Map<String, Object> newMap = new HashMap<>();
        try {
            for (int i = 0; i < list.size(); i++) {
                newMap.put("List[" + i + "]", list.get(i));
            }
        } catch (Exception e) {
            return null;
        }

        return formatMap(newMap);
    }

    private static boolean objectIsDateType(Object value) {
        Boolean result = false;
        if (
                value.getClass().equals(java.util.Date.class) ||
                        value.getClass().equals(java.sql.Date.class) ||
                        value.getClass().equals(java.time.Instant.class) ||
                        value.getClass().equals(java.time.ZonedDateTime.class) ||
                        value.getClass().equals(java.time.LocalDate.class) ||
                        value.getClass().equals(java.time.LocalDateTime.class)) {
            result = true;
        }
        return result;
    }

}
