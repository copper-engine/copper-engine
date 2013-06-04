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
package de.scoopgmbh.copper.examples.orechstration.adapter;

import de.scoopgmbh.copper.Acknowledge;
import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.network.mobile.services.AsyncNetworkServiceResponseReceiver;
import de.scoopgmbh.network.mobile.services.NetworkServiceProvider;
import de.scoopgmbh.network.mobile.services.ResetMailboxAcknowledge;
import de.scoopgmbh.network.mobile.services.ResetMailboxRequest;
import de.scoopgmbh.network.mobile.services.SendSmsRequest;

@javax.jws.WebService(
        serviceName = "AsyncNetworkServiceResponseReceiverService",
        portName = "AsyncNetworkServiceResponseReceiver",
        targetNamespace = "http://services.mobile.network.scoopgmbh.de/",
        wsdlLocation = "classpath:wsdl/MobileNetworkServices.wsdl",
        endpointInterface = "de.scoopgmbh.network.mobile.services.AsyncNetworkServiceResponseReceiver")
public class NetworkServiceAdapterImpl implements NetworkServiceAdapter, AsyncNetworkServiceResponseReceiver {
	
	private ProcessingEngine engine;
	private String callbachURI;
	private NetworkServiceProvider networkServiceProvider;
	
	public void setEngine(ProcessingEngine engine) {
		this.engine = engine;
	}
	
	public void setCallbachURI(String callbachURI) {
		this.callbachURI = callbachURI;
	}
	
	public void setNetworkServiceProvider(NetworkServiceProvider networkServiceProvider) {
		this.networkServiceProvider = networkServiceProvider;
	}

	@Override
	public String resetMailbox(String msisdn) {
		ResetMailboxRequest parameters = new ResetMailboxRequest();
		parameters.setMsisdn(msisdn);
		parameters.setCallback(callbachURI);
		ResetMailboxAcknowledge rv = networkServiceProvider.asyncResetMailbox(parameters );
		return rv.getCorrelationId();
	}

	@Override
	public void resetMailboxResponse(String correlationId, boolean success, int returnCode) {
		ResetMailboxResponse payload = new ResetMailboxResponse(success, returnCode);
		Response<ResetMailboxResponse> response = new Response<ResetMailboxResponse>(correlationId, payload, null);
		Acknowledge.DefaultAcknowledge ack = new Acknowledge.DefaultAcknowledge(); 
		engine.notify(response, ack);
		ack.waitForAcknowledge();
	}

	@Override
	public void sendSMS(String msisdn, String msg) {
		SendSmsRequest parameters = new SendSmsRequest();
		parameters.setMessage(msg);
		parameters.setMsisdn(msisdn);
		networkServiceProvider.sendSMS(parameters);
	}

}
