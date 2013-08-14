/*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.josso.gatein.agent;

import java.lang.reflect.Method;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.UsernameCredential;
import org.exoplatform.services.security.jaas.AbstractLoginModule;

/**
 * @author <a href="mailto:sshah@redhat.com">Sohil Shah</a>
 */
public final class GateinSSOLoginModule extends AbstractLoginModule
{
    private static final Log log = ExoLogger.getLogger(GateinSSOLoginModule.class
            .getName());

    /** JACC get context method. */
    private static Method getContextMethod;

    static
    {
        try
        {
            Class<?> policyContextClass = Thread.currentThread().getContextClassLoader().loadClass("javax.security.jacc.PolicyContext");
            getContextMethod = policyContextClass.getDeclaredMethod("getContext", String.class);
        }
        catch (ClassNotFoundException ignore)
        {
            log.debug("JACC not found ignoring it", ignore);
        }
        catch (Exception e)
        {
            log.error("Could not obtain JACC get context method", e);
        }
    }

    public boolean login() throws LoginException
    {
        try
        {
            Callback[] callbacks = new Callback[2];
            callbacks[0] = new NameCallback("Username");
            callbacks[1] = new PasswordCallback("Password", false);
            callbackHandler.handle(callbacks);

            String password = new String(((PasswordCallback) callbacks[1])
                    .getPassword());

            //
            // For clustered config check credentials stored and propagated in session. This won't work in tomcat because
            // of lack of JACC PolicyContext so the code must be a bit defensive
            String username = null;
            if (getContextMethod != null && password.startsWith("wci-ticket"))
            {
                HttpServletRequest request;
                try
                {
                    request = (HttpServletRequest)getContextMethod.invoke(null, "javax.servlet.http.HttpServletRequest");
                    username = (String)request.getSession().getAttribute("username");
                }
                catch(Throwable e)
                {
                    log.error(this,e);
                    log.error("LoginModule error. Turn off session credentials checking with proper configuration option of " +
                            "LoginModule set to false");
                }
            }

            if (username == null)
            {
                //SSO token could not be validated...hence a user id cannot be found
                log.error("---------------------------------------------------------");
                log.error("SSOLogin Failed. Credential Not Found!!");
                log.error("---------------------------------------------------------");
                return false;
            }

            //Perform authentication by setting up the proper Application State
            Authenticator authenticator = (Authenticator) getContainer()
                    .getComponentInstanceOfType(Authenticator.class);

            if (authenticator == null)
            {
                throw new LoginException(
                        "No Authenticator component found, check your configuration");
            }

            Identity identity = authenticator.createIdentity(username);

            sharedState.put("exo.security.identity", identity);
            sharedState.put("javax.security.auth.login.name", username);
            subject.getPublicCredentials().add(new UsernameCredential(username));

            return true;
        }
        catch (final Throwable e)
        {
            throw new LoginException(e.getMessage());
        }
    }

    public boolean logout() throws LoginException
    {
        return true;
    }

    public boolean abort() throws LoginException
    {
        return true;
    }

    public boolean commit() throws LoginException
    {
        return true;
    }

    @Override
    protected Log getLogger()
    {
        return log;
    }
}