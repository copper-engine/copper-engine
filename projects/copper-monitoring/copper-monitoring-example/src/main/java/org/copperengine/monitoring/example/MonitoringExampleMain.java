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
package org.copperengine.monitoring.example;

import javafx.application.Platform;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.copperengine.monitoring.client.main.MonitorMain;
import org.copperengine.monitoring.example.util.SingleProzessInstanceUtil;

public class MonitoringExampleMain {

    public static void main(String[] args) {
        new MonitoringExampleMain().start(args);
    }

    public void start(String[] args) {
        SingleProzessInstanceUtil.enforceSingleProzessInstance();

        final boolean unsecure = Boolean.getBoolean("unsecureCopperMonitoring");
        LogManager.getRootLogger().setLevel(Level.INFO);
        System.out.println("Copper monitoring using " + (unsecure ? "un" : "") + "secure remote invocation.");

        final String host = (args.length > 0) ? args[0] : "localhost";
        final int port = (args.length > 1) ? Integer.parseInt(args[1]) : 8080;

        new Thread(){
            @Override
            public void run() {
                getApplicationContext().createServer(host, port, unsecure).start();
            }
        }.start();


        MonitorMain.main(new String[]{"--monitorServerAdress=http://localhost:8080","--monitorServerUser=user1","--monitorServerPassword=pass1"});
        Platform.setImplicitExit(true);
    }

    protected ApplicationContext getApplicationContext() {
        return new ApplicationContext();
    }


}
