package de.scoopgmbh.copper.monitoring.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.RemoteAccessException;

import de.scoopgmbh.copper.monitoring.core.CopperMonitoringService;

public class CopperMonitorServiceSecurityProxy implements InvocationHandler {
	
	public static CopperMonitoringService wrapWithSecurity(CopperMonitoringService copperMonitoringService){
		return (CopperMonitoringService)java.lang.reflect.Proxy.newProxyInstance(
				CopperMonitoringService.class.getClassLoader(),new Class[]{ CopperMonitoringService.class},
				new CopperMonitorServiceSecurityProxy(copperMonitoringService));
	}
	
	private static final Logger logger = LoggerFactory.getLogger(CopperMonitorServiceSecurityProxy.class);

	private final CopperMonitoringService copperMonitoringService;
	
	public CopperMonitorServiceSecurityProxy(CopperMonitoringService copperMonitoringService) {
		this.copperMonitoringService = copperMonitoringService;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (SecurityUtils.getSubject().isAuthenticated()){
			return method.invoke(copperMonitoringService, args);
		} else {
			logger.warn("user not authenticated: "+SecurityUtils.getSubject().getPrincipal());
			throw new RemoteAccessException("user not Authenticated: "+SecurityUtils.getSubject().getPrincipal());
		}
	}
}