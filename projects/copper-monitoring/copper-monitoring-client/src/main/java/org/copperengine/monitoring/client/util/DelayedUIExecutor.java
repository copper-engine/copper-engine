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
