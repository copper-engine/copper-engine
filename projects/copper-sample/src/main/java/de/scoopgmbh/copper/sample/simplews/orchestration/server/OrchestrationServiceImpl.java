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
package de.scoopgmbh.copper.sample.simplews.orchestration.server;

import de.scoopgmbh.copper.CopperException;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowFactory;
import de.scoopgmbh.copper.persistent.PersistentScottyEngine;
import de.scoopgmbh.copper.tranzient.TransientScottyEngine;
import de.scoopgmbh.orchestration.OrchestrationService;

@javax.jws.WebService(
                      serviceName = "OrchestrationService",
                      portName = "OrchestrationServicePort",
                      targetNamespace = "http://orchestration.scoopgmbh.de/",
                      wsdlLocation = "file:wsdl/Orchestration.wsdl",
                      endpointInterface = "de.scoopgmbh.orchestration.OrchestrationService")
                      
public class OrchestrationServiceImpl implements OrchestrationService {

    private static final String syncWorkflow  = "de.scoopgmbh.copper.sample.orchestration.workflows.ResetMailboxWorkflow";
    private static final String asyncWorkflow = "de.scoopgmbh.copper.sample.orchestration.workflows.AsyncResetMailboxWorkflow";
    
    private static final boolean async = true;
    private static final boolean persistent = true;
    

    private TransientScottyEngine transientEngine;
    private PersistentScottyEngine persistentEngine;
    
    public void setTransientEngine(TransientScottyEngine transientEngine) {
		this.transientEngine = transientEngine;
	}
    
    public void setPersistentEngine(PersistentScottyEngine persistentEngine) {
		this.persistentEngine = persistentEngine;
	}

    /* (non-Javadoc)
     * @see de.scoopgmbh.orchestration.OrchestrationService#resetMailbox(java.lang.String  customername )*
     */
    public void resetMailbox(java.lang.String customername) {
    	try {
    		System.out.println("resetMailbox("+customername+")");
    		ResetMailboxRequest req = new ResetMailboxRequest();
    		req.setCustomerName(customername);
    		
    		if (persistent) {
        		// Use the persistent engine, i.e. the process instance will be stored in the underlying database
        		launchPersistentProcess(req);
    		}
    		else {
        		// Use the transient engine, i.e. the process instance will be stored in memory only
        		launchTransientProcess(req);
    		}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }

	private void launchTransientProcess(ResetMailboxRequest req) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		WorkflowFactory<ResetMailboxRequest> factory = transientEngine.createWorkflowFactory(async ? asyncWorkflow : syncWorkflow);

		// Attention!! Do not try to use the Workflow class directly, e.g. 
		// de.scoopgmbh.copper.sample.orchestration.workflows.ResetMailboxWorkflow wf = (ResetMailboxWorkflow) factory.newInstance();
		// this will lead to a class cast exception, because of class cloader stuff
		// The correct way is:
		Workflow<ResetMailboxRequest> wf = factory.newInstance();
		
		wf.setData(req);
		transientEngine.run(wf);
	}
    
	private void launchPersistentProcess(ResetMailboxRequest req) throws ClassNotFoundException, InstantiationException, IllegalAccessException, CopperException {
		WorkflowFactory<ResetMailboxRequest> factory = persistentEngine.createWorkflowFactory(async ? asyncWorkflow : syncWorkflow);
		
		// Attention!! Do not try to use the Workflow class directly, e.g. 
		//de.scoopgmbh.copper.sample.orchestration.workflows.ResetMailboxWorkflow wf = (ResetMailboxWorkflow) factory.newInstance();
		// this will lead to a class cast exception, because of class cloader stuff
		// The correct way is:
		Workflow<ResetMailboxRequest> wf = factory.newInstance();
		//AsyncResetMailboxWorkflow wf = (AsyncResetMailboxWorkflow) factory.newInstance();
		wf.setData(req);
		persistentEngine.run(wf);
	}
    
 

}
