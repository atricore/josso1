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

package org.josso.tc80.agent;

import org.apache.catalina.*;
import org.apache.catalina.authenticator.SavedRequest;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.josso.agent.*;
import org.josso.agent.http.WebAccessControlUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;


/**
 * Single Sign-On Agent implementation for Tomcat Catalina.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: SSOAgentValve.java 1657 2010-10-13 20:48:21Z sgonzalez $
 */
public class SSOAgentValve extends ValveBase
        implements Lifecycle, SessionListener, ContainerListener {

    private static final Log LOG = LogFactory.getLog(SSOAgentValve.class);

    /**
     * Descriptive information about this Valve implementation.
     */
    protected static String info =
            "org.apache.catalina.authenticator.SingleSignOn";

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * Component started flag.
     */
    protected boolean started = false;
    private CatalinaSSOAgent _agent;

    /**
     * Catalina Session to Local Session Map.
     */
    private Map<String, LocalSession> _sessionMap = Collections.synchronizedMap(new HashMap<String, LocalSession>());

    // ------------------------------------------------------ SessionListener Methods

    public void sessionEvent(SessionEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("JOSSO: SSOAgentValve.sessionEvent: " + event);
        }

        // extra protective guard
        try {
            // obtain the local session for the catalina session, and notify the
            // listeners for it.
            LocalSession localSession = _sessionMap.get(event.getSession().getId());

            if (event.getType().equals(Session.SESSION_DESTROYED_EVENT)) {
                if (localSession != null) {
                    localSession.expire();
                    _sessionMap.remove(event.getSession().getId());
                    if (LOG.isDebugEnabled())
                        LOG.debug("JOSSO: SSOAgentValve.sessionEvent: session not null. can expire.");
                }
            }

        } catch (Exception ex) {
            LOG.error("JOSSO: SESSION PURGE EXCEPTION: ", ex);
        }

        if (LOG.isDebugEnabled())
            LOG.debug("JOSSO: SSOAgentValve.sessionEvent: sessionMap.size remaining: " + _sessionMap.size());

        if (LOG.isDebugEnabled())
            LOG.debug("JOSSO: SSOAgentValve.sessionEvent: complete");

    }

    // ------------------------------------------------------ ContainerListener Methods

    public void containerEvent(ContainerEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("JOSSO: SSOAgentValve.containerEvent: " + event);
        }

        if (event.getType().equals(Context.CHANGE_SESSION_ID_EVENT)) {
            String[] sessionIds = (String[]) event.getData();
            String oldId = sessionIds[0];
            String newId = sessionIds[1];
            if (LOG.isDebugEnabled())
                LOG.debug("JOSSO: Due to session fixation protection session id " + oldId + " has changed to " + newId);

            LocalSession localSession = _sessionMap.get(oldId);
            if (localSession != null) {
                localSession.expire();
                _sessionMap.remove(oldId);
                if (LOG.isDebugEnabled())
                    LOG.debug("JOSSO: Expired local session " + oldId + " as it's no longer in use");
            }

        }
        if (LOG.isDebugEnabled())
            LOG.debug("JOSSO: SSOAgentValve.containerEvent: sessionMap.size remaining: " + _sessionMap.size());

        if (LOG.isDebugEnabled())
            LOG.debug("JOSSO: SSOAgentValve.containerEvent: complete");
    }

    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }

    /**
     * Set the Container to which this Valve is attached.
     *
     * @param container The container to which we are attached
    public void setContainer(Container container) {

    if (!(container instanceof Context))
    throw new IllegalArgumentException
    ("The SSOAgentValve must be associated to a Catalina Context or Host");

    super.setContainer(container);
    _container = container;
    }
     */

    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @throws org.apache.catalina.LifecycleException if this component detects a fatal error
     *                                                that prevents this component from being used
     */
    protected synchronized void startInternal() throws LifecycleException {

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                    ("Agent already started");
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        try {
            Lookup lookup = Lookup.getInstance();
            lookup.init("josso-agent-config.xml");
            _agent = (CatalinaSSOAgent) lookup.lookupSSOAgent();
            _agent.setDebug(LOG.isDebugEnabled() ? 1 : 0);
            _agent.setCatalinaContainer(container);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            throw new LifecycleException("Error starting SSO Agent : " + e.getMessage());
        }
        _agent.start();
        log("Started");
        setState(LifecycleState.STARTING);
    }

    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @throws org.apache.catalina.LifecycleException if this component detects a fatal error
     *                                                that needs to be reported
     */
    protected synchronized void stopInternal() throws LifecycleException {

        setState(LifecycleState.STOPPING);

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                    ("Agent not started");
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;
        _agent.stop();
        log("Stopped");

    }


    // ---------------------------------------------------------- Valve Methods


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {
        return (info);
    }

    /**
     * Perform single-sign-on support processing for this request.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     *                 in the current processing pipeline
     * @throws java.io.IOException            if an input/output error occurs
     * @throws javax.servlet.ServletException if a servlet error occurs
     */

    public void invoke(Request request, Response response)
            throws IOException, ServletException {

        HttpServletRequest hreq = request.getRequest();
        HttpServletResponse hres = response.getResponse();
        log("Processing : " + hreq.getContextPath() + " [" + hreq.getRequestURL() + "]");
        try {
            // ------------------------------------------------------------------
            // Check with the agent if this context should be processed.
            // ------------------------------------------------------------------
            String contextPath = hreq.getContextPath();
            String vhost = hreq.getServerName();

            // In catalina, the empty context is considered the root context
            if ("".equals(contextPath))
                contextPath = "/";

            if (!_agent.isPartnerApp(vhost, contextPath)) {
                getNext().invoke(request, response);
                log("Context is not a josso partner app : " + hreq.getContextPath());
                return;
            }

            // URI Encoding
            if (_agent.getUriEncoding() != null) {
                log("Setting request/response encoding to " + _agent.getUriEncoding());
                hreq.setCharacterEncoding(_agent.getUriEncoding());
                hres.setCharacterEncoding(_agent.getUriEncoding());
            }

            String nodeId = hreq.getParameter("josso_node");
            if (nodeId != null) {
                log("Storing JOSSO Node id : " + nodeId);
                _agent.setAttribute(hreq, hres, "JOSSO_NODE", nodeId);
            } else {
                nodeId = _agent.getAttribute(hreq, "JOSSO_NODE");
                log("Found JOSSO Node id : " + nodeId);
            }


            // ------------------------------------------------------------------
            // Check some basic HTTP handling
            // ------------------------------------------------------------------
            SSOPartnerAppConfig cfg = _agent.getPartnerAppConfig(vhost, contextPath);


            // P3P Header for IE 6+ compatibility when embedding JOSSO in a IFRAME
            if (cfg.isSendP3PHeader() && !hres.isCommitted()) {
                hres.setHeader("P3P", cfg.getP3PHeaderValue());
            }

            // Get our session ...
            Session session = getSession(request, true);

            // ------------------------------------------------------------------
            // Check if the partner application required the login form
            // ------------------------------------------------------------------
            log("Checking if its a josso_login_request for '" + hreq.getRequestURI() + "'");
            if (hreq.getRequestURI().endsWith(_agent.getJossoLoginUri()) ||
                    hreq.getRequestURI().endsWith(_agent.getJossoUserLoginUri())) {
                log("josso_login_request received for uri '" + hreq.getRequestURI() + "'");
                //save referer url in case the user clicked on Login from some public resource (page)
                //so agent can redirect the user back to that page after successful login
                if (hreq.getRequestURI().endsWith(_agent.getJossoUserLoginUri())) {
                    saveLoginBackToURL(hreq, hres, session, true);
                } else {
                    saveLoginBackToURL(hreq, hres, session, false);
                }

                String loginUrl = _agent.buildLoginUrl(hreq);
                log("Redirecting to login url '" + loginUrl + "'");
                //set non cache headers
                _agent.prepareNonCacheResponse(hres);
                hres.sendRedirect(hres.encodeRedirectURL(loginUrl));
                return;
            }

            // ------------------------------------------------------------------
            // Check if the partner application required a logout
            // ------------------------------------------------------------------

            log("Checking if its a josso_logout request for '" + hreq.getRequestURI() + "'");

            if (hreq.getRequestURI().endsWith(_agent.getJossoLogoutUri())) {
                log("josso_logout request received for uri '" + hreq.getRequestURI() + "'");
                String logoutUrl = _agent.buildLogoutUrl(hreq, cfg);
                log("Redirecting to logout url '" + logoutUrl + "'");

                // Clear previous COOKIE ...
                Cookie ssoCookie = _agent.newJossoCookie(request.getContextPath(), "-", hreq.isSecure());
                hres.addCookie(ssoCookie);

                //set non cache headers
                _agent.prepareNonCacheResponse(hres);
                hres.sendRedirect(hres.encodeRedirectURL(logoutUrl));
                return;
            }

            // ------------------------------------------------------------------
            // Check for the single sign on cookie
            // ------------------------------------------------------------------

            log("Checking for SSO cookie");
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
            log("Session is: " + session);
            LocalSession localSession = _sessionMap.get(session.getId());
            if (localSession == null) {
                localSession = new CatalinaLocalSession(session);
                // the local session is new so, make the valve listen for its events so that it can
                // map them to local session events.
                session.addSessionListener(this);
                session.getManager().getContext().addContainerListener(this);
                _sessionMap.put(session.getId(), localSession);

                log("Monitoring session " + session.getId());
            }
            // ------------------------------------------------------------------
            // Check if the partner application submitted custom login form
            // ------------------------------------------------------------------

            log("Checking if its a josso_authentication for '" + hreq.getRequestURI() + "'");
            if (hreq.getRequestURI().endsWith(_agent.getJossoAuthenticationUri())) {
                log("josso_authentication received for uri '" + hreq.getRequestURI() + "'");
                CatalinaSSOAgentRequest customAuthRequest = new CatalinaSSOAgentRequest(cfg.getId(),
                        SSOAgentRequest.ACTION_CUSTOM_AUTHENTICATION, jossoSessionId, localSession);
                customAuthRequest.setRequest(hreq);
                customAuthRequest.setResponse(hres);
                customAuthRequest.setContext(request.getContext());
                _agent.processRequest(customAuthRequest);
                return;
            }

            if (cookie == null || cookie.getValue().equals("-")) {

                // ------------------------------------------------------------------
                // Trigger LOGIN OPTIONAL if required
                // ------------------------------------------------------------------
                log("SSO cookie is not present, verifying optional login process ");

                // We have no cookie, remember me is enabled and a security check without assertion was received ...
                // This means that the user could not be identified ... go back to the original resource
                if (hreq.getRequestURI().endsWith(_agent.getJossoSecurityCheckUri()) &&
                        hreq.getParameter("josso_assertion_id") == null) {
                    String requestURI = this.getSavedRequestURL(hreq, session);
                    if (requestURI == null) {
                        requestURI = cfg.getDefaultResource();
                        log("Using default resource " + requestURI);
                    }
                    log(_agent.getJossoSecurityCheckUri() + " received without assertion.  Login Optional Process failed, redirecting to ["+ requestURI + "]");
                    _agent.prepareNonCacheResponse(hres);
                    hres.sendRedirect(hres.encodeRedirectURL(requestURI));
                    return;
                }

                // This is a standard anonymous request!
                if (!hreq.getRequestURI().endsWith(_agent.getJossoSecurityCheckUri())) {

                    // If saved request is NOT null, we're in the middle of another process ...
                    if (!isResourceIgnored(cfg, request) &&
                            _agent.isAutomaticLoginRequired(hreq, hres)) {
                        log("SSO cookie is not present, attempting automatic login");

                        // Save current request, so we can co back to it later ...
                        saveRequest(request, session);
                        String loginUrl = _agent.buildLoginOptionalUrl(hreq);
                        log("Redirecting to login url '" + loginUrl + "'");

                        //set non cache headers
                        _agent.prepareNonCacheResponse(hres);
                        hres.sendRedirect(hres.encodeRedirectURL(loginUrl));
                        return;
                    } else {
                        log("SSO cookie is not present, but login optional process is not required");
                    }
                    // save requested resource
                    if (!isResourceIgnored(cfg, request)) {
                        StringBuffer sb = new StringBuffer(hreq.getRequestURI());
                        if (hreq.getQueryString() != null) {
                            sb.append('?');
                            sb.append(hreq.getQueryString());
                        }
                        _agent.setAttribute(hreq, hres, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI, sb.toString());
                    }
                }
                log("SSO cookie is not present, checking for outbound relaying");
                if (!(hreq.getRequestURI().endsWith(_agent.getJossoSecurityCheckUri()) &&
                        hreq.getParameter("josso_assertion_id") != null)) {
                    log("SSO cookie not present and relaying was not requested, skipping");
                    getNext().invoke(request, response);
                    return;
                }

            }

            // ------------------------------------------------------------------
            // Check if this URI is subject to SSO protection
            // ------------------------------------------------------------------
            if (isResourceIgnored(cfg, request)) {
                getNext().invoke(request, response);
                return;
            }

            // This URI should be protected by SSO, go on ...
            log("Session is: " + session);

            // ------------------------------------------------------------------
            // Invoke the SSO Agent
            // ------------------------------------------------------------------
            log("Executing agent...");
            _agent.setCatalinaContainer(request.getContext());

            // ------------------------------------------------------------------
            // Check if a user has been authenticated and should be checked by the agent.
            // ------------------------------------------------------------------

            log("Checking if its a josso_security_check for '" + hreq.getRequestURI() + "'");
            if (hreq.getRequestURI().endsWith(_agent.getJossoSecurityCheckUri()) &&
                    hreq.getParameter("josso_assertion_id") != null) {
                log("josso_security_check received for uri '" + hreq.getRequestURI() + "' assertion id '" +
                        hreq.getParameter("josso_assertion_id")
                );
                String assertionId = hreq.getParameter(Constants.JOSSO_ASSERTION_ID_PARAMETER);
                CatalinaSSOAgentRequest relayRequest;
                log("Outbound relaying requested for assertion id [" + assertionId + "]");
                relayRequest = new CatalinaSSOAgentRequest(cfg.getId(),
                        SSOAgentRequest.ACTION_RELAY, null, localSession, assertionId
                );
                relayRequest.setRequest(hreq);
                relayRequest.setResponse(hres);
                relayRequest.setContext(request.getContext());

                SingleSignOnEntry entry = _agent.processRequest(relayRequest);
                if (entry == null) {
                    // This is wrong! We should have an entry here!

                    log("Outbound relaying failed for assertion id [" + assertionId + "], no Principal found.");
                    // Throw an exception, we will handle it below !
                    throw new RuntimeException("Outbound relaying failed. No Principal found. Verify your SSO Agent Configuration!");
                }
                log("Outbound relaying succesfull for assertion id [" + assertionId + "]");
                log("Assertion id [" + assertionId + "] mapped to SSO session id [" + entry.ssoId + "]");

                // The cookie is valid to for the partner application only ... in the future each partner app may
                // store a different auth. token (SSO SESSION) value
                cookie = _agent.newJossoCookie(request.getContextPath(), entry.ssoId, hreq.isSecure());
                hres.addCookie(cookie);

                //Redirect user to the saved splash resource (in case of auth request) or to request URI otherwise
                String requestURI = getSavedSplashResource(hreq);
                if (requestURI == null) {
                    requestURI = getSavedRequestURL(hreq, session);
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
                        log("No saved request found, using : '" + requestURI + "'");
                    }
                }

                clearSavedRequestURLs(hreq, hres, session);
                _agent.clearAutomaticLoginReferer(hreq, hres);
                _agent.prepareNonCacheResponse(hres);

                // Check if we have a post login resource :
                String postAuthURI = cfg.getPostAuthenticationResource();
                if (postAuthURI != null) {
                    String postAuthURL = _agent.buildPostAuthUrl(hres, requestURI, postAuthURI);

                    log("Redirecting to post-auth-resource '" + postAuthURL + "'");
                    hres.sendRedirect(postAuthURL);
                } else {

                    log("Redirecting to original '" + requestURI + "'");
                    hres.sendRedirect(hres.encodeRedirectURL(requestURI));
                }

                return;
            }

            CatalinaSSOAgentRequest r;

            log("Creating Security Context for Session [" + session + "]");
            r = new CatalinaSSOAgentRequest(cfg.getId(),
                    SSOAgentRequest.ACTION_ESTABLISH_SECURITY_CONTEXT, jossoSessionId, localSession
            );
            r.setRequest(hreq);
            r.setResponse(hres);
            r.setContext(request.getContext());

            SingleSignOnEntry entry = _agent.processRequest(r);


            log("Executed agent.");

            // ------------------------------------------------------------------
            // Has a valid user already been authenticated?
            // ------------------------------------------------------------------

            log("Process request for '" + hreq.getRequestURI() + "'");

            if (entry != null) {

                log("Principal '" + entry.principal +
                        "' has already been authenticated");

                (request).setAuthType(entry.authType);
                (request).setUserPrincipal(entry.principal);

            } else {
                log("No Valid SSO Session, attempt an optional login?");
                // This is a standard anonymous request!

                if (cookie != null) {
                    // cookie is not valid
                    cookie = _agent.newJossoCookie(request.getContextPath(), "-", hreq.isSecure());
                    hres.addCookie(cookie);
                }

                if (cookie != null || (getSavedRequestURL(hreq, session) == null && _agent.isAutomaticLoginRequired(hreq, hres))) {


                    log("SSO Session is not valid, attempting automatic login");

                    // Save current request, so we can co back to it later ...
                    saveRequest(request, session);
                    String loginUrl = _agent.buildLoginOptionalUrl(hreq);


                    log("Redirecting to login url '" + loginUrl + "'");

                    //set non cache headers
                    _agent.prepareNonCacheResponse(hres);
                    hres.sendRedirect(hres.encodeRedirectURL(loginUrl));
                    return;
                } else {

                    log("SSO cookie is not present, but login optional process is not required");
                }

            }

            // propagate the login and logout URLs to
            // partner applications.
            hreq.setAttribute("org.josso.agent.gateway-login-url", _agent.getGatewayLoginUrl());
            hreq.setAttribute("org.josso.agent.gateway-logout-url", _agent.getGatewayLogoutUrl());
            hreq.setAttribute("org.josso.agent.ssoSessionid", jossoSessionId);
            hreq.setAttribute("org.josso.agent.requester", r.getRequester());

            // ------------------------------------------------------------------
            // Invoke the next Valve in our pipeline
            // ------------------------------------------------------------------
            getNext().invoke(request, response);
        } catch (Throwable t) {
            //  This is a 'hack' : Because this valve exectues before the ErrorReportingValve, we need to preapare
            // some stuff and invoke the next valve in the chain always ...

            // Store this error, it will be checked by the ErrorReportingValve
            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, t);

            System.err.println(t);
            t.printStackTrace();

            // Mark this response as error!
            response.setError();

            // Let the next valves work on this
            getNext().invoke(request, response);
            return;

        } finally {

            log("Processed : " + hreq.getContextPath() + " [" + hreq.getRequestURL() + "]");
        }
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Return a String rendering of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("SingleSignOn[");
        // Sometimes the container is not present when this method is invoked ...
        sb.append(container != null ? container.getName() : "");
        sb.append("]");
        return (sb.toString());

    }

    // -------------------------------------------------------- Package Methods


    // ------------------------------------------------------ Protected Methods

    /**
     * Return the internal Session that is associated with this HttpRequest,
     * or <code>null</code> if there is no such Session.
     *
     * @param request The HttpRequest we are processing
     */
    protected Session getSession(Request request) {
        return (getSession(request, false));
    }

    /**
     * Return the internal Session that is associated with this HttpRequest,
     * possibly creating a new one if necessary, or <code>null</code> if
     * there is no such session and we did not create one.
     *
     * @param request The HttpRequest we are processing
     * @param create  Should we create a session if needed?
     */
    protected Session getSession(Request request, boolean create) {
        HttpServletRequest hreq = request.getRequest();
        HttpSession hses = hreq.getSession(create);

        log("getSession() : hses " + hses);
        if (hses == null)
            return (null);
        // Get catalina Context from request ...
        Manager manager = request.getContext().getManager();
        log("getSession() : manager is " + manager);
        if (manager == null)
            return (null);
        else {
            try {
                return (manager.findSession(hses.getId()));
            } catch (IOException e) {
                return (null);
            }
        }

    }

    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
        if (_agent != null)
            _agent.log(message);
    }

    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message   Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {
        if (_agent != null)
            _agent.log(message, throwable);
    }


    /**
     * Return the request URI (with the corresponding query string, if any)
     * from the saved request so that we can redirect to it.
     *
     * @param hreq    http request
     * @param session Our current session
     */
    private String getSavedRequestURL(HttpServletRequest hreq, Session session) {
        String savedURL = _agent.getAttribute(hreq, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI);
        if (savedURL == null || savedURL.equals("")) {
            SavedRequest saved =
                    (SavedRequest) session.getNote(org.apache.catalina.authenticator.Constants.FORM_REQUEST_NOTE);
            if (saved == null)
                return (null);
            StringBuffer sb = new StringBuffer(saved.getRequestURI());
            if (saved.getQueryString() != null) {
                sb.append('?');
                sb.append(saved.getQueryString());
            }
            savedURL = sb.toString();
        }
        return savedURL;
    }

    /**
     * Return the splash resource from session so that we can redirect the user to it
     * if (s)he was logged in using custom form
     *
     * @param hreq http request
     */
    private String getSavedSplashResource(HttpServletRequest hreq) {
        return _agent.getAttribute(hreq, Constants.JOSSO_SPLASH_RESOURCE_PARAMETER);
    }

    /**
     * Save the original request information into our session.
     *
     * @param request The request to be saved
     * @param session The session to contain the saved information
     * @throws java.io.IOException
     */
    protected void saveRequest(Request request, Session session)
            throws IOException {

        // Create and populate a SavedRequest object for this request
        SavedRequest saved = new SavedRequest();
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++)
                saved.addCookie(cookies[i]);
        }
        Enumeration names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            Enumeration values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                String value = (String) values.nextElement();
                saved.addHeader(name, value);
            }
        }
        Enumeration locales = request.getLocales();
        while (locales.hasMoreElements()) {
            Locale locale = (Locale) locales.nextElement();
            saved.addLocale(locale);
        }

        /** Need more catalina classes
         if ("POST".equalsIgnoreCase(request.getMethod())) {
         ByteChunk body = new ByteChunk();
         body.setLimit(request.getConnector().getMaxSavePostSize());

         byte[] buffer = new byte[4096];
         int bytesRead;
         InputStream is = request.getInputStream();

         while ( (bytesRead = is.read(buffer) ) >= 0) {
         body.append(buffer, 0, bytesRead);
         }
         saved.setContentType(request.getContentType());
         saved.setBody(body);
         } */

        saved.setMethod(request.getMethod());
        saved.setQueryString(request.getQueryString());
        saved.setRequestURI(request.getRequestURI());

        // Stash the SavedRequest in our session for later use
        session.setNote(org.apache.catalina.authenticator.Constants.FORM_REQUEST_NOTE, saved);

        StringBuffer sb = new StringBuffer(request.getRequestURI());
        if (request.getQueryString() != null) {
            String q = request.getQueryString();
            if (!q.startsWith("?"))
                sb.append('?');
            sb.append(q);
        }
        _agent.setAttribute(request.getRequest(), request.getResponse().getResponse(), WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI, sb.toString());
    }

    /**
     * Save referer URI into our session for later use.
     * <p>
     * This method is used so agent can know from which
     * public resource (page) user requested login.
     *
     * @param request               http request
     * @param response              http response
     * @param session               current session
     * @param overrideSavedResource true if saved resource should be overridden, false otherwise
     */
    protected void saveLoginBackToURL(HttpServletRequest request, HttpServletResponse response, Session session, boolean overrideSavedResource) {
        String referer = request.getHeader("referer");
        //saved request will exist only if user requested some protected resource
        String savedURL = _agent.getAttribute(request, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI);
        SavedRequest saved =
                (SavedRequest) session.getNote(org.apache.catalina.authenticator.Constants.FORM_REQUEST_NOTE);
        if (((savedURL == null && saved == null) || overrideSavedResource) && referer != null && !referer.equals("")) {

            saved = new SavedRequest();

            int p = referer.indexOf("?");

            String uri = p >= 0 ? referer.substring(0, p) : referer;
            String queryStr = p >= 0 ? referer.substring(p) : null;
            saved.setRequestURI(uri);
            saved.setQueryString(queryStr);

            session.setNote(org.apache.catalina.authenticator.Constants.FORM_REQUEST_NOTE, saved);

            _agent.setAttribute(request, response, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI, referer);
        } else if (saved != null) {
            StringBuffer sb = new StringBuffer(saved.getRequestURI());
            if (saved.getQueryString() != null) {
                String q = saved.getQueryString();
                if (!q.startsWith("?"))
                    sb.append('?');
                sb.append(q);
            }
            _agent.setAttribute(request, response, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI, sb.toString());
        }
    }

    /**
     * Remove saved request URLs from session
     * to avoid mixing up resources from previous operations
     * (logins, logouts) with the current one.
     *
     * @param hreq    http request
     * @param hres    http response
     * @param session Our current session
     */
    protected void clearSavedRequestURLs(HttpServletRequest hreq, HttpServletResponse hres, Session session) {
        session.removeNote(org.apache.catalina.authenticator.Constants.FORM_REQUEST_NOTE);
        _agent.removeAttribute(hreq, hres, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI);
        _agent.removeAttribute(hreq, hres, Constants.JOSSO_SPLASH_RESOURCE_PARAMETER);
    }

    protected boolean isResourceIgnored(SSOPartnerAppConfig cfg, Request request) {
        // There are some web-resources to ignore.
        String[] ignoredWebResources = cfg.getIgnoredWebRources();


        log("Found [" + (ignoredWebResources != null ? ignoredWebResources.length + "" : "no") + "] ignored web resources ");

        if (ignoredWebResources != null && ignoredWebResources.length > 0) {

            Realm realm = request.getContext().getRealm();
            SecurityConstraint[] constraints
                    = realm.findSecurityConstraints(request, request.getContext());

            if ((constraints != null)) {

                for (int i = 0; i < ignoredWebResources.length; i++) {

                    // For each ignored web resource, find a correspondig web resource collection.
                    String ignoredWebResource = ignoredWebResources[i];
                    for (int j = 0; j < constraints.length; j++) {

                        // Find a matching web resource collection for each ignored web resources
                        SecurityConstraint constraint = constraints[j];
                        if (constraint.findCollection(ignoredWebResource) != null) {

                            // We should ignore this URI, it's not subject to SSO protection.

                            log("Not subject to SSO protection :  web-resource-name:" + ignoredWebResource);

                            return true;
                        }
                    }
                }
            }
        }

        return false;

    }


}
