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

package org.josso.agent.http;

import org.josso.agent.AbstractSSOAgent;
import org.josso.agent.SSOAgentRequest;
import org.josso.agent.SSOPartnerAppConfig;
import org.josso.agent.Constants;
import org.josso.auth.util.CipherUtil;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.SSOIdentityException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import javax.servlet.http.Cookie;
import java.security.Principal;
import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Rev: 608 $ $Date: 2008-08-21 12:35:13 -0300 (Thu, 21 Aug 2008) $
 */
public abstract class HttpSSOAgent extends AbstractSSOAgent {

    private static final String JOSSO_LOGIN_URI = "/josso_login/";

    private static final String JOSSO_USER_LOGIN_URI = "/josso_user_login/";
    
    private static final String JOSSO_SECURITY_CHECK_URI = "/josso_security_check";

    private static final String JOSSO_LOGOUT_URI = "/josso_logout/";

    private static final String JOSSO_AUTHENTICATION_URI = "/josso_authentication/";

    private List _builders = new ArrayList();

    private List _automaticStrategies = new ArrayList();
    
    public void start() {
        super.start();
        
        // Automatically register default strategy
        if (_automaticStrategies.isEmpty()) {
            _automaticStrategies.add(new DefaultAutomaticLoginStrategy(org.josso.agent.Constants.JOSSO_AUTH_LOGIN_SUFFICIENT));
        }

        for (int i=0; i<_automaticStrategies.size(); i++) {
        	AutomaticLoginStrategy as = (AutomaticLoginStrategy) _automaticStrategies.get(i);
            if (as instanceof AbstractAutomaticLoginStrategy) {
                ((AbstractAutomaticLoginStrategy)as).setAgent(this);
            }
        }
    }

    /**
     * By default we do not require to authenticate all requests.
     */
    protected boolean isAuthenticationAlwaysRequired() {
        return false;
    }

    protected void propagateSecurityContext(SSOAgentRequest request, Principal principal) {
        HttpSSOAgentRequest servletSSOAgentRequest = (HttpSSOAgentRequest) request;
        SSOPartnerAppConfig partnerAppConfig;

        String contextPath = servletSSOAgentRequest.getRequest().getContextPath();

        // In catalina, the empty context is considered the root context
        if ("".equals(contextPath))
            contextPath = "/";

        partnerAppConfig = getPartnerAppConfig(servletSSOAgentRequest.getRequest().getServerName(),
                contextPath
        );

        if (partnerAppConfig.getSecurityContextPropagationConfig() == null) {
            // No security propagation configuration found, ignore this.
            return;
        }

        String binding = partnerAppConfig.getSecurityContextPropagationConfig().getBinding();
        String userPlaceHolder = partnerAppConfig.getSecurityContextPropagationConfig().getUserPlaceHolder();
        String rolesPlaceHolder = partnerAppConfig.getSecurityContextPropagationConfig().getRolesPlaceHolder();
        String propertiesPlaceholder = partnerAppConfig.getSecurityContextPropagationConfig().getPropertiesPlaceHolder();
        String user = principal.getName();

        if (binding != null && userPlaceHolder != null && rolesPlaceHolder != null) {
            SSORole[] roleSets;

            try {
                roleSets = im.findRolesBySSOSessionId(request.getRequester(),  servletSSOAgentRequest.getSessionId());
            } catch (SSOIdentityException e) {

                log("Error fetching roles for SSO Session [" + servletSSOAgentRequest.getSessionId() + "]" +
                        " on attempting to propagate security context, aborting");

                return;
            }

            HttpServletRequest hreq = servletSSOAgentRequest.getRequest();

            if (binding.equalsIgnoreCase("HTTP_HEADERS")) {

                HashMap headers = new HashMap();
                List users = new ArrayList();
                users.add(user);
                headers.put(userPlaceHolder, users);

                if (debug > 0)
                    log("Propagated user [" + user + "] onto HTTP Header [" + userPlaceHolder + "]");

                List roles = new ArrayList();
                for (int i = 0; i < roleSets.length; i++) {
                    SSORole roleSet = roleSets[i];

                    roles.add(roleSet.getName());

                    if (debug > 0)
                        log("Propagated role [" + roleSet.getName() + "] onto HTTP_HEADERS based security context");
                }
                headers.put(rolesPlaceHolder, roles);

                hreq.setAttribute(SecurityContextExporterFilter.SECURITY_CONTEXT_BINDING,
                        SecurityContextExporterFilter.HTTP_HEADERS_BINDING);

                hreq.setAttribute(SecurityContextExporterFilter.SECURITY_CONTEXT_CONTENT, headers);

            } else if (binding.equalsIgnoreCase("HREQ_ATTRS")) {

                HashMap attrs = new HashMap();
                attrs.put(userPlaceHolder, user);

                for (int i = 0; i < roleSets.length; i++) {
                    SSORole roleSet = roleSets[i];
                    attrs.put(rolesPlaceHolder + "_" + i, roleSet.getName());
                    if (debug > 0)
                        log("Propagated role [" + roleSet.getName() + "] onto HREQ_ATTRS based security context");
                }

                SSOUser usr = (SSOUser) principal;
                if (usr.getProperties() != null) {
                    Properties props = new Properties();
                    for (int i = 0; i < usr.getProperties().length; i++) {
                        attrs.put(propertiesPlaceholder + "_" + usr.getProperties()[i].getName(),
                                usr.getProperties()[i].getValue());
                        if (debug > 0)
                            log("Propagated role [" + usr.getProperties()[i].getName() + "=" +
                                    usr.getProperties()[i].getValue() + "] onto HREQ_ATTRS based security context");
                    }
                }

                hreq.setAttribute(SecurityContextExporterFilter.SECURITY_CONTEXT_CONTENT, attrs);

                hreq.setAttribute(SecurityContextExporterFilter.SECURITY_CONTEXT_BINDING,
                        SecurityContextExporterFilter.HTTP_REQ_ATTRS_BINDING);

            }
        }
    }

    /**
     * @deprecated
     * @param hreq
     * @return
     */
    public boolean isAutomaticLoginRequired(HttpServletRequest hreq) {
        return isAutomaticLoginRequired(hreq, null);
    }
    
    /**
     *      1) Required     - The LoginModule is required to succeed.
     *			If it succeeds or fails, authentication still continues
     *			to proceed down the LoginModule list.
     *
     *      3) Sufficient   - The LoginModule is not required to
     *			succeed.  If it does succeed, control immediately
     *			returns to the application (authentication does not
     *			proceed down the LoginModule list).
     *			If it fails, authentication continues down the
     *			LoginModule list.
     *
     *      4) Optional     - The LoginModule is not required to
     *			succeed.  If it succeeds or fails,
     *			authentication still continues to proceed down the
     *			LoginModule list.
     */
    public boolean isAutomaticLoginRequired(HttpServletRequest hreq, HttpServletResponse hres) {

        // If any required module returns false, this will be false
        Boolean requiredFlag = null;

        // If any sufficient module returns true, this will be true
        Boolean sufficientFlag = null;

        for (int i=0; i<_automaticStrategies.size(); i++) {
        	
        	AutomaticLoginStrategy as = (AutomaticLoginStrategy) _automaticStrategies.get(i);

            if (as.getMode().equals(org.josso.agent.Constants.JOSSO_AUTH_LOGIN_SUFFICIENT)) {

                if (as.isAutomaticLoginRequired(hreq, hres)) {
                    sufficientFlag = Boolean.TRUE;
                    break; // Stop evaluation
                }
            }

            if (as.getMode().equals(org.josso.agent.Constants.JOSSO_AUTH_LOGIN_REQUIRED)) {

                if (!as.isAutomaticLoginRequired(hreq, hres)) {
                    requiredFlag = Boolean.FALSE;
                } else if (requiredFlag == null) {
                    requiredFlag = Boolean.TRUE;
                }
            }

            // This does not affect the outcome of the evaluation
            if (as.getMode().equals(org.josso.agent.Constants.JOSSO_AUTH_LOGIN_OPTIONAL)) {
                as.isAutomaticLoginRequired(hreq, hres);
            }

        }
       
        // If any required module returned a value, use it.
        if (requiredFlag != null) {
            return requiredFlag.booleanValue();
        }

        // If any sufficient modules returned a value, use it; otherwise return false.
        return sufficientFlag != null && sufficientFlag.booleanValue();

    }

    /**
     * @deprecated
     * @param hreq
     */
    public void clearAutomaticLoginReferer(HttpServletRequest hreq) {
        clearAutomaticLoginReferer(hreq, null);
    }
    
    public void clearAutomaticLoginReferer(HttpServletRequest hreq, HttpServletResponse hres) {
    	removeAttribute(hreq, hres, "JOSSO_AUTOMATIC_LOGIN_REFERER");
    }

    /**
     * This method builds a logout URL based on a HttpServletRequest.  The url contains all necessary parameters
     * required by the front-channel part of the SSO protocol.
     *
     * @return
     * @deprecated
     */
    public String buildLogoutUrl(HttpServletRequest hreq) {
        return buildLogoutUrl(hreq, "/");
    }

    /**
     * This method builds a logout URL based on a HttpServletRequest.  The url contains all necessary parameters
     * required by the front-channel part of the SSO protocol.
     *
     * @return
     */
    public String buildLogoutUrl(HttpServletRequest hreq, SSOPartnerAppConfig cfg) {
        return buildLogoutUrl(hreq, cfg.getDefaultResource() != null ? cfg.getDefaultResource() : "/");
    }


    /**
     * This method builds a logout URL based on a HttpServletRequest.  The url contains all necessary parameters
     * required by the front-channel part of the SSO protocol.
     *
     * @return
     */
    public String buildLogoutUrl(HttpServletRequest hreq, String backToPath) {

        String backto = buildBackToURL(hreq, backToPath);

        String logoutUrl = getGatewayLogoutUrl() + (backto != null ? "?josso_back_to=" + backto : "");

        logoutUrl += buildLogoutUrlParams(hreq);

        return logoutUrl;
    }

    /**
     * This method builds a login URL based on a HttpServletRequest.  The url contains all necessary parameters
     * required by the front-channel part of the SSO protocol.
     */
    public String buildLoginUrl(HttpServletRequest hreq) {
        String loginUrl = getGatewayLoginUrl();

        String backto = buildBackToURL(hreq, getJOSSOSecurityCheckUri());

        loginUrl = loginUrl + "?josso_back_to=" + backto;

        // Add login URL parameters
        loginUrl += buildLoginUrlParams(hreq);

        return loginUrl;
    }

    /**
     * This method builds a login URL based on a HttpServletRequest.  The url contains all necessary parameters
     * required by the front-channel part of the SSO protocol.
     */
    public String buildLoginOptionalUrl(HttpServletRequest hreq) {
        String loginUrl = getGatewayLoginUrl();

        String backto = buildBackToURL(hreq, getJOSSOSecurityCheckUri());

        loginUrl = loginUrl + "?josso_cmd=login_optional&josso_back_to=" + backto;

        // Add login URL parameters
        loginUrl += buildLoginUrlParams(hreq);

        return loginUrl;
    }


    /**
     * This method builds the back_to URL value pointing to the given URI.
     * <p/>
     * The determines the host used to build the back_to URL in the following order :
     * <p/>
     * First, checks the singlePointOfAccess agent's configuration property.
     * Then checks the reverse-proxy-host HTTP header value from the request.
     * Finally uses current host name.
     */
    public String buildBackToURL(HttpServletRequest hreq, String uri) {
        String backto = null;

        // Build the back to url.
        String contextPath = hreq.getContextPath();

        // This is the root context
        if (contextPath == null || "".equals(contextPath))
            contextPath = "/";

        String reverseProxyHost = hreq.getHeader(org.josso.gateway.Constants.JOSSO_REVERSE_PROXY_HEADER);
        String singlePointOfAccess = getSinglePointOfAccess();

        if (singlePointOfAccess != null) {
            // Using single-point of access configuration.
            if (debug >= 1)
                log("josso_back_to option : singlePointOfAccess: " + singlePointOfAccess);
            backto = singlePointOfAccess + contextPath + uri;

        } else if (reverseProxyHost != null) {
            // Using reverse proxy host header.
            if (debug >= 1)
                log("josso_back_to option : reverse-proxy-host: " + reverseProxyHost);
            backto = reverseProxyHost + contextPath + uri;

        } else {
            // Using default host
            StringBuffer mySelf = HttpUtils.getRequestURL(hreq);

            try {
                java.net.URL url = new java.net.URL(mySelf.toString());
                backto = url.getProtocol() + "://" + url.getHost() + ((url.getPort() > 0) ? ":" + url.getPort() : "");
            } catch (java.net.MalformedURLException e) {
                throw new RuntimeException(e);
            }

            backto += (contextPath.endsWith("/") ? contextPath.substring(0, contextPath.length() - 1) : contextPath) + uri;

        }

        if (debug >= 1)
            log("Using josso_back_to : " + backto);

        return backto;
    }

    public String buildPostAuthUrl(HttpServletResponse hres, String requestURI, String postAuthURI) {
        // TODO : Is there a better way to do this ?
        String encURL = requestURI.replaceAll("&", "%26").replaceAll("\\?", "%3F");

        return hres.encodeRedirectURL(postAuthURI +  "?josso_original_resource=" + hres.encodeURL(encURL));

    }

    /**
     * This creates a new JOSSO Cookie for the given path and value.
     *
     * @param path  the path associated with the cookie, normaly the partner application context.
     * @param value the SSO Session ID
     * @return
     */
    public Cookie newJossoCookie(String path, String value, boolean secure) {

        // Some browsers don't like cookies without paths. This is useful for partner applications configured in the root context
        if (path == null || "".equals(path))
            path = "/";

        Cookie ssoCookie = new Cookie(org.josso.gateway.Constants.JOSSO_SINGLE_SIGN_ON_COOKIE, value);
        ssoCookie.setMaxAge(-1);
        ssoCookie.setPath(path);
        ssoCookie.setSecure(secure);

        // TODO : Check domain / secure ?
        //ssoCookie.setDomain(cfg.getSessionTokenScope());
        //ssoCookie.setSecure(true);

        return ssoCookie;
    }

    public String buildAutomaticSubmitForm(HttpServletRequest request) {
        //TODO - remove permanently
        return null;
    }


    /**
     * This method builds request URL parameters that will be sent to the gateway when attempting logins and identity assertions
     * trhough the front channel (HTTP)
     *
     * @param hreq
     * @return
     */
    protected String buildLoginUrlParams(HttpServletRequest hreq) {

        String urlParams = "";

        for (int i = 0; i < _builders.size(); i++) {
            FrontChannelParametersBuilder builder = (FrontChannelParametersBuilder) _builders.get(i);
            SSONameValuePair[] params = builder.buildParamters(hreq);

            for (int j = 0; j < params.length; j++) {
                SSONameValuePair param = params[j];
                urlParams += "&" + param.getName() + "=" + param.getValue();
            }
        }

        return urlParams;

    }

    /**
     * This method builds request URL parameters that will be sent to the gateway when attempting logins and identity assertions
     * trhough the front channel (HTTP)
     *
     * @param hreq
     * @return
     */
    protected String buildLogoutUrlParams(HttpServletRequest hreq) {

        String urlParams = "";
        for (int i = 0; i < _builders.size(); i++) {
            FrontChannelParametersBuilder builder = (FrontChannelParametersBuilder) _builders.get(i);
            SSONameValuePair[] params = builder.buildParamters(hreq);
            for (int j = 0; j < params.length; j++) {
                SSONameValuePair param = params[j];
                urlParams += "&" + param.getName() + "=" + param.getValue();
            }
        }

        return urlParams;

    }

    protected void sendCustomAuthentication(SSOAgentRequest request) throws IOException {
        HttpServletRequest hreq = ((HttpSSOAgentRequest) request).getRequest();
        HttpServletResponse hres = ((HttpSSOAgentRequest) request).getResponse();
        prepareNonCacheResponse(hres);

        SSOPartnerAppConfig cfg = this.getPartnerAppConfig(hreq.getServerName(), hreq.getContextPath());

        String splash_resource = null;
        /* If this is an authentication request, our splash resource will be one of the following (in the given order):
         * 1. submitted josso_splash_resource parameter
         * 2. default splash resource, defined in josso-agent-config
         * TODO : Referer values should be handled by agent when processing LOGIN_REQUESTS (josso_login) 3. value from referrer header
         * 
         * If this is not authentication request, splash resource will be request URI
         */
        if (hreq.getRequestURI().endsWith(this.getJOSSOAuthenticationUri())) {

            if (debug > 0)
                log("sendCustomAuthentication executed, URL does match AUTHENTICATION URI");

            //try josso_splash_resource defined as hidden field
            splash_resource = hreq.getParameter(Constants.JOSSO_SPLASH_RESOURCE_PARAMETER);

            if (splash_resource == null || "".equals(splash_resource)) {
                if (cfg != null) {
                    splash_resource = cfg.getSplashResource();
                }
                /* TODO :Verify this! Agents should store referer values as SAVED_REQUESTS when
                processing a login or automatic request
        		if(splash_resource == null || "".equals(splash_resource)){
        			//fall back to referer
        			splash_resource = hreq.getHeader("referer");
        		}
                 */
            }
        } else {

            if (debug > 0)
                log("sendCustomAuthentication executed but URL does not match AUTHENTICATION URI");

            // TODO : Verify this! We should never get here ..

            StringBuffer sb = new StringBuffer(hreq.getRequestURI());
            if (hreq.getQueryString() != null) {
                sb.append('?');
                sb.append(hreq.getQueryString());
            }
            splash_resource = sb.toString();
        }

        if (debug > 0)
            log("Storing Splash resource '" + splash_resource + "'");

        setAttribute(hreq, hres, Constants.JOSSO_SPLASH_RESOURCE_PARAMETER, splash_resource);

        // Create Automatic post form
        StringBuffer sb = new StringBuffer(256);

        // TODO : Use a template instead ?
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n" +
                "\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n"
                + "<body onload=\"document.forms[0].submit()\">\n" +
                "<noscript>\n" + "<p>\n" + "<strong>Note:</strong> Since your browser does not support JavaScript,\n" +
                "you must press the Continue button once to proceed.\n" + "</p>\n" + "</noscript>\n" +
                "<form action=\"").append(getGatewayLoginUrl()).
                append("\" method=\"post\" name=\"usernamePasswordLoginForm\" enctype=\"application/x-www-form-urlencoded\">\n"
                        + "        <div>");

        //copy all submitted parameters into hidden fields
        Enumeration paramNames = hreq.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String paramValue = hreq.getParameter(paramName);
            if (!Constants.JOSSO_SPLASH_RESOURCE_PARAMETER.equals(paramName)) {
                sb.append("\n            <input type=\"hidden\" value=\"").append(paramValue).append("\" name=\"").append(paramName).append("\" />");
            }
        }

//        sb.append("\n            <input type=\"hidden\" name=\"josso_back_to\"value=\"").append(buildBackToURL(hreq, getJOSSOSecurityCheckUri())).append("\"/>\n").
                sb.append("\n            <noscript><input type=\"submit\" value=\"Continue\"/></noscript>\n" +
                        "        </div>\n" +
                        "</form>\n" +
                        "</body>\n" +
                        "</html>");


        hres.setContentType("text/html");
        PrintWriter out = hres.getWriter();
        out.print(sb.toString());

        if (debug >= 1) {
            log("Sending an automatic post form : \n" + sb.toString());
        }

        out.flush();
    }

    /**
     * Sets non cache headers in HttpServletResponse
     *
     * @param response
     */
    public void prepareNonCacheResponse(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    /**
     * Checks if the requested resource is subject to SSO protection 
     * (it compares the request path against configured <ignore-url-patterns>).
     * 
     * @param cfg partner application configuration
     * @param request http request
     * @return true if requested resource should be ignored, false otherwise
     */
    public boolean isResourceIgnored(SSOPartnerAppConfig cfg, HttpServletRequest request) {
        // There are some url-patterns to ignore
        String[] ignoredUrlPatterns = cfg.getIgnoredUrlPatterns();

        if (debug >= 1)
            log("Found [" +  (ignoredUrlPatterns!= null ? ignoredUrlPatterns.length+"" : "no") + "] ignored url patterns ");

        if (ignoredUrlPatterns != null && ignoredUrlPatterns.length > 0) {
            
        	String requestPath = request.getServletPath();
        	
            // Add the path info, if there is any
            String pathInfo = request.getPathInfo();
            if (pathInfo != null) {
                requestPath += pathInfo;
            }
            
        	for (int i=0; i<ignoredUrlPatterns.length; i++) {
	        	String ignoredUrlPattern = ignoredUrlPatterns[i];
	        	
	        	if (matchPattern(requestPath, ignoredUrlPattern)) {
	        		
	        		// We should ignore this URI, it's not subject to SSO protection
	        		if (debug >= 1)
	                    log("Not subject to SSO protection :  url-pattern:" + ignoredUrlPattern);
	                
	        		return true;
	        	}
        	}
        }

        return false;
    }
    
    /**
     * Does the specified request path match the specified URL pattern?
     * This method follows the same rules (in the same order) as those used
     * for mapping requests to servlets.
     *
     * @param path Context-relative request path to be checked
     *  (must start with '/')
     * @param pattern URL pattern to be compared against
     */
    protected boolean matchPattern(String path, String pattern) {
        // Normalize the argument strings
        if ((path == null) || (path.length() == 0))
            path = "/";
        if ((pattern == null) || (pattern.length() == 0))
            pattern = "/";

        // Check for exact match
        if (path.equals(pattern))
            return (true);

        // Check for path prefix matching
        if (pattern.startsWith("/") && pattern.endsWith("/*")) {
            pattern = pattern.substring(0, pattern.length() - 2);
            if (pattern.length() == 0)
                return (true);  // "/*" is the same as "/"
            if (path.endsWith("/"))
                path = path.substring(0, path.length() - 1);
            while (true) {
                if (pattern.equals(path))
                    return (true);
                int slash = path.lastIndexOf('/');
                if (slash <= 0)
                    break;
                path = path.substring(0, slash);
            }
            return (false);
        }

        // Check for suffix matching
        if (pattern.startsWith("*.")) {
            int slash = path.lastIndexOf('/');
            int period = path.lastIndexOf('.');
            if ((slash >= 0) && (period > slash) &&
                path.endsWith(pattern.substring(1))) {
                return (true);
            }
            return (false);
        }

        // Check for universal mapping
        if (pattern.equals("/"))
            return (true);

        return (false);
    }

    public String getJOSSOLoginUri() {
        return JOSSO_LOGIN_URI;
    }

    public String getJOSSOUserLoginUri() {
        return JOSSO_USER_LOGIN_URI;
    }
    
    public String getJOSSOSecurityCheckUri() {
        return JOSSO_SECURITY_CHECK_URI;
    }

    public String getJOSSOLogoutUri() {
        return JOSSO_LOGOUT_URI;
    }

    public String getJOSSOAuthenticationUri() {
        return JOSSO_AUTHENTICATION_URI;
    }


    // --------------------------- Spring friendly

    public void setParametersBuilders(List builders) {
        this._builders = builders;
    }

    public List getParametersBuilders() {
        return _builders;
    }
    
    /**
     * Sets attribute as a cookie (if stateOnClient enabled) 
     * or in the http session.
     * Value is base64 encoded.
     * 
     * @param hreq http request
     * @param hres http response
     * @param name attribute name
     * @param value attribute value
     */
    public void setAttribute(HttpServletRequest hreq,
                                  HttpServletResponse hres,
                                  String name,
                                  String value) {

        if (isStateOnClient()) {

            Set removed = (Set) hreq.getAttribute("org.josso.attrs.removed");
            if (removed == null)
                removed = new HashSet();

            if (removed.contains(name))
                removed.remove(name);

            log("Storing attribute "  + name + "=" + value + " client side");
            if (hres == null)
                throw new IllegalArgumentException("HTTP Servlet response cannot be null.  Are you using any deprecated operations?");
            
            String cookieValue = null;
            try {
            	// TODO: upgrade to commons-codec 1.4 and use URL-safe mode?
            	cookieValue = CipherUtil.encodeBase64(value.getBytes());
            	cookieValue = URLEncoder.encode(cookieValue, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			    log("Base64 encoding failed : " + value, e);
			    cookieValue = value;
	        }
            
            // Store value as session cookie
            Cookie cookie = new Cookie(name, cookieValue);
            cookie.setPath(hreq.getContextPath());
            cookie.setMaxAge(-1);

            hres.addCookie(cookie);

            // Local copy
            hreq.setAttribute(name, value);

        } else {

            log("Storing attribute "  + name + "=" + value + " server side");
            // Use HTTP Session ( TODO : Use LocalSession instead ? )
            hreq.getSession().setAttribute(name, value);
        }

    }

    /**
     * Retrieves attribute value from the cookie (if stateOnClient enabled) 
     * or from the http session.
     * 
     * @param hreq http request
     * @param name attribute name
     * @return attribute value
     */
    public String getAttribute(HttpServletRequest hreq, String name) {
        if (isStateOnClient()) {

            Set removed = (Set) hreq.getAttribute("org.josso.attrs.removed");
            if (removed == null)
                removed = new HashSet();

            if (removed.contains(name))
                return null;

            // If a local value is present, use it.
            String vlocal = (String) hreq.getAttribute(name);
            if (vlocal != null && !"".equals(vlocal))
                return vlocal;

            // Use a cookie value, if present
            Cookie[] cookies = hreq.getCookies();
            if (cookies != null) {
            	for (int i=0; i<cookies.length; i++) {
            		Cookie cookie = cookies[i];
                    if (cookie.getName().equals(name)) {
                        String cookieValue = cookie.getValue();
                        String value = null;
                        try {
                        	// TODO: upgrade to commons-codec 1.4 and use URL-safe mode?
                        	cookieValue = URLDecoder.decode(cookieValue, "UTF-8");
							value = new String(CipherUtil.decodeBase64(cookieValue));
						} catch (UnsupportedEncodingException e) {
							log("Base64 decoding failed : " + cookieValue, e);
							value = cookieValue;
						}
                        if (value == null || value.equals("-") || value.equals(""))
                            return null;
                        return value;
                    }
                }
            }

            return null;
        } else {
            // Use HTTP Session ( TODO : Use LocalSession instead ? )
            return (String) hreq.getSession().getAttribute(name);
        }

    }

    /**
     * Removes attribute with the given name 
     * (removes the cookie if stateOnClient enabled, 
     * otherwise it removes it from the http session).
     * 
     * @param hreq http request
     * @param hres http response
     * @param name attribute name
     */
    public void removeAttribute(HttpServletRequest hreq, HttpServletResponse hres, String name) {
        if (isStateOnClient()) {

            Set removed = (Set) hreq.getAttribute("org.josso.attrs.removed");
            if (removed == null)
                removed = new HashSet();

            log("Remove attribute "  + name + " from client side");
            // Use a cookie

            // Store value as session cookie
            Cookie cookie = new Cookie(name, (String) "-");
            cookie.setPath(hreq.getContextPath());
            cookie.setMaxAge(0);

            hres.addCookie(cookie);

            // Mark this as removed
            removed.add(name);

            // Local copy
            hreq.removeAttribute(name);

        } else {

            log("Remove attribute "  + name + " from server side");
            // Use HTTP Session ( TODO : Use LocalSession instead ? )
            hreq.getSession().removeAttribute(name);
        }

    }
    
    public List getAutomaticLoginStrategies() {
        return _automaticStrategies;
    }

    public void setAutomaticLoginStrategies(List _automaticStrategies) {
        this._automaticStrategies = _automaticStrategies;
    }
}
