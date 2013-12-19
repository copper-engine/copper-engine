/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitoring.example.util;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

public class SingleProzessInstanceUtil {

    public static interface KillMXBean {
        void kill();
    }

    public static class KillMBeanImpl implements KillMXBean {

        @Override
        public void kill() {
            System.exit(0);
        }
    }

    public static void enforceSingleProzessInstance() {
        // close old instances to free resource

        ObjectName name;
        try {
            name = new ObjectName("killUtil:type=Kill");
        } catch (MalformedObjectNameException e1) {
            throw new RuntimeException(e1);
        }

        try {

            JMXServiceURL u = new JMXServiceURL(
                    "service:jmx:rmi:///jndi/rmi://localhost/jmxrmi");
            JMXConnector c = JMXConnectorFactory.connect(u);
            MBeanServerConnection mbsc = c.getMBeanServerConnection();
            KillMXBean mbeanProxy = JMX.newMBeanProxy(mbsc, name, KillMXBean.class, true);
            mbeanProxy.kill();
        } catch (Exception e1) {
            // Ignore
        }

        try {
            LocateRegistry.createRegistry(1099);
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            String url = "service:jmx:rmi:///jndi/rmi://localhost/jmxrmi";
            JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(
                    new JMXServiceURL(url), null, server);
            connectorServer.start();

            KillMBeanImpl maze = new KillMBeanImpl();
            server.registerMBean(maze, name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
