/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.regtest.test;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataHolder {

    private static final Logger logger = LoggerFactory.getLogger(DataHolder.class);

    private final Map<String, Object> map = new HashMap<String, Object>();

    public void clear(String id) {
        logger.info("{} - clear({})", this, id);
        synchronized (map) {
            map.remove(id);
        }
    }

    public void put(String id, Object data) {
        logger.info("{} - put({}, {})", this, id, data);
        synchronized (map) {
            map.put(id, data);
        }
    }

    public Object get(String id) {
        logger.info("{} - get({})", this, id);
        synchronized (map) {
            return map.get(id);
        }
    }
}
