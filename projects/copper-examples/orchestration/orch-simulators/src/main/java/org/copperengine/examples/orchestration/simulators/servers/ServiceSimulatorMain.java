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
package org.copperengine.examples.orchestration.simulators.servers;

import javax.xml.ws.Endpoint;

public class ServiceSimulatorMain {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Server");
        Endpoint.publish("http://localhost:9092/NetworkServiceProvider", new NetworkServiceProviderImpl());
        Endpoint.publish("http://localhost:9092/CustomerService", new CustomerServiceImpl());
        Thread.sleep(Long.MAX_VALUE);
    }

}
