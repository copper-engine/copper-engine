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
package org.copperengine.examples.orchestration.simulators.servers;

import java.net.URL;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.copperengine.network.mobile.services.AsyncNetworkServiceResponseReceiver;
import org.copperengine.network.mobile.services.AsyncNetworkServiceResponseReceiverService;
import org.copperengine.network.mobile.services.Empty;
import org.copperengine.network.mobile.services.NetworkServiceProvider;
import org.copperengine.network.mobile.services.ResetMailboxAcknowledge;
import org.copperengine.network.mobile.services.ResetMailboxRequest;
import org.copperengine.network.mobile.services.SendSmsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@javax.jws.WebService(
        serviceName = "NetworkServiceProviderService",
        portName = "NetworkServiceProviderPort",
        targetNamespace = "http://services.mobile.network.copperengine.org/",
        wsdlLocation = "classpath:wsdl/MobileNetworkServices.wsdl",
        endpointInterface = "org.copperengine.network.mobile.services.NetworkServiceProvider")
public class NetworkServiceProviderImpl implements NetworkServiceProvider {

	private static final Logger logger = LoggerFactory.getLogger(NetworkServiceProviderImpl.class);

	private AtomicLong cidFactory = new AtomicLong(System.currentTimeMillis()*10L);
	private ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
	private Random random = new Random();

	@Override
	public ResetMailboxAcknowledge asyncResetMailbox(final ResetMailboxRequest parameters) { 
		logger.info("asyncResetMailbox(msisdn={})", parameters.getMsisdn());
		final String correlationId = Long.toHexString(cidFactory.incrementAndGet());
		final ResetMailboxAcknowledge ack = new ResetMailboxAcknowledge();
		ack.setReturnCode(0);
		ack.setCorrelationId(correlationId);

		threadPool.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					final AsyncNetworkServiceResponseReceiverService ss = new AsyncNetworkServiceResponseReceiverService(new URL(parameters.getCallback()));
					final AsyncNetworkServiceResponseReceiver port = ss.getAsyncNetworkServiceResponseReceiver();
					final boolean success = random.nextBoolean();
					logger.info("Sending async reponse: correlationId={}, success={}", correlationId, success);
					port.resetMailboxResponse(correlationId,success,0);
				}
				catch(Exception e) {
					logger.error("Unable to send async response",e);
				}
			}
		}, 5, TimeUnit.SECONDS);
		return ack;
	}

	@Override
	public Empty sendSMS(SendSmsRequest parameters) {
		logger.info("sendSMS(msisdn={}, msg={})", parameters.getMsisdn(), parameters.getMessage());
		return new Empty();
	}

}
