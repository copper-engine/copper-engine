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
package org.copperengine.monitoring.server;

import java.rmi.RemoteException;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.copperengine.monitoring.core.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureLoginService implements LoginService {
    private static final long serialVersionUID = 8412747004504683148L;
    static final Logger logger = LoggerFactory.getLogger(SpringRemotingServer.class);

    public SecureLoginService(Realm realm) {
        super();
        SecurityUtils.setSecurityManager(new DefaultSecurityManager(realm));
    }

    @Override
    public String doLogin(String username, String password) throws RemoteException {
        // get the currently executing user:
        Subject currentUser = SecurityUtils.getSubject();
        // Session session = currentUser.getSession(true);
        // log.info(session.getId().toString());

        // let's log in the current user so we can check against roles and permissions:
        if (!currentUser.isAuthenticated()) {
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            token.setRememberMe(true);
            try {

                currentUser.login(token);
                token.clear();

                // if (this.concurrentSessionControl == true) {
                // removeConcurrentSessions(currentUser);
                // }

                String sessionId = currentUser.getSession(false).getId().toString();
                currentUser.getSession(false).setTimeout(1000 * 60 * 60 * 24);
                logger.info(sessionId);
                return sessionId;
            } catch (UnknownAccountException uae) {
                logger.info("There is no user with username of " + token.getPrincipal());
            } catch (IncorrectCredentialsException ice) {
                logger.info("Password for account " + token.getPrincipal() + " was incorrect!");
            } catch (LockedAccountException lae) {
                logger.info("The account for username " + token.getPrincipal() + " is locked.  "
                        + "Please contact your administrator to unlock it.");
            } catch (AuthenticationException ae) {
                logger.info(null, ae);
            }
            return null;
        } else {
            return currentUser.getSession(false).getId().toString();
        }
    }

    // private void removeConcurrentSessions(Subject currentUser) throws InvalidSessionException, CacheException {
    // String cacheName = ((CachingSessionDAO) ((DefaultSessionManager)
    // securityManager.getSessionManager()).getSessionDAO())
    // .getActiveSessionsCacheName();
    // Cache cache = securityManager.getCacheManager().getCache(cacheName);
    // log.debug("using cache: " + cacheName);
    // Iterator iter = cache.keys().iterator();
    // while (iter.hasNext()) {
    // String sess = (String) iter.next();
    // log.debug("key: " + sess);
    // if (sess.equals(currentUser.getSession(false).getId())) {
    // log.debug("removeConcurrentSessions: skip current session");
    // continue;
    // }
    // Object objKeys = cache.get(sess);
    // Session objSess = (Session) objKeys;
    // // Collection<Object> keys = objSess.getAttributeKeys();
    // if (objSess != null) {
    // Collection keys = objSess.getAttributeKeys();
    // for (Object obj : keys) {
    // log.debug("key name: " + obj.toString());
    // // SessionSubjectBinder.AUTHENTICATED_SESSION_KEY - bolean
    // // SessionSubjectBinder.PRINCIPALS_SESSION_KEY - PrincipalCollection
    // }
    // PrincipalCollection principalCollection = (PrincipalCollection) objSess
    // .getAttribute(SessionSubjectBinder.PRINCIPALS_SESSION_KEY);
    // if (principalCollection != null) {
    // for (Object obj : principalCollection.asList()) {
    // log.debug("principal name: " + obj.toString());
    // if (obj.toString().equals("user1")) {
    // log.debug("user user1 already logged in. remove its previous session");
    // cache.remove(sess);
    // }
    // }
    // }
    // }
    // log.debug("");
    // }
    // }

}
