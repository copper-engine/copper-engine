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
package org.copperengine.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Simple utility class to load and run a Spring FileSystemXmlApplicationContext.
 *
 * @author austermann
 */
public class SpringEngineStarter {

    private static final Logger logger = LoggerFactory.getLogger(SpringEngineStarter.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: " + SpringEngineStarter.class.getName() + " <configLocations>");
            System.exit(-2);
        }
        try {
            new FileSystemXmlApplicationContext(args);
        } catch (Exception e) {
            logger.error("Startup failed", e);
            System.exit(-1);
        }
    }

}
