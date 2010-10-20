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

    private boolean _rememberMeEnabled;

    private boolean _sendP3PHeader;

    private String _p3pHeaderValue;
    
    private String _splashResource;

    private String _defaultResource;

    // If this is set, after login the agent should go here and export the original request as request parameter
    private String _postAuthenticationResource;

    private SecurityContextPropagationConfig _securityContextPropagationConfig;

    /**
     * Stores the received context as part of this Partner App. configuration.
     * The context may have a starting slash : "/partnerapp" or "partnerapp".
     *
     * @param context
     */
    public SSOPartnerAppConfig(String id, String vhost, String context, String[] ignoredWebResources,
    						   String[] ignoredUrlPatterns, 
    						   SecurityContextPropagationConfig securityContextPropagationConfig) {
        this();
        _id = id;
        _vhost = vhost;
        _context = context;
        _ignoredWebResources = ignoredWebResources;
        _ignoredUrlPatterns = ignoredUrlPatterns;
        _securityContextPropagationConfig = securityContextPropagationConfig;
    }


    /**
     * Stores the received context as part of this Partner App. configuration.
     * The context may have a starting slash : "/partnerapp" or "partnerapp".
     *
     * @param context
     * @deprecated use constructor receiving id
     */
    public SSOPartnerAppConfig(String context, String[] ignoredWebResources, String[] ignoredUrlPatterns) {
        this(context, null, context, ignoredWebResources, ignoredUrlPatterns, null);
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

    /**
     *
     * @org.apache.xbean.Property alias="security-context-propagation"
     */
    public SecurityContextPropagationConfig getSecurityContextPropagationConfig() {
        return _securityContextPropagationConfig;
    }

    public String toString() {
        String r = "";
        String r2 = "";
        for (int i = 0; i < _ignoredWebResources.length; i++) {
            r += _ignoredWebResources[i] + ",";
        }
        for (int i = 0; i < _ignoredUrlPatterns.length; i++) {
            r2 += _ignoredUrlPatterns[i] + ",";
        }
        return _id + ":" + (_vhost != null ? _vhost : "") + _context + (_ignoredWebResources.length > 0 ? " [" + r + "]" : "") +
        		(_ignoredUrlPatterns.length > 0 ? " [" + r2 + "]" : "") + 
                (_securityContextPropagationConfig != null ? " [" + _securityContextPropagationConfig + "]" : ""
                );
    }
}
