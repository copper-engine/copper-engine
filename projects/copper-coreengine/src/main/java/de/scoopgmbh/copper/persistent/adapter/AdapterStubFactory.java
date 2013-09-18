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
package de.scoopgmbh.copper.persistent.adapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import de.scoopgmbh.copper.persistent.EntityPersister;
import de.scoopgmbh.copper.persistent.PersistenceContext;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;
import de.scoopgmbh.copper.persistent.SavepointAware;

public class AdapterStubFactory {
	
	static final Method savepointAwareOnSaveMethod;
	static {
		try {
			savepointAwareOnSaveMethod = SavepointAware.class.getMethod("onSave", new Class<?>[]{PersistenceContext.class});
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new RuntimeException(e);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public <A> A createAdapter(Class<A> adapterClass) {
		ArrayList<Class<?>> interfaces = new ArrayList<Class<?>>(Arrays.asList(adapterClass.getInterfaces()));
		if (adapterClass.isInterface())
			interfaces.add(adapterClass);
		return (A)Proxy.newProxyInstance (adapterClass.getClassLoader(), interfaces.toArray(new Class[0]), createInvocationHandler(adapterClass.getName()));
	}
	
	static InvocationHandler createInvocationHandler(final String adapterId) {
		InvocationHandler h = new InvocationHandler() {
			
			Collection<AdapterCall> calls;
			
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				if (method == savepointAwareOnSaveMethod) { 
					onSave((PersistenceContext)args[0]);
					return null;
				}
				String correlationId = UUID.randomUUID().toString();
				Object ret = Void.class;
				if (method.getReturnType() == void.class)
					ret = null;
				if (method.getReturnType() == String.class)
					ret = correlationId;
				if (method.getReturnType() == AdapterCall.class) {
					AdapterCall c = new AdapterCall(adapterId, correlationId, method, args);
					if (calls == null) {
						calls = new ArrayList<AdapterCall>();
						calls.add(c);
					}
					ret = c;					
				}
				if (ret == Void.class)
					throw new RuntimeException("Only return types void, String and AdapterCall allowed");
						return ret;
			}

			public void onSave(PersistenceContext pc) {
				if (calls == null)
					return;
				PersistentWorkflow<?> wf = pc.getWorkflow();
				EntityPersister<AdapterCall> persister = pc.getPersister(AdapterCall.class);
				for (AdapterCall call : calls) {
					call.setWorkflowData(wf.getId(), wf.getPriority());
					persister.insert(call);
				}
			}

		};
		return h;
	}


}
