/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.core.common;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of the {@link IdFactory} interface, using an {@link AtomicLong}.
 * The AtomicLong is initialized with <code>System.currentTimeMillis()*1000</code> at system start.
 *
 * @author austermann
 */
public class AtomicLongIdFactory implements IdFactory {

    private static final AtomicLong x = new AtomicLong(System.currentTimeMillis() * 1000);

    @Override
    public String createId() {
        return new StringBuilder(Long.toHexString(x.incrementAndGet())).reverse().toString();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void startup() {
    }

}
