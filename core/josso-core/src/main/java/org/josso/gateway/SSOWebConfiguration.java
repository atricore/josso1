/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
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
 *
 */
package org.josso.gateway;

import java.util.Collection;
import java.util.List;

/**
 * SSO Web configuration properties.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOWebConfiguration.java 612 2008-08-22 12:17:20Z gbrigand $
 */
public interface SSOWebConfiguration {

    /**
     * Default URL where the user will be redirected to after a successfull login
     */
    String getLoginBackToURL();

    /**
     * Default URL where the user will be redirected to after a logout
     */
    String getLogoutBackToURL();

    /**
     * This is the URL of the custom login interface
     */
    String getCustomLoginURL();

    /**
     * Flag to indicate that the authorization token must be secure.
     */
    boolean isSessionTokenSecure();

    /**
     * Flag to indicate that the sso token must live client-side
     */
    boolean isSessionTokenOnClient();

    /**
     * Flag to indicate that authentications must be remembered (stored in user applications)
     * @return
     */
    boolean isRememberMeEnabled();
    
    /**
     * Flag to indicate if basic authentication is enabled
     * @return
     */
    boolean isBasicAuthenticationEnabled();
    
    /**
     * Flag to indicate if strong authentication is enabled
     * @return
     */
    boolean isStrongAuthenticationEnabled();
    
    /**
     * Flag to indicate if ntlm authentication is enabled
     * @return
     */
    boolean isNtlmAuthenticationEnabled();

    /**
     * @return max age in minutes that a user authentication will be remembered if the user does not login again.
     */
    int getRememberMeMaxAge();

    /**
     * @return Flag to indicate that a P3P header must be generated as part of the response
     */
    boolean isSendP3PHeader();


    /**
     * @return P3P Header value
     */
    String getP3PHeaderValue();

    /**
     * @return List of trusted domains to redirectc the user back to after login/logout.
     */
    List<String> getTrustedHosts();

    void setTrustedHosts(List<String> trustedHosts);

}
