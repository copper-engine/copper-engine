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
package org.copperengine.monitoring.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.shiro.SecurityUtils;
import org.copperengine.monitoring.core.CopperMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.RemoteAccessException;

public class CopperMonitorServiceSecurityProxy implements InvocationHandler {

    public static CopperMonitoringService secure(CopperMonitoringService copperMonitoringService) {
        return (CopperMonitoringService) java.lang.reflect.Proxy.newProxyInstance(
                CopperMonitoringService.class.getClassLoader(), new Class[] { CopperMonitoringService.class },
                new CopperMonitorServiceSecurityProxy(copperMonitoringService));
    }

    private static final Logger logger = LoggerFactory.getLogger(CopperMonitorServiceSecurityProxy.class);

    private final CopperMonitoringService copperMonitoringService;

    public CopperMonitorServiceSecurityProxy(CopperMonitoringService copperMonitoringService) {
        this.copperMonitoringService = copperMonitoringService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            return method.invoke(copperMonitoringService, args);
        } else {
            final String text = "user not authenticated: " + SecurityUtils.getSubject().getPrincipal() + " session:" + SecurityUtils.getSubject().getSession().getHost();
            logger.warn(text);
            throw new RemoteAccessException(text);
        }
    }
}