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

package org.josso.servlet.agent;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.Constants;
import org.josso.agent.LocalSession;
import org.josso.agent.Lookup;
import org.josso.agent.SSOAgentRequest;
import org.josso.agent.SSOPartnerAppConfig;
import org.josso.agent.SingleSignOnEntry;
import org.josso.agent.http.HttpSSOAgent;
import org.josso.agent.http.WebAccessControlUtil;

/**
 * JOSSO Servlet Filter for Generic SSO Agent, this replaces the Valve in tomcat or other container specific components.
 * The fillter will handle web logic to authenticate, login and logout users.
 *
 * Date: Nov 27, 2007
 * Time: 9:28:53 AM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class GenericServletSSOAgentFilter implements Filter {

    public static final String KEY_SESSION_MAP = "org.josso.servlet.agent.sessionMap";
    public static final String LAZY_STARTUP ="lazy";
    /**
     * The servlet context
     */
    protected ServletContext context;

    /**
     * One agent instance for all applications.
     */
    protected HttpSSOAgent agent;

    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(GenericServletSSOAgentFilter.class);

    public GenericServletSSOAgentFilter() {

    }

    protected void startup() throws ServletException {

        try {

            Lookup lookup = Lookup.getInstance();
            lookup.init("josso-agent-config.xml"); // For spring compatibility ...

            // We need at least an abstract SSO Agent
            agent = (HttpSSOAgent) lookup.lookupSSOAgent();
            if (log.isDebugEnabled())
            agent.setDebug(1);
            agent.start();


            // Publish agent in servlet context
            context.setAttribute("org.josso.agent", agent);

        } catch (Exception e) {
            throw new ServletException("Error starting SSO Agent : " + e.getMessage(), e);
        }

    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // Validate and update our current component state
        context = filterConfig.getServletContext();
        context.setAttribute(KEY_SESSION_MAP, new HashMap());

        // Lazy startup shifts filter initialization upon the first request is received
        // This allows the container to setup web application's classloader
        // In some containers - such as JRun - a filter will not be able to access web application's resources
        // (e.g. WEB-INF/classes) during initialization due to that this is not fully initialized yet.
        if (agent == null && !filterConfig.getInitParameter("init").equals(LAZY_STARTUP)) {
            startup();
        }


    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        if (agent == null) {
            startup();
        }

        HttpServletRequest hreq =
                (HttpServletRequest) request;
        HttpServletResponse hres =
                (HttpServletResponse) response;


        if (log.isDebugEnabled())
            log.debug("Processing : " + hreq.getContextPath());

        try {
            // ------------------------------------------------------------------
            // Check with the agent if this context should be processed.
            // ------------------------------------------------------------------
            String contextPath = hreq.getContextPath();
            String vhost = hreq.getServerName();

            // Take the node from the request first and store it if found.
            String nodeId = hreq.getParameter("josso_node");
            if (nodeId != null) {
                if (log.isDebugEnabled())
                    log.debug("Storing JOSSO Node id : " + nodeId);
                agent.setAttribute(hreq, hres, "JOSSO_NODE",  nodeId);
            } else {
                nodeId = agent.getAttribute(hreq, "JOSSO_NODE");
                if (log.isDebugEnabled())
                    log.debug("Found JOSSO Node id : " + nodeId);
            }

            // In catalina, the empty context is considered the root context
            if ("".equals(contextPath))
                contextPath = "/";

            if (!agent.isPartnerApp(vhost, contextPath)) {
                filterChain.doFilter(hreq, hres);
                if (log.isDebugEnabled())
                    log.debug("Context is not a josso partner app : " + hreq.getContextPath());

                return;
            }

            // ------------------------------------------------------------------
            // Check some basic HTTP handling
            // ------------------------------------------------------------------
            // P3P Header for IE 6+ compatibility when embedding JOSSO in a IFRAME
            SSOPartnerAppConfig cfg = agent.getPartnerAppConfig(vhost, contextPath);
            if (cfg.isSendP3PHeader() && !hres.isCommitted()) {
                hres.setHeader("P3P", cfg.getP3PHeaderValue());
            }

            // Get our session ...
            HttpSession session = hreq.getSession(true);
            
            // ------------------------------------------------------------------
            // Check if the partner application required the login form
            // ------------------------------------------------------------------
            if (log.isDebugEnabled())
                log.debug("Checking if its a josso_login_request for '" + hreq.getRequestURI() + "'");

            if (hreq.getRequestURI().endsWith(agent.getJossoLoginUri()) ||
            		hreq.getRequestURI().endsWith(agent.getJossoUserLoginUri())) {

                if (log.isDebugEnabled())
                    log.debug("josso_login_request received for uri '" + hreq.getRequestURI() + "'");

                //save referer url in case the user clicked on Login from some public resource (page)
                //so agent can redirect the user back to that page after successful login
                if (hreq.getRequestURI().endsWith(agent.getJossoUserLoginUri())) {
                	saveLoginBackToURL(hreq, hres, session, true);
                } else {
                	saveLoginBackToURL(hreq, hres, session, false);
                }
                
                String loginUrl = agent.buildLoginUrl(hreq);

                if (log.isDebugEnabled())
                    log.debug("Redirecting to login url '" + loginUrl + "'");

                //set non cache headers
                agent.prepareNonCacheResponse(hres);
                hres.sendRedirect(hres.encodeRedirectURL(loginUrl));

                return;

            }

            // ------------------------------------------------------------------
            // Check if the partner application required a logout
            // ------------------------------------------------------------------
            if (log.isDebugEnabled())
                log.debug("Checking if its a josso_logout request for '" + hreq.getRequestURI() + "'");

            if (hreq.getRequestURI().endsWith(agent.getJossoLogoutUri())) {

                if (log.isDebugEnabled())
                    log.debug("josso_logout request received for uri '" + hreq.getRequestURI() + "'");

                String logoutUrl = agent.buildLogoutUrl(hreq, cfg);

                if (log.isDebugEnabled())
                    log.debug("Redirecting to logout url '" + logoutUrl + "'");

                // Clear previous COOKIE ...
                Cookie ssoCookie = agent.newJossoCookie(hreq.getContextPath(), "-", hreq.isSecure());
                hres.addCookie(ssoCookie);
                
                // invalidate session (unbind josso security context)
                session.invalidate();
                
                //set non cache headers
                agent.prepareNonCacheResponse(hres);
                hres.sendRedirect(hres.encodeRedirectURL(logoutUrl));

                return;

            }

            // ------------------------------------------------------------------
            // Check for the single sign on cookie
            // ------------------------------------------------------------------
            if (log.isDebugEnabled())
                log.debug("Checking for SSO cookie");
            Cookie cookie = null;
            Cookie cookies[] = hreq.getCookies();
            if (cookies == null)
                cookies = new Cookie[0];
            for (int i = 0; i < cookies.length; i++) {
                if (org.josso.gateway.Constants.JOSSO_SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
                    cookie = cookies[i];
                    break;
                }
            }
            
            String jossoSessionId = (cookie == null) ? null : cookie.getValue();
            if (log.isDebugEnabled())
                log.debug("Session is:" + session);


            // Get session map for this servlet context.
            Map sessionMap = (Map) hreq.getSession().getServletContext().getAttribute(KEY_SESSION_MAP);
            if (sessionMap == null) {
                synchronized (this) {
                    sessionMap = (Map) hreq.getSession().getServletContext().getAttribute(KEY_SESSION_MAP);
                    if (sessionMap == null) {
                        sessionMap = Collections.synchronizedMap(new HashMap());
                        hreq.getSession().getServletContext().setAttribute(KEY_SESSION_MAP, sessionMap);
                    }
                }
            }
            GenericServletLocalSession localSession = (GenericServletLocalSession) sessionMap.get(session.getId());
            if (sessionMap.get(session.getId()) == null) {
                localSession = new GenericServletLocalSession(session);
                // the local session is new so, make the valve listen for its events so that it can
                // map them to local session events.
                // Not supported : session.addSessionListener(this);
                sessionMap.put(session.getId(), localSession);
            } else {
                // Update the session, just in case we have an older copy wrapped on our local session.
                localSession.updateSession(session);
            }

            
            // ------------------------------------------------------------------
            // Check if the partner application submitted custom login form
            // ------------------------------------------------------------------
            
            if (log.isDebugEnabled()){
                log.debug("Checking if its a josso_authentication for '" + hreq.getRequestURI() + "'");
            }
            if (hreq.getRequestURI().endsWith(agent.getJossoAuthenticationUri())) {

            	if (log.isDebugEnabled()){
                    log.debug("josso_authentication received for uri '" + hreq.getRequestURI() + "'");
            	}
            	
            	GenericServletSSOAgentRequest customAuthRequest = (GenericServletSSOAgentRequest) doMakeSSOAgentRequest(cfg.getId(), SSOAgentRequest.ACTION_CUSTOM_AUTHENTICATION, jossoSessionId, nodeId, localSession, null, hreq, hres);
                
                agent.processRequest(customAuthRequest);
                
                return;
            }

            if (cookie == null || cookie.getValue().equals("-")) {
            	
            	// ------------------------------------------------------------------
                // Trigger LOGIN OPTIONAL if required
                // ------------------------------------------------------------------

            	 if (log.isDebugEnabled())
            		 log.debug("SSO cookie is not present, verifying optional login process ");

                // We have no cookie, remember me is enabled and a security check without assertion was received ...
                // This means that the user could not be identified ... go back to the original resource
                if (hreq.getRequestURI().endsWith(agent.getJossoSecurityCheckUri()) &&
                    hreq.getParameter("josso_assertion_id") == null) {

                	 if (log.isDebugEnabled())
                		 log.debug(agent.getJossoSecurityCheckUri() + " received without assertion.  Login Optional Process failed");

                    String requestURI = getSavedRequestURL(hreq);
                    agent.prepareNonCacheResponse(hres);
                    hres.sendRedirect(hres.encodeRedirectURL(requestURI));
                    return;

                }
                
            	// This is a standard anonymous request!
                if (!hreq.getRequestURI().endsWith(agent.getJossoSecurityCheckUri())) {

                    if (!agent.isResourceIgnored(cfg, hreq) &&
                    		agent.isAutomaticLoginRequired(hreq, hres)) {

                        if (log.isDebugEnabled())
                        	log.debug("SSO cookie is not present, attempting automatic login");

                        // Save current request, so we can co back to it later ...
                        saveRequestURL(hreq, hres);
                        String loginUrl = agent.buildLoginOptionalUrl(hreq);

                        if (log.isDebugEnabled())
                        	log.debug("Redirecting to login url '" + loginUrl + "'");
                        
                        //set non cache headers
                        agent.prepareNonCacheResponse(hres);
                        hres.sendRedirect(hres.encodeRedirectURL(loginUrl));
                        return;
                    } else {
                    	if (log.isDebugEnabled())
                    		log.debug("SSO cookie is not present, but login optional process is not required");
                    }

                    // save requested resource
                	if (!agent.isResourceIgnored(cfg, hreq)) {
    	            	StringBuffer sb = new StringBuffer(hreq.getRequestURI());
    	                if (hreq.getQueryString() != null) {
    	                    sb.append('?');
    	                    sb.append(hreq.getQueryString());
    	                }
    	            	agent.setAttribute(hreq, hres, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI, sb.toString());
                	}

                }
                
                if (log.isDebugEnabled())
                	log.debug("SSO cookie is not present, checking for outbound relaying");

                if (!(hreq.getRequestURI().endsWith(agent.getJossoSecurityCheckUri()) &&
                    hreq.getParameter("josso_assertion_id") != null)) {
                    log.debug("SSO cookie not present and relaying was not requested, skipping");
                    filterChain.doFilter(hreq, hres);
                    return;
                }

            }

            // ------------------------------------------------------------------
            // Check if this URI is subject to SSO protection
            // ------------------------------------------------------------------
            if (agent.isResourceIgnored(cfg, hreq)) {
                filterChain.doFilter(hreq, hres);
                return;
            }

            // This URI should be protected by SSO, go on ...
            if (log.isDebugEnabled())
                log.debug("Session is: " + session);
            
            // ------------------------------------------------------------------
            // Invoke the SSO Agent
            // ------------------------------------------------------------------
            if (log.isDebugEnabled())
                log.debug("Executing agent...");

            // ------------------------------------------------------------------
            // Check if a user has been authenitcated and should be checked by the agent.
            // ------------------------------------------------------------------
            if (log.isDebugEnabled())
                log.debug("Checking if its a josso_security_check for '" + hreq.getRequestURI() + "'");

            if (hreq.getRequestURI().endsWith(agent.getJossoSecurityCheckUri()) &&
                hreq.getParameter("josso_assertion_id") != null) {

                if (log.isDebugEnabled())
                    log.debug("josso_security_check received for uri '" + hreq.getRequestURI() + "' assertion id '" +
                            hreq.getParameter("josso_assertion_id")
                    );

                String assertionId = hreq.getParameter(Constants.JOSSO_ASSERTION_ID_PARAMETER);

                GenericServletSSOAgentRequest relayRequest;

                if (log.isDebugEnabled())
                    log.debug("Outbound relaying requested for assertion id [" + assertionId + "]");

                relayRequest = (GenericServletSSOAgentRequest) doMakeSSOAgentRequest( cfg.getId(), SSOAgentRequest.ACTION_RELAY, null, nodeId, localSession, assertionId, hreq, hres);

                SingleSignOnEntry entry = agent.processRequest(relayRequest);
                if (entry == null) {
                    // This is wrong! We should have an entry here!
                    log.error("Outbound relaying failed for assertion id [" + assertionId + "], no Principal found.");
                    // Throw an exception and let the container send the INERNAL SERVER ERROR
                    throw new ServletException("No Principal found. Verify your SSO Agent Configuration!");
                }

                if (log.isDebugEnabled())
                    log.debug("Outbound relaying successful for assertion id [" + assertionId + "]");

                if (log.isDebugEnabled())
                    log.debug("Assertion id [" + assertionId + "] mapped to SSO session id [" + entry.ssoId + "]");

                // The cookie is valid to for the partner application only ... in the future each partner app may
                // store a different auth. token (SSO SESSION) value
                cookie = agent.newJossoCookie(hreq.getContextPath(), entry.ssoId, hreq.isSecure());
                hres.addCookie(cookie);

                // Redirect the user to the original request URI (which will cause
                // the original request to be restored)
                String requestURI = getSavedSplashResource(hreq);
                if(requestURI == null) {
                	requestURI = getSavedRequestURL(hreq);
	                if (requestURI == null) {
	
	                	if (cfg.getDefaultResource() != null) {
                            requestURI = cfg.getDefaultResource();
                        } else {
    		                // If no saved request is found, redirect to the partner app root :
	    	                requestURI = hreq.getRequestURI().substring(
		                        0, (hreq.getRequestURI().length() - agent.getJossoSecurityCheckUri().length()));
                        }
	                	
	                    // If we're behind a reverse proxy, we have to alter the URL ... this was not necessary on tomcat 5.0 ?!
	                    String singlePointOfAccess = agent.getSinglePointOfAccess();
	                    if (singlePointOfAccess != null) {
	                        requestURI = singlePointOfAccess + requestURI;
	                    } else {
	                        String reverseProxyHost = hreq.getHeader(org.josso.gateway.Constants.JOSSO_REVERSE_PROXY_HEADER);
	                        if (reverseProxyHost != null) {
	                            requestURI = reverseProxyHost + requestURI;
	                        }
	                    }
	
	                    if (log.isDebugEnabled())
	                        log.debug("No saved request found, using : '" + requestURI + "'");
	                }
                }

                clearSavedRequestURLs(hreq, hres);
               	agent.clearAutomaticLoginReferer(hreq, hres);
               	agent.prepareNonCacheResponse(hres);
               	
               	// Check if we have a post login resource :
                String postAuthURI = cfg.getPostAuthenticationResource();
                if (postAuthURI != null) {
                    String postAuthURL = agent.buildPostAuthUrl(hres, requestURI, postAuthURI);
                    if (log.isDebugEnabled())
                        log.debug("Redirecting to post-auth-resource '" + postAuthURL  + "'");
                    hres.sendRedirect(postAuthURL);
                } else {
                	if (log.isDebugEnabled())
                         log.debug("Redirecting to original '" + requestURI + "'");
                    hres.sendRedirect(hres.encodeRedirectURL(requestURI));
                }
               	
                return;
            }


            SSOAgentRequest r = doMakeSSOAgentRequest(cfg.getId(), SSOAgentRequest.ACTION_ESTABLISH_SECURITY_CONTEXT, jossoSessionId, nodeId, localSession, null, hreq, hres);
            SingleSignOnEntry entry = agent.processRequest(r);

            if (log.isDebugEnabled())
                log.debug("Executed agent.");

            // ------------------------------------------------------------------
            // Has a valid user already been authenticated?
            // ------------------------------------------------------------------
            if (log.isDebugEnabled())
                log.debug("Process request for '" + hreq.getRequestURI() + "'");

            if (entry != null) {
                if (log.isDebugEnabled())
                    log.debug("Principal '" + entry.principal +
                        "' has already been authenticated");
                // TODO : Not supported
                // (request).setAuthType(entry.authType);
                // (request).setUserPrincipal(entry.principal);
            } else {
            	log.info("No Valid SSO Session, attempt an optional login?");
                // This is a standard anonymous request!

            	if (cookie != null) {
                	// cookie is not valid
                	cookie = agent.newJossoCookie(hreq.getContextPath(), "-", hreq.isSecure());
                	hres.addCookie(cookie);
                }
            	
            	if (cookie != null || (getSavedRequestURL(hreq) == null && agent.isAutomaticLoginRequired(hreq, hres))) {

                    if (log.isDebugEnabled())
                    	log.debug("SSO Session is not valid, attempting automatic login");

                    // Save current request, so we can co back to it later ...
                    saveRequestURL(hreq, hres);
                    String loginUrl = agent.buildLoginOptionalUrl(hreq);

                    if (log.isDebugEnabled())
                    	log.debug("Redirecting to login url '" + loginUrl + "'");
                    
                    //set non cache headers
                    agent.prepareNonCacheResponse(hres);
                    hres.sendRedirect(hres.encodeRedirectURL(loginUrl));
                    return;
                } else {
                    if (log.isDebugEnabled())
                    	log.debug("SSO cookie is not present, but login optional process is not required");
                }

            }

            // propagate the login and logout URLs to
            // partner applications.
            hreq.setAttribute("org.josso.agent.gateway-login-url", agent.getGatewayLoginUrl() );
            hreq.setAttribute("org.josso.agent.gateway-logout-url", agent.getGatewayLogoutUrl() );
            hreq.setAttribute("org.josso.agent.ssoSessionid", jossoSessionId);

            // ------------------------------------------------------------------
            // Invoke the next Valve in our pipeline
            // ------------------------------------------------------------------
            filterChain.doFilter(hreq, hres);
        } finally {
            if (log.isDebugEnabled())
                log.debug("Processed : " + hreq.getContextPath());
        }
    }

    public void destroy() {
        // Validate and update our current component state
        if (agent != null) {
            agent.stop();
            agent = null;
        }


    }
    
    /**
     * Return the splash resource from session so that we can redirect the user to it
     * if (s)he was logged in using custom form
     * @param hreq current http request
     */
    private String getSavedSplashResource(HttpServletRequest hreq){
    	return agent.getAttribute(hreq, Constants.JOSSO_SPLASH_RESOURCE_PARAMETER);
    }    


    /**
     * Return the request URI (with the corresponding query string, if any)
     * from the saved request so that we can redirect to it.
     *
     * @param hreq current http request
     */
    private String getSavedRequestURL(HttpServletRequest hreq) {
        return agent.getAttribute(hreq, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI);
    }

    /**
     * Creates a new request
     */
    protected SSOAgentRequest doMakeSSOAgentRequest(String requester, int action, String sessionId, String nodeId, LocalSession session, String assertionId,
                                                    HttpServletRequest hreq, HttpServletResponse hres) {
        GenericServletSSOAgentRequest r = new GenericServletSSOAgentRequest(requester, action, sessionId, session, assertionId, nodeId);
        r.setRequest(hreq);
        r.setResponse(hres);

        return r;

    }

    /**
     * Saves the original request URL into our session.
     *
     * @param hreq The request to be saved
     * @param hres The http servlet response associated to the request
     */
    private void saveRequestURL(HttpServletRequest hreq, HttpServletResponse hres) {
        StringBuffer sb = new StringBuffer(hreq.getRequestURI());
        if (hreq.getQueryString() != null) {
            String q = hreq.getQueryString();
            if (!q.startsWith("?"))
                sb.append('?');
            sb.append(q);
        }
        agent.setAttribute(hreq, hres, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI, sb.toString());
    }

    /**
     * Save referer URI into our session for later use.
     * 
     * This method is used so agent can know from which
     * public resource (page) user requested login
     *
     * @deprecated.
     * 
     * @param request http request
     * @param session current session
     * @param overrideSavedResource true if saved resource should be overridden, false otherwise
     */
    protected void saveLoginBackToURL(HttpServletRequest request, HttpSession session, boolean overrideSavedResource) {
        saveLoginBackToURL(request, null, session, overrideSavedResource);
    }

    /**
     * Save referer URI into our session for later use.
     *
     * This method is used so agent can know from which
     * public resource (page) user requested login.
     *
     * @param request http request
     * @param response http response
     * @param session current session
     * @param overrideSavedResource true if saved resource should be overridden, false otherwise
     */
    protected void saveLoginBackToURL(HttpServletRequest request, HttpServletResponse response, HttpSession session, boolean overrideSavedResource) {

    	String referer = request.getHeader("referer");
    	if ((getSavedRequestURL(request) == null || overrideSavedResource) && referer != null && !referer.equals("")) {

    		agent.setAttribute(request, response, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI, referer);
        }
    }
    
    /**
     * Remove saved request URLs from session 
     * to avoid mixing up resources from previous operations 
     * (logins, logouts) with the current one.
     * 
     * @param hreq http request
     * @param hres http response
     */
    protected void clearSavedRequestURLs(HttpServletRequest hreq, HttpServletResponse hres) {
    	agent.removeAttribute(hreq, hres, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI);
    	agent.removeAttribute(hreq, hres, Constants.JOSSO_SPLASH_RESOURCE_PARAMETER);
    }

}
