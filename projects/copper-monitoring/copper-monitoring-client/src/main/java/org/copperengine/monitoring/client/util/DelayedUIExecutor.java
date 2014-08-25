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
package org.copperengine.monitoring.client.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DelayedUIExecutor {
    private static final Logger logger = LoggerFactory.getLogger(DelayedUIExecutor.class);

    abstract public void execute() throws Exception;
    
    public void executeWithDelays(long... seconds) {
        Callable<Void> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            execute();
                        } catch (Exception e) {
                            logger.error("Failed to execute UI task.", e);
                        }                
                    }                    
                });
                return null;
            }
        };
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        for(long sec : seconds) {
            scheduledExecutor.schedule(task, sec, TimeUnit.SECONDS);
        }
    }
    
}
