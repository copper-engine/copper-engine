package de.scoopgmbh.copper.persistent.adapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import de.scoopgmbh.copper.persistent.PersistentWorkflow;

public class AdapterStubFactory {
	
	
	public static <A> A createAdapter(final PersistentWorkflow<?> wf, String adapterId, Class<A> adapterClass) {
		ArrayList<Class<?>> interfaces = new ArrayList<Class<?>>(Arrays.asList(adapterClass.getInterfaces()));
		if (adapterClass.isInterface())
			interfaces.add(adapterClass);
		return (A)Proxy.newProxyInstance (adapterClass.getClassLoader(), interfaces.toArray(new Class[0]), createInvocationHandler(wf, adapterId));
	}
	
	static InvocationHandler createInvocationHandler(final PersistentWorkflow<?> wf, final String adapterId) {
		InvocationHandler h = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				if (method.getReturnType() != null)
					throw new RuntimeException("Cannot buffer call with non-void return type");
				AdapterCall c = new AdapterCall(wf.getId(), adapterId, UUID.randomUUID().toString(), method, args, wf.getPriority());
				wf.addAdapterCall(c);
				return null;
			}
		};
		return h;
	}

}
