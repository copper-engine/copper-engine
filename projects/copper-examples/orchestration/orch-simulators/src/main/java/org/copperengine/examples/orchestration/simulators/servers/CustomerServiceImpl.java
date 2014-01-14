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
package org.copperengine.examples.orchestration.simulators.servers;

import org.copperengine.customerservice.Customer;
import org.copperengine.customerservice.CustomerService;
import org.copperengine.customerservice.CustomerType;
import org.copperengine.customerservice.GetCustomersByMsisdnRequest;
import org.copperengine.customerservice.GetCustomersByMsisdnResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@javax.jws.WebService(
        serviceName = "CustomerServiceService",
        portName = "CustomerServicePort",
        targetNamespace = "http://customerservice.copperengine.org/",
        wsdlLocation = "classpath:wsdl/CustomerService.wsdl",
        endpointInterface = "org.copperengine.customerservice.CustomerService")
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    /*
     * (non-Javadoc)
     * 
     * @see de.scoopgmbh.customerservice.CustomerService#getCustomersByMsisdn(de.scoopgmbh.customerservice.
     * GetCustomersByMsisdnRequest parameters )*
     */
    @Override
    public GetCustomersByMsisdnResponse getCustomersByMsisdn(GetCustomersByMsisdnRequest parameters) {
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
