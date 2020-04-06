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
package org.copperengine.core.util;

public class Blocker {

    private volatile boolean blocked = false;
    private final Object mutex = new Object();

    public Blocker(boolean blocked) {
        this.blocked = blocked;
    }

    public void block() {
        synchronized (mutex) {
            blocked = true;
        }
    }

    public void unblock() {
        synchronized (mutex) {
            blocked = false;
            mutex.notifyAll();
        }
    }

    public void pass() throws InterruptedException {
        if (blocked) {
            synchronized (mutex) {
                while (blocked) {
                    mutex.wait();
                }
            }
        }
    }
}
