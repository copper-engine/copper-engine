
/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.sample.simulator.customerservice;

import javax.xml.ws.Endpoint;

public class CustomerServiceSimulator{

    public final static String address = "http://localhost:9091/CustomerServicePort";
	
    public static void main(String args[]) throws java.lang.Exception { 
        System.out.println("Starting Server");
        Object implementor = new CustomerServiceImpl();
        Endpoint.publish(address, implementor);

        System.out.println("Server ready..."); 
        
        Thread.sleep(24 * 60 * 60 * 1000); 
        System.out.println("Server exiting");
        System.exit(0);
    }
}
