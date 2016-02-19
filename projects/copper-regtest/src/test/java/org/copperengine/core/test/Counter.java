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
package org.copperengine.core.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Counter {

    private static final Logger logger = LoggerFactory.getLogger(Counter.class);
    private static int counter = 0;
    private static int n;

    public synchronized static void inc() {
        counter++;
        if (counter == n) {
            Counter.class.notify();
        }
        if (counter % 1000 == 0) {
            logger.info(counter + " finished so far...");
        }
    }

    public synchronized static void doWait(int n) {
        Counter.n = n;
        while (counter < n) {
            try {
                Counter.class.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
