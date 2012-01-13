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
package de.scoopgmbh.copper.sample.simplews.customer.server;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Response;

import de.scoopgmbh.customerservice.Customer;
import de.scoopgmbh.customerservice.CustomerService;
import de.scoopgmbh.customerservice.CustomerType;
import de.scoopgmbh.customerservice.GetCustomersByNameResponse;
import de.scoopgmbh.customerservice.NoSuchCustomerException;
import de.scoopgmbh.customerservice.ResetMailboxResponse;

@javax.jws.WebService(
                      serviceName = "CustomerServiceService",
                      portName = "CustomerServicePort",
                      targetNamespace = "http://customerservice.scoopgmbh.de/",
                      wsdlLocation = "file:wsdl/CustomerService.wsdl",
                      endpointInterface = "de.scoopgmbh.customerservice.CustomerService")
                      
public class CustomerServiceImpl implements CustomerService {

    private static final Logger LOG = Logger.getLogger(CustomerServiceImpl.class.getName());

    /* (non-Javadoc)
     * @see de.scoopgmbh.customerservice.CustomerService#resetMailbox(int  customerId )*
     */
    public void resetMailbox(int customerId) throws NoSuchCustomerException    { 
        System.out.println("resetMailbox("+customerId+")");
        
        try {
			Thread.sleep(2000);
		} 
        catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        System.out.println("return");
    }

    /* (non-Javadoc)
     * @see de.scoopgmbh.customerservice.CustomerService#getCustomersByName(java.lang.String  name )*
     */
    public de.scoopgmbh.customerservice.Customer getCustomersByName(java.lang.String name) throws NoSuchCustomerException    { 
        System.out.println("getCustomersByName("+name+")");
        
        try {
			Thread.sleep(2000);
		} 
        catch (InterruptedException e) {
			e.printStackTrace();
		}

        Customer c = new Customer();
        c.setCustomerId(4711);
        c.setMsisdn("491701234567");
        c.setType(CustomerType.BUSINESS);

        System.out.println("return "+c);
        return c;
    }

	@Override
	public Response<ResetMailboxResponse> resetMailboxAsync(int customerId) {
        return null; 
        /*not called */
	}

	@Override
	public Future<?> resetMailboxAsync(int customerId, AsyncHandler<ResetMailboxResponse> asyncHandler) {
        return null; 
        /*not called */
	}

	@Override
	public Response<GetCustomersByNameResponse> getCustomersByNameAsync(String name) {
        return null; 
        /*not called */
	}

	@Override
	public Future<?> getCustomersByNameAsync(String name, AsyncHandler<GetCustomersByNameResponse> asyncHandler) {
        return null; 
        /*not called */
	}    
	
    public static void main(String args[]) throws java.lang.Exception { 
        System.out.println("Starting Server");
        Object implementor = new CustomerServiceImpl();
        String address = "http://localhost:9091/CustomerServicePort";
        Endpoint.publish(address, implementor);
        System.out.println("Server ready..."); 
        
        Thread.sleep(60 * 60 * 1000); 
        System.out.println("Server exiting");
        System.exit(0);
    }

	
}
