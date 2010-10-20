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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The default SSO Web configuration implementation.
 *
 * @org.apache.xbean.XBean element="web-configuration"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOWebConfigurationImpl.java 612 2008-08-22 12:17:20Z gbrigand $
 */

public class SSOWebConfigurationImpl implements SSOWebConfiguration {

    private static final Log logger = LogFactory.getLog(SSOWebConfigurationImpl.class);

    private static final String DEFAULT_P3P_HEADER_VALUE="CP=\"CAO PSA OUR\"";

    private String _loginBackToURL;

    private String _logoutBackToURL;

    private String _customLoginURL;

    private boolean _sessionTokenSecure = false;

    private int _userMaxSessions;

    private boolean _sessionTokenOnClient;

    private boolean _rememberMeEnabled;
    
    private boolean _basicAuthenticationEnabled = true;
    
    private boolean _strongAuthenticationEnabled = true;
    
    private boolean _ntlmAuthenticationEnabled = true;

    private int _rememberMeMaxAge;

    private boolean _sendP3PHeader;

    private String _p3pHeaderValue;

    private List<String> _trustedHosts = new ArrayList<String>();

    public String getLoginBackToURL() {
        return _loginBackToURL;
    }

    public void setLoginBackToURL(String loginBackToURL) {
        _loginBackToURL = loginBackToURL;
    }

    public String getLogoutBackToURL() {
        return _logoutBackToURL;
    }

    public void setLogoutBackToURL(String logoutBackToURL) {
        _logoutBackToURL = logoutBackToURL;
    }

    /**
     */
    public boolean isSessionTokenSecure() {
        return _sessionTokenSecure;
    }

    public void setSessionTokenSecure(String sessionTokenSecure) {
        _sessionTokenSecure = Boolean.valueOf(sessionTokenSecure).booleanValue();
    }

    public void setSessionTokenSecure(boolean b) {
        _sessionTokenSecure = b;
    }

    public boolean getSessionTokenOnClient() {
        return _sessionTokenOnClient;
    }

    public boolean isSessionTokenOnClient() {
        return _sessionTokenOnClient;
    }

    public void setSessionTokenOnClient(boolean sessionTokenOnClient) {
        _sessionTokenOnClient = sessionTokenOnClient;
    }

    public String getCustomLoginURL() {
        return _customLoginURL;
    }

    public void setCustomLoginURL(String customLoginURL) {
        _customLoginURL = customLoginURL;
    }

    public boolean isRememberMeEnabled() {
        return _rememberMeEnabled;
    }

    public void setRememberMeEnabled(boolean _rememberMeEnabled) {
        this._rememberMeEnabled = _rememberMeEnabled;
    }

    public boolean isBasicAuthenticationEnabled() {
        return _basicAuthenticationEnabled;
    }

    public void setBasicAuthenticationEnabled(boolean _basicAuthenticationEnabled) {
        this._basicAuthenticationEnabled = _basicAuthenticationEnabled;
    }
    
    public boolean isStrongAuthenticationEnabled() {
        return _strongAuthenticationEnabled;
    }

    public void setStrongAuthenticationEnabled(boolean _strongAuthenticationEnabled) {
        this._strongAuthenticationEnabled = _strongAuthenticationEnabled;
    }
    
    public boolean isNtlmAuthenticationEnabled() {
        return _ntlmAuthenticationEnabled;
    }

    public void setNtlmAuthenticationEnabled(boolean _ntlmAuthenticationEnabled) {
        this._ntlmAuthenticationEnabled = _ntlmAuthenticationEnabled;
    }
    
    /**
     * @return max age in minutes that a user authentication will be remembered if the user does not login again.
     */
    public int getRememberMeMaxAge() {
        return _rememberMeMaxAge;
    }

    /**
     *
     * @param rememberMeMaxAge max age in minuts that a user authentication will be remembered if the user does not login again
     */
    public void setRememberMeMaxAge(int rememberMeMaxAge) {
        this._rememberMeMaxAge = rememberMeMaxAge;
    }

    public boolean isSendP3PHeader() {
        return _sendP3PHeader;
    }

    public void setSendP3PHeader(boolean value) {
        this._sendP3PHeader = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.josso.selfservices.password.generator.IPassowrdFilter#setBlacklist(java.util.List)
     * @org.apache.xbean.Property alias="trusted-hosts" nestedType="java.lang.String"
     */
    public List<String> getTrustedHosts() {
        return _trustedHosts;
    }

    public void setTrustedHosts(List<String> trustedHosts) {
        this._trustedHosts = trustedHosts;
    }

    /**
     * @org.apache.xbean.Property alias="P3PHeaderValue"
     * @return
     */
    public String getP3PHeaderValue() {
        return _p3pHeaderValue != null ? _p3pHeaderValue : DEFAULT_P3P_HEADER_VALUE;
    }

    public void setP3PHeaderValue(String value) {
        this._p3pHeaderValue = value;
    }
}
