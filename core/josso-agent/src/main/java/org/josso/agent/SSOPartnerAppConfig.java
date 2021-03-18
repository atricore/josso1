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
package org.josso.agent;

import org.josso.gateway.GatewayServiceLocator;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.josso.gateway.identity.service.SSOIdentityProviderService;
import org.josso.gateway.session.service.SSOSessionManagerService;

import java.io.Serializable;

/**
 * @org.apache.xbean.XBean element="partner-app"
 *
 * SSO Partner Application configuration.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOPartnerAppConfig.java 598 2008-08-16 05:41:50Z gbrigand $
 */

public class SSOPartnerAppConfig implements Serializable {

    private static final String DEFAULT_P3P_HEADER_VALUE="CP=\"CAO PSA OUR\"";

    private String _id;
    private String _context;
    private String _vhost;
    private String[] _ignoredWebResources;
    private String[] _ignoredUrlPatterns;
    private String[] _ignoredHttpMethods;

    private String _gatewayLoginUrl;
    private String _gatewayLogoutUrl;
    private GatewayServiceLocator _gsl;

    private boolean _rememberMeEnabled;

    private boolean _sendP3PHeader;

    private String _p3pHeaderValue;
    private String _splashResource;
    private String _defaultResource;

    // If this is set, after login the agent should go here and export the original request as request parameter
    private String _postAuthenticationResource;

    private boolean _disableBackTo;

    private SecurityContextPropagationConfig _securityContextPropagationConfig;

    protected transient SSOSessionManagerService sm;
    protected transient SSOIdentityManagerService im;
    protected transient SSOIdentityProviderService ip;

    /**
     * Stores the received context as part of this Partner App. configuration.
     * The context may have a starting slash : "/partnerapp" or "partnerapp".
     *
     * @param context
     */
    public SSOPartnerAppConfig(String id, String vhost, String context, String[] ignoredWebResources,
    						   String[] ignoredUrlPatterns,
    						   String[] ignoredHttpMethods,
    						   SecurityContextPropagationConfig securityContextPropagationConfig) {
        this();
        _id = id;
        _vhost = vhost;
        _context = context;
        _ignoredWebResources = ignoredWebResources;
        _ignoredUrlPatterns = ignoredUrlPatterns;
        _ignoredHttpMethods = ignoredHttpMethods;
        _securityContextPropagationConfig = securityContextPropagationConfig;
    }


    /**
     * Stores the received context as part of this Partner App. configuration.
     * The context may have a starting slash : "/partnerapp" or "partnerapp".
     *
     * @param context
     * @deprecated use constructor receiving id
     */
    public SSOPartnerAppConfig(String context, String[] ignoredWebResources, String[] ignoredUrlPatterns, String[] ignoredHttpMethods) {
        this(context, null, context, ignoredWebResources, ignoredUrlPatterns, ignoredHttpMethods, null);
    }

    /**
     * Spring friendly constructor
     */
    public SSOPartnerAppConfig() {

    }

    public boolean isRememberMeEnabled() {
        return _rememberMeEnabled;
    }

    public void setRememberMeEnabled(boolean rememberMeEnabled) {
        this._rememberMeEnabled = rememberMeEnabled;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public void setContext(String _context) {
        this._context = _context;
    }

    public void setVhost(String _vhost) {
        this._vhost = _vhost;
    }
    
    public void setSplashResource(String _splashResource){
    	this._splashResource = _splashResource;
    }

    public void setDefaultResource(String r) {
        this._defaultResource = r;
    }

    public void setPostAuthenticationResource(String r) {
        this._postAuthenticationResource = r;
    }

    /**
     * @org.apache.xbean.Property alias="ignore-web-resource-collections"
     * @return
     */                                          
    public void setIgnoredWebResources(String[] _ignoredWebResources) {
        this._ignoredWebResources = _ignoredWebResources;
    }

    /**
     * @org.apache.xbean.Property alias="ignore-url-patterns"
     * @return
     */
    public void setIgnoredUrlPatterns(String[] _ignoredUrlPatterns) {
        this._ignoredUrlPatterns = _ignoredUrlPatterns;
    }

    public void setIgnoredHttpMethods(String[] ignoredHttpMethods) {
        this._ignoredHttpMethods = ignoredHttpMethods;
    }

    public void setSecurityContextPropagationConfig(SecurityContextPropagationConfig _securityContextPropagationConfig) {
        this._securityContextPropagationConfig = _securityContextPropagationConfig;
    }

    /**
     * The configuration identifier
     */
    public String getId() {
        return _id;
    }

    /**
     * The host or virtual host associated to this partner application
     */
    public String getVhost() {
        return _vhost;
    }

    /**
     * The web context this application is belongs to.
     */
    public String getContext() {
        return _context;
    }
    
    public String getSplashResource(){
    	return _splashResource;
    }

    public String getDefaultResource() {
        return this._defaultResource;
    }

    public String getPostAuthenticationResource(){
    	return _postAuthenticationResource;
    }

    /**
     * The list of web resource names declared in the partner application deployment descriptor that should be ignored by JOSSO.
     */
    public String[] getIgnoredWebRources() {
        return _ignoredWebResources;
    }

    /**
     * The list of url patterns declared in the partner application deployment descriptor that should be ignored by JOSSO.
     */
    public String[] getIgnoredUrlPatterns() {
        return _ignoredUrlPatterns;
    }

    public String[] getIgnoredHttpMethods() {
        return _ignoredHttpMethods;
    }

    public String getGatewayLoginUrl() {
        return _gatewayLoginUrl;
    }

    public void setGatewayLoginUrl(String appLoginUrl) {
        _gatewayLoginUrl = appLoginUrl;
    }
    
    public boolean isSendP3PHeader() {
        return _sendP3PHeader;
    }

    public void setSendP3PHeader(boolean value) {
        this._sendP3PHeader = value;
    }

    /**
     * @org.apache.xbean.Property alias="P3PHeaderValue"
     */
    public String getP3PHeaderValue() {
        return _p3pHeaderValue != null ? _p3pHeaderValue : DEFAULT_P3P_HEADER_VALUE;
    }

    public void setP3PHeaderValue(String value) {
        this._p3pHeaderValue = value;
    }

    public GatewayServiceLocator getGatewayServiceLocator() {
        return _gsl;
    }

    public void setGatewayServiceLocator(GatewayServiceLocator _gsl) {
        this._gsl = _gsl;
    }

    public String getGatewayLogoutUrl() {
        return _gatewayLogoutUrl;
    }

    public void setGatewayLogoutUrl(String appLogoutUrl) {
        this._gatewayLogoutUrl = appLogoutUrl;
    }

    public boolean isDisableBackTo() {
        return _disableBackTo;
    }

    public void setDisableBackTo(boolean _disableBackTo) {
        this._disableBackTo = _disableBackTo;
    }

    /**
     *
     * @org.apache.xbean.Property alias="security-context-propagation"
     */
    public SecurityContextPropagationConfig getSecurityContextPropagationConfig() {
        return _securityContextPropagationConfig;
    }

    public SSOSessionManagerService getSessionManagerService() {
        try {
            if (_gsl == null)
                return null;

            if (sm == null)
                sm = _gsl.getSSOSessionManager();

            return sm;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SSOIdentityManagerService getIdentityManagerService() {
        try {

            if (_gsl == null)
                return null;

            if (im == null)
                im = _gsl.getSSOIdentityManager();

            return im;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SSOIdentityProviderService getIdentityProviderService() {
        try {

            if (_gsl == null)
                return null;

            if (ip == null)
                ip = _gsl.getSSOIdentityProvider();

            return ip;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        String r = "";
        String r2 = "";
        String r3 = "";
        for (int i = 0; i < _ignoredWebResources.length; i++) {
            r += _ignoredWebResources[i] + ",";
        }
        for (int i = 0; i < _ignoredUrlPatterns.length; i++) {
            r2 += _ignoredUrlPatterns[i] + ",";
        }
        for (int i = 0; i < _ignoredHttpMethods.length; i++) {
            r3 += _ignoredHttpMethods[i] + ",";
        }
        return _id + ":" + (_vhost != null ? _vhost : "") + _context +
                (_ignoredWebResources.length > 0 ? " [" + r + "]" : "") +
        		(_ignoredUrlPatterns.length > 0 ? " [" + r2 + "]" : "") +
                (_ignoredHttpMethods.length > 0 ? " [" + r3 + "]" : "") +
                (_securityContextPropagationConfig != null ? " [" + _securityContextPropagationConfig + "]" : ""
                );
    }

}
