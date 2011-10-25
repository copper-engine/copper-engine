/*
 * Copyright 2002-2011 SCOOP Software GmbH
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
package de.scoopgmbh.copper.sample.simplews.nocopper;

import de.scoopgmbh.customerservice.Customer;
import de.scoopgmbh.customerservice.CustomerService;
import de.scoopgmbh.orchestration.OrchestrationService;

@javax.jws.WebService(
                      serviceName = "OrchestrationService",
                      portName = "OrchestrationServicePort",
                      targetNamespace = "http://orchestration.scoopgmbh.de/",
                      wsdlLocation = "file:wsdl/Orchestration.wsdl",
                      endpointInterface = "de.scoopgmbh.orchestration.OrchestrationService")
                      
public class OrchestrationServiceImpl implements OrchestrationService {

    private CustomerService customerService;
    
    public void setCustomerService(CustomerService customerService) {
		this.customerService = customerService;
	}

    /* (non-Javadoc)
     * @see de.scoopgmbh.orchestration.OrchestrationService#resetMailbox(java.lang.String  customername )*
     */
    public void resetMailbox(java.lang.String customername) { 
        System.out.println("resetMailbox("+customername+")");
        try {
			System.out.println("started");

			System.out.println("calling 'getCustomersByName("+customername+")'");
        	Customer c = customerService.getCustomersByName(customername);
			System.out.println("customer="+c);
			
			System.out.println("calling 'resetMailbox("+c.getCustomerId()+")'");
        	customerService.resetMailbox(c.getCustomerId());
			System.out.println("finished");
        } 
        catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
}
