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
import org.josso.gateway.assertion.exceptions.AssertionNotValidException;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.josso.gateway.identity.service.SSOIdentityProviderService;
import org.josso.gateway.session.exceptions.FatalSSOSessionException;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.exceptions.SSOSessionException;
import org.josso.gateway.session.service.SSOSessionManagerService;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a partial implementation of an Single Sign-on Agent.
 * An Agent stands in between the Gateway and the Security Domain were partner application reside.
 * It provides transparent security context to partner applications, providing user and role information
 * by invoking the gateway through JAAS.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: AbstractSSOAgent.java 609 2008-08-21 19:24:02Z sgonzalez $
 */

public abstract class AbstractSSOAgent implements SSOAgent {

    /**
     * The name of the cookie that holds the JOSSO Session id.
     */
    // private static final String JOSSO_SINGLE_SIGN_ON_COOKIE = "JOSSO_SESSIONID";

    public static final long DEFAULT_SESSION_ACCESS_MIN_INTERVAL = 1000;

    public static final ThreadLocal<SSOAgentRequest> _currentRequest = new ThreadLocal<SSOAgentRequest>();

    // ----------------------------------------------------- Instance Variables
    /**
     * The cache of SingleSignOnEntry instances for authenticated Principals,
     * keyed by the cookie value that is used to select them.
     */
    protected final Map<String, SingleSignOnEntry> cache =
            Collections.synchronizedMap(new HashMap<String, SingleSignOnEntry>());

    /**
     * The cache of single sign on identifiers, keyed by the Session that is
     * associated with them.
     */
    protected final Map<LocalSession, String> reverse =
            Collections.synchronizedMap(new HashMap<LocalSession, String>());

    protected boolean started = false;

    protected int debug = 0;

    protected GatewayServiceLocator gsl;
    protected SSOSessionManagerService sm;
    protected SSOIdentityManagerService im;
    protected SSOIdentityProviderService ip;

    // ---------------<Configuration properties >
    protected SSOAgentConfiguration _cfg;

    private String _gatewayLoginUrl;
    private String _gatewayLogoutUrl;
    private String _gatewayLoginErrorUrl;
    private String _singlePointOfAccess;

    private long _sessionAccessMinInterval = DEFAULT_SESSION_ACCESS_MIN_INTERVAL;

    private boolean _isStateOnClient = false;
    
    // --------- <Some statistical information, exposed through MBeans >
    private long _requestCount;
    private long _l1CacheHits;
    private long _l2CacheHits;


    // ----------------------------------------------------- Properties

    /**
     * Configures the Gateway Service Locator to be used by the SSOAgent.
     */
    public void setGatewayServiceLocator(GatewayServiceLocator gsl) {
        this.gsl = gsl;
    }

    /**
     *
     * Obtains the Gateway Service Locator used by the SSOAgent to build
     * the concrete clients needed to query the Gateway services.
     * <p/>
     * This getter is need by the JAASLoginModule to know which Gateway Service Locator to use.
     *
     * @return the configured gateway service locator
     */
    public GatewayServiceLocator getGatewayServiceLocator() {
        return gsl;
    }

    public SSOSessionManagerService getSSOSessionManager() {
        return sm;
    }

    public SSOIdentityManagerService getSSOIdentityManager() {
        return im;
    }

    /**
     * Sets the Login Form Url of the Gateway.
     */
    public void setGatewayLoginUrl(String gatewayLoginUrl) {
        _gatewayLoginUrl = gatewayLoginUrl;
    }

    /**
     * Returns the Login Form Url of the Gateway.
     *
     * @return the gateway login url
     */
    public String getGatewayLoginUrl() {
        return _gatewayLoginUrl;
    }

    /**
     * Returns the Error Login Url of the Gateway.
     *
     * @return the gateway login url
     * @deprecated No longer supported!
     */
    public String getGatewayLoginErrorUrl() {
        return _gatewayLoginErrorUrl;
    }

    /**
     * Sets the Error Login Url of the Gateway.
     * @deprecated no longe used
     */
    public void setGatewayLoginErrorUrl(String gatewayLoginErrorUrl) {
        log("gatewayLoginErrorUrl is no longer supported, modify your agent config.  Check customLoginUrl in JOSSO Gwy config for alternatives.");
        _gatewayLoginErrorUrl = gatewayLoginErrorUrl;
    }

    /**
     * Sets the Logout Form Url of the Gateway.
     */
    public void setGatewayLogoutUrl(String gatewayLogoutUrl) {
        _gatewayLogoutUrl = gatewayLogoutUrl;
    }

    /**
     * Returns the Logout Form Url of the Gateway.
     *
     * @return the gateway login url
     */
    public String getGatewayLogoutUrl() {
        return _gatewayLogoutUrl;
    }

    /**
     * Used by the configuraiton, to set the session access min interval.
     */
    public void setSessionAccessMinInterval(String v) {
        setSessionAccessMinInterval(Long.parseLong(v));
    }

    /**
     * Gets the session access min interval.
     */
    public long getSessionAccessMinInterval() {
        return _sessionAccessMinInterval;
    }

    /**
     * Sets the session access min interval.
     */
    public void setSessionAccessMinInterval(long sessionAccessMinInterval) {
        _sessionAccessMinInterval = sessionAccessMinInterval;
    }

    /**
     * Single Point of Access to the SSO infrastructure. Useful when working in N-Tier mode behind a reverse proxy or
     * load balancer
     */
    public String getSinglePointOfAccess() {
        return _singlePointOfAccess;
    }

    /**
     * Single Point of Access to the SSO infrastructure. Useful when working in N-Tier mode behind a reverse proxy or
     * load balancer
     *
     * @param singlePointOfAccess used in combination with reverse proxy setups.
     */
    public void setSinglePointOfAccess(String singlePointOfAccess) {
        _singlePointOfAccess = singlePointOfAccess;
    }

    /**
     * Returns true if the received context should be processed by this agent.  It means that
     * the context belongs to a partner application.
     *
     * @param contextPath the web application context to be checked.
     * @return true if this context belongs to a josso partner app.
     */
    public boolean isPartnerApp(String vhost, String contextPath) {
        return getPartnerAppConfig(vhost, contextPath) != null;
    }

    /**
     * Returns the partner application configuration definition associtated with the given context.
     * If no partner application is defined for the context, returns null.
     */
    public SSOPartnerAppConfig getPartnerAppConfig(String vhost, String contextPath) {

        List<SSOPartnerAppConfig> papps = _cfg.getSsoPartnerApps();

        if (contextPath == null || "".equals(contextPath))
            contextPath = "/";

        for (SSOPartnerAppConfig ssoPartnerAppConfig : papps) {

            if ((ssoPartnerAppConfig.getVhost() == null || ssoPartnerAppConfig.getVhost().equals(vhost)) &&
                    contextPath.equals(ssoPartnerAppConfig.getContext()))
                return ssoPartnerAppConfig;
        }

        log("No partner application configured for '"+vhost+"' and '"+contextPath+"'");
        return null;

    }

    /**
     * Starts the Agent.
     */
    public void start() {

        try {
            sm = gsl.getSSOSessionManager();
            im = gsl.getSSOIdentityManager();
            ip = gsl.getSSOIdentityProvider();

            for (SSOPartnerAppConfig cfg : _cfg.getSsoPartnerApps()) {
                if (cfg.getId() == null) {
                    log("ERROR! You should define an ID for partner application " + cfg.getContext());
                }
            }

            if (debug > 0)
                log("Agent Started");
        } catch (Exception e) {
            log("Can't create session/identity managers : \n" + e.getMessage(), e);
        }

    }

    /**
     * Authenticated a user session previously authenticated by the gateway.
     *
     * @param request the JOSSO Agent request
     * @return the logged user identity.
     */
    public final SingleSignOnEntry processRequest(SSOAgentRequest request) {

        // We need to make the request available to other componets that responde to container contracts

        try {
            _currentRequest.set(request);

            return execute(request);
        } finally {
            _currentRequest.remove();

        }

    }

    protected SingleSignOnEntry execute(SSOAgentRequest request) {

        try {

            // Count this request.
            _requestCount++;

            int action = request.getAction();
            String jossoSessionId = request.getSessionId();
            LocalSession localSession = request.getLocalSession();

            if (action == SSOAgentRequest.ACTION_ASSERT_SESSION) {

                try {
                    accessSession(request.getRequester(), jossoSessionId);
                } catch (SSOSessionException e) {
                    throw new FatalSSOSessionException("Assertion error for session : " + jossoSessionId, e);
                }

                return null;
            }
            
            if (action == SSOAgentRequest.ACTION_CUSTOM_AUTHENTICATION){
            	sendCustomAuthentication(request);
            	return null;
            }

            if (action == SSOAgentRequest.ACTION_RELAY) {

                String assertionId = request.getAssertionId();
                jossoSessionId = resolveAssertion(request.getRequester(), assertionId);
                request.setSessionId(jossoSessionId);
            }


            // Look up the cached Principal associated with this cookie value
            if (debug > 0)
                log("Checking for cached principal for " + jossoSessionId);

            SingleSignOnEntry entry = lookup(jossoSessionId);
            if (entry != null) {

                if (debug > 0)
                    log(" Found cached principal '" +
                            entry.principal.getName() + "' with auth type '" +
                            entry.authType + "'");

                // Count the cache hit.
                _l1CacheHits++;

                entry = accessSession(request.getRequester(), entry, jossoSessionId);

                if (entry != null) {

                    if (isAuthenticationAlwaysRequired()) {
                        Principal p = authenticate(request);
                        if (debug > 0)
                            log("Updating Principal information");
                        entry.updatePrincipal(p);
                    }

                    // Even if a cached principal is present, the container-private
                    // security context might not be, therefore force an authentication
                    // so that the security context is recreated.
                    //
                    propagateSecurityContext(request, entry.principal);

                }

                return entry;
            }

            // Make the agent receive local session events.
            localSession.addSessionListener(this);

            // Associated local session to the SSO Session
            associateLocalSession(jossoSessionId, localSession);

            // Invoke the JAAS Gateway Login Module to obtain user information
            Principal ssoUserPrincipal = authenticate(request);

            if (ssoUserPrincipal != null) {
                if (debug > 0)
                    log("Principal checked for SSO Session '" + jossoSessionId + "' : " + ssoUserPrincipal);

                register(jossoSessionId, ssoUserPrincipal, "JOSSO");
                entry = lookup(jossoSessionId);
                entry = accessSession(request.getRequester(), entry, jossoSessionId);

                if (entry != null)
                    propagateSecurityContext(request, entry.principal);

                return entry;

            }

            if (debug > 0)
                log("There is no associated principal for SSO Session '" + jossoSessionId + "'");

            return null;

        } catch (Exception e) {
            log("Error processing JOSSO Agent request : " + e.getMessage());
            if (debug > 0)
                log("Exception recieved while processing JOSSO Agent request : " + e.getMessage(), e);

            return null;
        }

    }

    protected void propagateSecurityContext(SSOAgentRequest request, Principal principal) {
        throw new java.lang.UnsupportedOperationException(
                "No support for alternative mechanisms for security context propagation"
        );
    }

    /**
     * Dereference assertion id by invoking the corresponding operation using the back-channel
     *
     * @param assertionId
     * @return null if the authentication assertion is invalid
     */
    protected String resolveAssertion(String requester, String assertionId) {

        try {

            if (debug > 0)
                log("Dereferencing assertion for id '" + assertionId + "'");

            String ssoSessionId = ip.resolveAuthenticationAssertion(requester, assertionId);

            if (debug > 0)
                log("Dereferencing assertion for id '" + assertionId + "' as SSO Session '"+ssoSessionId+"'");

            return ssoSessionId;
        } catch (AssertionNotValidException e) {
            if (debug > 0)
                log("Invalid Assertion");

            return null;

        } catch (Exception e) {
            log(e.getMessage() != null ? e.getMessage() : e.toString(), e);
            return null;
        }

    }

    /**
     * Access sso session related with given entry.  If sso session is no longer valid,
     * deregisters the session and return null.
     *
     * @param entry
     * @return null if the sso session is no longer valid.
     */
    protected SingleSignOnEntry accessSession(String requester, SingleSignOnEntry entry, String jossoSessionId) {

        // Just in case
        if (entry == null)
            return entry;

        // Do not access server more than once in a second ...
        long now = System.currentTimeMillis();
        if ((now - entry.lastAccessTime) < getSessionAccessMinInterval()) {
            _l2CacheHits++;
            return entry;
        }

        try {
            // send a keep-alive event for the SSO session

            if (debug > 0)
                log("Notifying keep-alive event for session '" + jossoSessionId + "'");

            sm.accessSession(requester, jossoSessionId);
            entry.lastAccessTime = now;
            return entry;

        } catch (NoSuchSessionException e) {
            if (debug > 0)
                log("SSO Session is no longer valid");

            deregister(entry.ssoId);
            return null;

        } catch (Exception e) {
            log(e.getMessage() != null ? e.getMessage() : e.toString(), e);
            deregister(entry.ssoId);
            return null;
        }

    }


    /**
     * Access sso session related with given the given SSO session identifier.
     * In case the session is invalid or cannot be asserted an SSOException is thrown.
     */
    protected void accessSession(String requester, String jossoSessionId) throws SSOSessionException {


        try {
            // send a keep-alive event for the SSO session
            if (debug > 0)
                log("Notifying keep-alive event for session '" + jossoSessionId + "'");

            sm.accessSession(requester, jossoSessionId);

        } catch (NoSuchSessionException e) {
            if (debug > 0)
                log("SSO Session is no longer valid");

            throw e;

        } catch (Exception e) {
            log(e.getMessage() != null ? e.getMessage() : e.toString(), e);
            throw new SSOSessionException(e.getMessage() != null ? e.getMessage() : e.toString(), e);
        }

    }
    
    abstract protected void sendCustomAuthentication(SSOAgentRequest request) throws IOException;

    /**
     * Template method used by the agent to obtain a principal from a SSO Agent request.
     *
     * @param request
     * @return the authenticated principal.
     */
    abstract protected Principal authenticate(SSOAgentRequest request);

    /**
     * This indicates that sso agent request must be always authenticated.
     */
    abstract protected boolean isAuthenticationAlwaysRequired();

    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    abstract protected void log(String message);

    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message   Message to be logged
     * @param throwable Associated exception
     */
    abstract protected void log(String message, Throwable throwable);

    /**
     * Stop the Agent.
     */
    public void stop() {
        if (debug > 0)
            log("Agent Stopped");

    }

    // ------------------------------------------------ LocalSessionListener Methods

    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event SessionEvent that has occurred
     */
    public void localSessionEvent(LocalSessionEvent event) {

        // We only care about session destroyed events
        if (!LocalSession.LOCAL_SESSION_DESTROYED_EVENT.equals(event.getType()))
            return;

        // Look up the single session id associated with this session (if any)
        LocalSession session = event.getLocalSession();

        if (debug > 0)
            log("Local session destroyed on " + session);

        // notify gateway that the session was destroyed.
        localSessionDestroyedEvent(session);

    }

    public void setConfiguration(SSOAgentConfiguration cfg) {
        _cfg = cfg;
    }

    public SSOAgentConfiguration getConfiguration() {
        return _cfg;
    }

    /**
     * Disassociates a Local Session from the Single Sign-on session since the Local Session
     * was destroyed.
     *
     * @param session
     */
    protected void localSessionDestroyedEvent(LocalSession session) {
        String ssoId = null;
        synchronized (reverse) {
            ssoId = reverse.remove(session);
        }
        if (ssoId == null)
            return;

        // Deregister this single sso session id, invalidating associated sessions
        deregister(ssoId);

    }


    /**
     * Associate the specified single sign on identifier with the
     * specified Session.
     *
     * @param ssoId        Single sign on identifier
     * @param localSession Local Session to be associated to the SSO Session.
     */
    protected void associateLocalSession(String ssoId, LocalSession localSession) {

        SingleSignOnEntry sso = lookup(ssoId);
        if (sso != null)
            sso.addSession(localSession);

        synchronized (reverse) {
            reverse.put(localSession, ssoId);
        }

    }

    /**
     * Deregister the specified single sign on identifier, and invalidate
     * any associated sessions.
     *
     * @param ssoId Single sign on identifier to deregister
     */
    protected void deregister(String ssoId) {

        // Look up and remove the corresponding SingleSignOnEntry
        SingleSignOnEntry sso = null;
        synchronized (cache) {
            sso = (SingleSignOnEntry) cache.remove(ssoId);
        }

    }


    /**
     * Register the specified Principal as being associated with the specified
     * value for the single sign on identifier.
     *
     * @param ssoId     Single sign on identifier to register
     * @param principal Associated user principal that is identified
     * @param authType  Authentication type used to authenticate this
     *                  user principal
     */
    protected void register(String ssoId, Principal principal, String authType) {

        synchronized (cache) {
            cache.put(ssoId, new SingleSignOnEntry(ssoId, principal, authType));
        }

    }

    /**
     * Look up and return the cached SingleSignOn entry associated with this
     * sso id value, if there is one; otherwise return <code>null</code>.
     *
     * @param ssoId Single sign on identifier to look up
     */
    protected SingleSignOnEntry lookup(String ssoId) {
        synchronized (cache) {
            return cache.get(ssoId);
        }
    }

    public int getDebug() {
        return debug;
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }

    public long getRequestCount() {
        return _requestCount;
    }

    public long getL1CacheHits() {
        return _l1CacheHits;
    }

    public long getL2CacheHits() {
        return _l2CacheHits;
    }

	public boolean isStateOnClient() {
		return _isStateOnClient;
	}

    public void setIsStateOnClient(boolean isStateOnClient) {
        _isStateOnClient = isStateOnClient;
    }


	public void setStateOnClient(boolean isStateOnClient) {
		_isStateOnClient = isStateOnClient;
	}
}

