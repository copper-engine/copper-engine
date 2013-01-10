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
package de.scoopgmbh.copper.examples.orechstration.simulators.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.customerservice.Customer;
import de.scoopgmbh.customerservice.CustomerService;
import de.scoopgmbh.customerservice.CustomerType;
import de.scoopgmbh.customerservice.GetCustomersByMsisdnRequest;
import de.scoopgmbh.customerservice.GetCustomersByMsisdnResponse;

@javax.jws.WebService(
                      serviceName = "CustomerServiceService",
                      portName = "CustomerServicePort",
                      targetNamespace = "http://customerservice.scoopgmbh.de/",
                      wsdlLocation = "classpath:wsdl/CustomerService.wsdl",
                      endpointInterface = "de.scoopgmbh.customerservice.CustomerService")
                      
public class CustomerServiceImpl implements CustomerService {
	
	private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    /* (non-Javadoc)
     * @see de.scoopgmbh.customerservice.CustomerService#getCustomersByMsisdn(de.scoopgmbh.customerservice.GetCustomersByMsisdnRequest  parameters )*
     */
    public de.scoopgmbh.customerservice.GetCustomersByMsisdnResponse getCustomersByMsisdn(GetCustomersByMsisdnRequest parameters) {
    	logger.info("getCustomersByMsisdn(msisdn={})", parameters.getMsisdn());
    	GetCustomersByMsisdnResponse response = new GetCustomersByMsisdnResponse();
    	response.setReturn(new Customer());
    	response.getReturn().setContractNumber(Long.toHexString(Long.parseLong(parameters.getMsisdn())));
    	response.getReturn().setMsisdn(parameters.getMsisdn());
    	response.getReturn().setType(Long.parseLong(parameters.getMsisdn()) % 2 == 0 ? CustomerType.BUSINESS : CustomerType.PRIVATE);
    	response.getReturn().setSecret("sc00p");
    	return response;
    }

}
