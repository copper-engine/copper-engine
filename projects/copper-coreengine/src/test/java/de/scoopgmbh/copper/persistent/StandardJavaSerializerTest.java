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
package de.scoopgmbh.copper.persistent;

import junit.framework.TestCase;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowFactory;
import de.scoopgmbh.copper.common.JdkRandomUUIDFactory;
import de.scoopgmbh.copper.wfrepo.FileBasedWorkflowRepository;

public class StandardJavaSerializerTest extends TestCase {
	
	@SuppressWarnings("unchecked")
	public void testX() throws Exception {
		final int SIZE = 20*1024;
		StringBuilder dataSB = new StringBuilder(SIZE);
		for (int i=0; i<SIZE; i++) {
			int pos = (int)(Math.random()*70.0);
			dataSB.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890!§$%&/()=?".substring(pos,pos+1));
		}
		final String data = dataSB.toString(); 
		
		FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
		repo.setSourceDir("src/workflow/java");
		repo.setTargetDir("target/compiled_workflow");
		repo.start();
		WorkflowFactory<String> wfFactory = repo.createWorkflowFactory("de.scoopgmbh.copper.test.PersistentSpock2GTestWF");
		Workflow<String> wf = wfFactory.newInstance();
		JdkRandomUUIDFactory idFactory = new JdkRandomUUIDFactory();
		wf.setId(idFactory.createId());
		wf.setPriority(5);
		wf.setData(data);
		wf.setProcessorPoolId("P#DEFAULT");
		
		Serializer serializer = new StandardJavaSerializer();
		String _data = serializer.serializeWorkflow(wf);
		Workflow<String> wf2 = (Workflow<String>) serializer.deserializeWorkflow(_data, repo);
		assertNull(wf2.getId());
		assertEquals(wf.getData(), wf2.getData());
	}

}
