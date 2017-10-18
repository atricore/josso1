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

package org.josso.jaspi.agent;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
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
import org.josso.gateway.identity.SSORole;

import java.util.*;

/**
 * JOSSO server auth module.
 */
public class JASPISSOAuthModule extends JOSSOServerAuthModule {

    public static final String KEY_SESSION_MAP = "org.josso.servlet.agent.sessionMap";

	private static final Log log = LogFactory.getLog(JASPISSOAuthModule.class);
	
	private static HttpSSOAgent _agent;

    protected String delegatingLoginContextName = null;


    /**
     * <p>
     * Creates an instance of {@code JASPISSOAuthModule}.
     * </p>
     */
	public JASPISSOAuthModule() {
        this(null);
	}


    /**
     * <p>
     * Creates an instance of {@code HTTPFormServerAuthModule} with the specified delegating login context name.
     * </p>
     *
     * @param delegatingLoginContextName the name of the login context configuration that contains the JAAS modules that
     *                                   are to be called by this module.
     */
    public JASPISSOAuthModule(String delegatingLoginContextName) {
        super();

        this.delegatingLoginContextName = delegatingLoginContextName;
    }

    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options) throws AuthException {
        if (_agent == null) {
            synchronized(this) {
                try {
                    if (_agent == null) {
                        Lookup lookup = Lookup.getInstance();
                        lookup.init("josso-agent-config.xml");
                        _agent = (HttpSSOAgent) lookup.lookupSSOAgent();
                        if (log.isDebugEnabled()) {
                            _agent.setDebug(1);
                        }
                        _agent.start();

                        if (log.isDebugEnabled())
                            log.debug("Agent started");
                    }
                } catch (Exception e) {
                    log.error("Error starting SSO Agent : " + e.getMessage(), e);
                    throw new RuntimeException("Error starting SSO Agent : " + e.getMessage(), e);
                }
            }
        }

        super.initialize(requestPolicy, responsePolicy, handler, options);
    }

    @Override
	public AuthStatus secureResponse(MessageInfo messageInfo,
			Subject serviceSubject) throws AuthException {
		throw new RuntimeException("Not Applicable");
		//return AuthStatus.SEND_SUCCESS;
	}

	@Override
	public AuthStatus validateRequest(MessageInfo messageInfo,
			Subject clientSubject, Subject serviceSubject) throws AuthException {
		
		HttpServletRequest hreq = (HttpServletRequest) messageInfo.getRequestMessage();
	    HttpServletResponse hres = (HttpServletResponse) messageInfo.getResponseMessage();
	    
	    if (log.isDebugEnabled()) {
	    	log.debug("Processing : " + hreq.getContextPath() + " ["+hreq.getRequestURL()+"]");
	    }
	    
	    try {
	        // ------------------------------------------------------------------
	        // Check with the agent if this context should be processed.
	        // ------------------------------------------------------------------
	        String contextPath = hreq.getContextPath();
	        String vhost = hreq.getServerName();

	        // In catalina, the empty context is considered the root context
	        if ("".equals(contextPath)) {
	            contextPath = "/";
	        }

	        if (!_agent.isPartnerApp(vhost, contextPath)) {
	            if (log.isDebugEnabled()) {
	                log.debug("Context is not a josso partner app : " + hreq.getContextPath());
	            }
	            AuthStatus status = AuthStatus.SUCCESS;
                return status;
	        }

	        // ------------------------------------------------------------------
	        // Check some basic HTTP handling
	        // ------------------------------------------------------------------
	        // P3P Header for IE 6+ compatibility when embedding JOSSO in a IFRAME
	        SSOPartnerAppConfig cfg = _agent.getPartnerAppConfig(vhost, contextPath);
	        if (cfg.isSendP3PHeader() && !hres.isCommitted()) {
	            hres.setHeader("P3P", cfg.getP3PHeaderValue());
	        }

	        // Get our session ...
            HttpSession session = hreq.getSession(true);
	        
	        // ------------------------------------------------------------------
	        // Check if the partner application required the login form
	        // ------------------------------------------------------------------
	        if (log.isDebugEnabled()) {
	            log.debug("Checking if its a josso_login_request for '" + hreq.getRequestURI() + "'");
	        }
	        
	        if (hreq.getRequestURI().endsWith(_agent.getJossoLoginUri()) ||
	        		hreq.getRequestURI().endsWith(_agent.getJossoUserLoginUri())) {

	            if (log.isDebugEnabled()) {
	                log.debug("josso_login_request received for uri '" + hreq.getRequestURI() + "'");
	            }
	            
	            //save referer url in case the user clicked on Login from some public resource (page)
	            //so agent can redirect the user back to that page after successful login
	            if (hreq.getRequestURI().endsWith(_agent.getJossoUserLoginUri())) {
	            	saveLoginBackToURL(hreq, hres, session, true);
	            } else {
	            	saveLoginBackToURL(hreq, hres, session, false);
	            }
	            
	            String loginUrl = _agent.buildLoginUrl(hreq);

	            if (log.isDebugEnabled()) {
	                log.debug("Redirecting to login url '" + loginUrl + "'");
	            }
	            
	            //set non cache headers
	            _agent.prepareNonCacheResponse(hres);
	            hres.sendRedirect(hres.encodeRedirectURL(loginUrl));

                // Request is authorized for this URI
	            return AuthStatus.SEND_CONTINUE;
	        }
	        
	        // ------------------------------------------------------------------
	        // Check if the partner application required a logout
	        // ------------------------------------------------------------------
	        if (log.isDebugEnabled()) {
	            log.debug("Checking if its a josso_logout request for '" + hreq.getRequestURI() + "'");
	        }

	        if (hreq.getRequestURI().endsWith(_agent.getJossoLogoutUri())) {

	            if (log.isDebugEnabled()) {
	                log.debug("josso_logout request received for uri '" + hreq.getRequestURI() + "'");
	            }

	            String logoutUrl = _agent.buildLogoutUrl(hreq, cfg);

	            if (log.isDebugEnabled()) {
	                log.debug("Redirecting to logout url '" + logoutUrl + "'");
	            }

	            // Clear previous COOKIE ...
	            Cookie ssoCookie = _agent.newJossoCookie(hreq.getContextPath(), "-", hreq.isSecure());
	            hres.addCookie(ssoCookie);
	            
	            // invalidate session (unbind josso security context)
                session.invalidate();
                
	            //set non cache headers
	            _agent.prepareNonCacheResponse(hres);
	            hres.sendRedirect(hres.encodeRedirectURL(logoutUrl));

                // Request is authorized for this URI
	            return AuthStatus.SEND_CONTINUE;
	        }

	        // ------------------------------------------------------------------
	        // Check for the single sign on cookie
	        // ------------------------------------------------------------------
	        if (log.isDebugEnabled()) {
	            log.debug("Checking for SSO cookie");
	        }
	        Cookie cookie = null;
	        Cookie cookies[] = hreq.getCookies();
	        if (cookies == null) {
	            cookies = new Cookie[0];
	        }
	        for (int i = 0; i < cookies.length; i++) {
	            if (org.josso.gateway.Constants.JOSSO_SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
	                cookie = cookies[i];
	                break;
	            }
	        }
	        
	        String jossoSessionId = (cookie == null) ? null : cookie.getValue();
	        if (log.isDebugEnabled()) {
	            log.debug("Session is: " + session);
	        }


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

	        LocalSession localSession = (LocalSession) sessionMap.get(session.getId());
            if (localSession == null) {
                localSession = new JASPILocalSession(session);
                // the local session is new so, make the valve listen for its events so that it can
                // map them to local session events.
                // Not Supported : session.addSessionListener(this);
                sessionMap.put(session.getId(), localSession);


            }

	        // ------------------------------------------------------------------
	        // Check if the partner application submitted custom login form
	        // ------------------------------------------------------------------
	        
	        if (log.isDebugEnabled()) {
	            log.debug("Checking if its a josso_authentication for '" + hreq.getRequestURI() + "'");
	        }
	        if (hreq.getRequestURI().endsWith(_agent.getJossoAuthenticationUri())) {

	            if (log.isDebugEnabled()) {
	                log.debug("josso_authentication received for uri '" + hreq.getRequestURI() + "'");
	            }

	            JASPISSOAgentRequest customAuthRequest = (JASPISSOAgentRequest) 
	            			doMakeSSOAgentRequest(cfg.getId(), SSOAgentRequest.ACTION_CUSTOM_AUTHENTICATION, jossoSessionId,
	            			localSession, null, hreq, hres);
                
	            _agent.processRequest(customAuthRequest);

                // Request is authorized
	            return AuthStatus.SEND_CONTINUE;
	        }              

	        if (cookie == null || cookie.getValue().equals("-")) {

	            // ------------------------------------------------------------------
	            // Trigger LOGIN OPTIONAL if required
	            // ------------------------------------------------------------------

	            if (log.isDebugEnabled())
	                log.debug("SSO cookie is not present, verifying optional login process ");

	            // We have no cookie, remember me is enabled and a security check without assertion was received ...
	            // This means that the user could not be identified ... go back to the original resource
	            if (hreq.getRequestURI().endsWith(_agent.getJossoSecurityCheckUri()) &&
	                hreq.getParameter("josso_assertion_id") == null) {

	                if (log.isDebugEnabled())
	                    log.debug(_agent.getJossoSecurityCheckUri() + " received without assertion.  Login Optional Process failed");

	                String requestURI = this.getSavedRequestURL(hreq);
	                _agent.prepareNonCacheResponse(hres);
	                hres.sendRedirect(hres.encodeRedirectURL(requestURI));
	                AuthStatus status = AuthStatus.SEND_CONTINUE;
                    return status;
	            }

	            // This is a standard anonymous request!
	            if (!hreq.getRequestURI().endsWith(_agent.getJossoSecurityCheckUri())) {

	                // If saved request is NOT null, we're in the middle of another process ...
	                if (!_agent.isResourceIgnored(cfg, hreq) && 
                    		_agent.isAutomaticLoginRequired(hreq, hres)) {

	                    if (log.isDebugEnabled()) {
	                        log.debug("SSO cookie is not present, attempting automatic login");
	                    }
	                    
	                    // Save current request, so we can co back to it later ...
	                    saveRequestURL(hreq, hres);
	                    String loginUrl = _agent.buildLoginOptionalUrl(hreq);

	                    if (log.isDebugEnabled()) {
	                        log.debug("Redirecting to login url '" + loginUrl + "'");
	                    }
	                    
	                    //set non cache headers
	                    _agent.prepareNonCacheResponse(hres);
	                    hres.sendRedirect(hres.encodeRedirectURL(loginUrl));
	                    //hreq.getRequestDispatcher(loginUrl).forward(hreq, hres);
	                    AuthStatus status = AuthStatus.SEND_CONTINUE;
	                    return status;
	                } else {
	                    if (log.isDebugEnabled()) {
	                        log.debug("SSO cookie is not present, but login optional process is not required");
	                    }
	                }
	            }
	            
	            if (log.isDebugEnabled()) {
	                log.debug("SSO cookie is not present, checking for outbound relaying");
	            }

	            if (!(hreq.getRequestURI().endsWith(_agent.getJossoSecurityCheckUri()) &&
	                hreq.getParameter("josso_assertion_id") != null)) {
	                log.debug("SSO cookie not present and relaying was not requested, skipping");
	                AuthStatus status = AuthStatus.SUCCESS;
                    return status;
	            }
	
	        }

	        // ------------------------------------------------------------------
	        // Check if this URI is subject to SSO protection
	        // ------------------------------------------------------------------
	        if (_agent.isResourceIgnored(cfg, hreq)) {
                // Ignored resources are authorized
	        	return AuthStatus.SUCCESS;
	        }

	        // This URI should be protected by SSO, go on ...
	        if (log.isDebugEnabled()) {
	            log.debug("Session is: " + session);
	        }
	
	        // ------------------------------------------------------------------
	        // Invoke the SSO Agent
	        // ------------------------------------------------------------------
	        if (log.isDebugEnabled()) {
	            log.debug("Executing agent...");
	        }
	        
	        // ------------------------------------------------------------------
	        // Check if a user has been authenticated and should be checked by the agent.
	        // ------------------------------------------------------------------
	        if (log.isDebugEnabled()) {
	            log.debug("Checking if its a josso_security_check for '" + hreq.getRequestURI() + "'");
	        }

	        if (hreq.getRequestURI().endsWith(_agent.getJossoSecurityCheckUri()) &&
	            hreq.getParameter("josso_assertion_id") != null) {

	            if (log.isDebugEnabled()) {
	                log.debug("josso_security_check received for uri '" + hreq.getRequestURI() + "' assertion id '" +
	                        hreq.getParameter("josso_assertion_id")
	                );
	            }

	            String assertionId = hreq.getParameter(Constants.JOSSO_ASSERTION_ID_PARAMETER);

	            JASPISSOAgentRequest relayRequest;

	            if (log.isDebugEnabled()) {
	                log.debug("Outbound relaying requested for assertion id [" + assertionId + "]");
	            }

	            relayRequest = (JASPISSOAgentRequest) doMakeSSOAgentRequest(cfg.getId(), SSOAgentRequest.ACTION_RELAY, null, localSession, assertionId, hreq, hres);

	            SingleSignOnEntry entry = _agent.processRequest(relayRequest);
	            if (entry == null) {
	                // This is wrong! We should have an entry here!
	                if (log.isDebugEnabled()) {
	                    log.debug("Outbound relaying failed for assertion id [" + assertionId + "], no Principal found.");
	                }
	                // Throw an exception, we will handle it below !
	                throw new RuntimeException("Outbound relaying failed. No Principal found. Verify your SSO Agent Configuration!");
	            } else {
	            	// Add the SSOUser as a Principal
	                if (!clientSubject.getPrincipals().contains(entry.principal)) {
	                	clientSubject.getPrincipals().add(entry.principal);
	                }
                    SSORole[] ssoRolePrincipals = _agent.getRoleSets(cfg.getId(), entry.ssoId, relayRequest.getNodeId());
                    List<String> rolesList = new ArrayList<String>();

	                for (int i=0; i < ssoRolePrincipals.length; i++) {
	                    if (clientSubject.getPrincipals().contains(ssoRolePrincipals[i])) {
	                        continue;
	                    }
                        rolesList.add(ssoRolePrincipals[i].getName());

	                    clientSubject.getPrincipals().add(ssoRolePrincipals[i]);
	                    log.debug("Added SSORole Principal to the Subject : " + ssoRolePrincipals [i]);
	                }

	                registerWithCallbackHandler(entry.principal, entry.principal.getName(), entry.ssoId, rolesList.toArray(new String[rolesList.size()]));
	            }

	            if (log.isDebugEnabled()) {
	                log.debug("Outbound relaying succesfull for assertion id [" + assertionId + "]");
	            }

	            if (log.isDebugEnabled()) {
	                log.debug("Assertion id [" + assertionId + "] mapped to SSO session id [" + entry.ssoId + "]");
	            }

	            // The cookie is valid to for the partner application only ... in the future each partner app may
	            // store a different auth. token (SSO SESSION) value
	            cookie = _agent.newJossoCookie(hreq.getContextPath(), entry.ssoId, hreq.isSecure());
	            hres.addCookie(cookie);

	            //Redirect user to the saved splash resource (in case of auth request) or to request URI otherwise
	            String requestURI = getSavedSplashResource(hreq);
	            if(requestURI == null) {
	            	requestURI = getSavedRequestURL(hreq);
	                if (requestURI == null) {
	
	                    if (cfg.getDefaultResource() != null) {
	                        requestURI = cfg.getDefaultResource();
	                    } else {
			                // If no saved request is found, redirect to the partner app root :
	    	                requestURI = hreq.getRequestURI().substring(
		                        0, (hreq.getRequestURI().length() - _agent.getJossoSecurityCheckUri().length()));
	                    }
	                    
		                // If we're behind a reverse proxy, we have to alter the URL ... this was not necessary on tomcat 5.0 ?!
		                String singlePointOfAccess = _agent.getSinglePointOfAccess();
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

	            _agent.clearAutomaticLoginReferer(hreq, hres);
	            _agent.prepareNonCacheResponse(hres);

	            // Check if we have a post login resource :
	            String postAuthURI = cfg.getPostAuthenticationResource();
	            if (postAuthURI != null) {
	                String postAuthURL = _agent.buildPostAuthUrl(hres, requestURI, postAuthURI);
	                if (log.isDebugEnabled()) {
	                    log.debug("Redirecting to post-auth-resource '" + postAuthURL  + "'");
	                }
	                hres.sendRedirect(postAuthURL);
	            } else {
	                if (log.isDebugEnabled()) {
	                    log.debug("Redirecting to original '" + requestURI + "'");
	                }
	                hres.sendRedirect(hres.encodeRedirectURL(requestURI));
	            }

	            AuthStatus status = AuthStatus.SEND_SUCCESS;
                return status;
	        }
	        
	        if (log.isDebugEnabled()) {
	        	log.debug("Creating Security Context for Session [" + session + "]");
	        }
	        SSOAgentRequest r = doMakeSSOAgentRequest(cfg.getId(), SSOAgentRequest.ACTION_ESTABLISH_SECURITY_CONTEXT, jossoSessionId, localSession, null, hreq, hres);
            SingleSignOnEntry entry = _agent.processRequest(r);

	        if (log.isDebugEnabled()) {
	            log.debug("Executed agent.");
	        }

	        // ------------------------------------------------------------------
	        // Has a valid user already been authenticated?
	        // ------------------------------------------------------------------
	        if (log.isDebugEnabled()) {
	            log.debug("Process request for '" + hreq.getRequestURI() + "'");
	        }

	        if (entry != null) {
	            if (log.isDebugEnabled()) {
	                log.debug("Principal '" + entry.principal +
	                    "' has already been authenticated");
	            }
	            // Add the SSOUser as a Principal
                if (!clientSubject.getPrincipals().contains(entry.principal)) {
                	clientSubject.getPrincipals().add(entry.principal);
                }
                SSORole[] ssoRolePrincipals = _agent.getRoleSets(cfg.getId(), entry.ssoId, r.getNodeId());
                List<String> rolesList = new ArrayList<String>();
                for (int i=0; i < ssoRolePrincipals.length; i++) {
                    if (clientSubject.getPrincipals().contains(ssoRolePrincipals[i])) {
                        continue;
                    }
                    rolesList.add(ssoRolePrincipals[i].getName());
                    clientSubject.getPrincipals().add(ssoRolePrincipals[i]);
                    log.debug("Added SSORole Principal to the Subject : " + ssoRolePrincipals [i]);
                }
                registerWithCallbackHandler(entry.principal, entry.principal.getName(), entry.ssoId, rolesList.toArray(new String[rolesList.size()]));
	        } else {
	            log.debug("No Valid SSO Session, attempt an optional login?");
	            // This is a standard anonymous request!
	            
	            if (cookie != null) {
	            	// cookie is not valid
	            	cookie = _agent.newJossoCookie(hreq.getContextPath(), "-", hreq.isSecure());
	            	hres.addCookie(cookie);
	            }
	            
	            if (cookie != null || (getSavedRequestURL(hreq) == null && _agent.isAutomaticLoginRequired(hreq, hres))) {
	                if (log.isDebugEnabled()) {
	                    log.debug("SSO Session is not valid, attempting automatic login");
	                }

	                // Save current request, so we can co back to it later ...
	                saveRequestURL(hreq, hres);
	                String loginUrl = _agent.buildLoginOptionalUrl(hreq);

	                if (log.isDebugEnabled()) {
	                    log.debug("Redirecting to login url '" + loginUrl + "'");
	                }
	                
	                //set non cache headers
	                _agent.prepareNonCacheResponse(hres);
	                hres.sendRedirect(hres.encodeRedirectURL(loginUrl));

                    // Request is authorized for this URI
	                return AuthStatus.SEND_CONTINUE;
	            } else {
	                if (log.isDebugEnabled()) {
	                    log.debug("SSO cookie is not present, but login optional process is not required");
	                }
	            }

	        }

	        // propagate the login and logout URLs to
	        // partner applications.
	        hreq.setAttribute("org.josso.agent.gateway-login-url", _agent.getGatewayLoginUrl() );
	        hreq.setAttribute("org.josso.agent.gateway-logout-url", _agent.getGatewayLogoutUrl() );
	        hreq.setAttribute("org.josso.agent.ssoSessionid", jossoSessionId);
			hreq.setAttribute("org.josso.agent.requester", r.getRequester());
	
	        clearSavedRequestURLs(hreq, hres);
	        
	        AuthStatus status = AuthStatus.SUCCESS;
            return status;
	    } catch (Throwable t) {
            log.warn(t.getMessage(), t);
	    	throw new AuthException(t.getMessage());
	        //return AuthStatus.FAILURE;
	    } finally {
	        if (log.isDebugEnabled()) {
	            log.debug("Processed : " + hreq.getContextPath() + " ["+hreq.getRequestURL()+"]");
	        }
	    }
	}

	/**
     * Creates a new SSO Agent request.
     * 
     * @return SSO Agent request
     */
    protected SSOAgentRequest doMakeSSOAgentRequest(String requester, int action, String sessionId, LocalSession session, String assertionId,
    			HttpServletRequest hreq, HttpServletResponse hres) {
        JASPISSOAgentRequest r = new JASPISSOAgentRequest(requester, action, sessionId, session, assertionId);
        r.setRequest(hreq);
        r.setResponse(hres);
        return r;
    }
    
	/**
     * Return the request URI (with the corresponding query string, if any)
     * from the saved request so that we can redirect to it.
     *
     * @param hreq current http request
     */
    private String getSavedRequestURL(HttpServletRequest hreq) {
        return _agent.getAttribute(hreq, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI);
    }
    
    /**
     * Return the splash resource from session so that we can redirect the user to it
     * if (s)he was logged in using custom form.
     * @param hreq current http request
     */
    private String getSavedSplashResource(HttpServletRequest hreq){
    	return _agent.getAttribute(hreq, Constants.JOSSO_SPLASH_RESOURCE_PARAMETER);
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
        _agent.setAttribute(hreq, hres, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI, sb.toString());
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
    		_agent.setAttribute(request, response, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI, referer);
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
    	_agent.removeAttribute(hreq, hres, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI);
    	_agent.removeAttribute(hreq, hres, Constants.JOSSO_SPLASH_RESOURCE_PARAMETER);
    }
}
