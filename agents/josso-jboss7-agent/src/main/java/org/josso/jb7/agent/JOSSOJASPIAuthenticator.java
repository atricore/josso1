package org.josso.jb7.agent;

import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.DateTool;
import org.jboss.as.web.WebLogger;
import org.jboss.as.web.security.JBossGenericPrincipal;
import org.jboss.as.web.security.JBossWebRealm;
import org.jboss.as.web.security.jaspi.WebJASPIAuthenticator;
import org.jboss.logging.Logger;
import org.jboss.security.ServerAuthenticationManager;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.message.GenericMessageInfo;
import org.josso.agent.*;
import org.josso.agent.http.HttpSSOAgent;
import org.josso.agent.http.WebAccessControlUtil;
import org.josso.jaspi.agent.JASPICallbackHandler;
import org.josso.jaspi.agent.JASPISSOAgentRequest;

import javax.security.auth.Subject;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: sgonzalez@atriocore.com
 * @date: 7/31/13
 */
public class JOSSOJASPIAuthenticator extends WebJASPIAuthenticator {

    public static final String KEY_SESSION_MAP = "org.josso.servlet.agent.sessionMap";

    private static Logger log = Logger.getLogger(JOSSOJASPIAuthenticator.class);

    /**
     * "Expires" header always set to Date(1), so generate once only
     */
    private static final String DATE_ONE =
            (new SimpleDateFormat(DateTool.HTTP_RESPONSE_DATE_HEADER,
                    Locale.US)).format(new Date(1));

    private static HttpSSOAgent _agent;

    public JOSSOJASPIAuthenticator() {
        try {
            if (_agent == null) {
                Lookup lookup = Lookup.getInstance();
                lookup.init("josso-agent-config.xml");
                _agent = (HttpSSOAgent) lookup.lookupSSOAgent();
                if (log.isDebugEnabled()) {
                    _agent.setDebug(1);
                }
                _agent.start();
            }
        } catch (Exception e) {
            log.error("Error starting SSO Agent : " + e.getMessage(), e);
            throw new RuntimeException("Error starting SSO Agent : " + e.getMessage(), e);
        }
    }



    /**
     * Enforce the security restrictions in the web application deployment
     * descriptor of our associated Context.
     *
     * @param request Request to be processed
     * @param response Response to be processed
     *
     * @exception IOException if an input/output error occurs
     * @exception javax.servlet.ServletException if thrown by a processing element
     */
    @Override
    public void invoke(Request request, Response response)
            throws IOException, ServletException {

        if (log.isDebugEnabled())
            log.debug("Security checking request " +
                    request.getMethod() + " " + request.getRequestURI());
        LoginConfig config = this.context.getLoginConfig();

        // Have we got a cached authenticated Principal to record?
        if (cache) {
            Principal principal = request.getUserPrincipal();
            if (principal == null) {
                Session session = request.getSessionInternal(false);
                if (session != null) {
                    if (!jossoCookieExists(request)) {
                        session.setPrincipal(null);
                    }
                    principal = session.getPrincipal();
                    if (principal != null) {
                        if (log.isDebugEnabled())
                            log.debug("We have cached auth type " +
                                    session.getAuthType() +
                                    " for principal " +
                                    session.getPrincipal());
                        request.setAuthType(session.getAuthType());
                        request.setUserPrincipal(principal);
                    }
                }
            }
        }

        // Special handling for form-based logins to deal with the case
        // where the login form and all josso reserver uris might be outside the secured area
        String contextPath = this.context.getPath();
        String requestURI = request.getDecodedRequestURI();
        if (requestURI.startsWith(contextPath) &&
                requestURI.endsWith(Constants.FORM_ACTION) ||
                isJossoReservedUri(contextPath, requestURI)) {
            if (!authenticate(request, response, config)) {
                if (log.isDebugEnabled())
                    log.debug(" Failed authenticate() test ??" + requestURI );
                return;
            }
        }

        // Make sure that ALL resources are not cached by web proxies
        // or browsers as caching can provide a security hole
        if (disableProxyCaching &&
                // Note: Disabled for Mozilla FORM support over SSL
                // (improper caching issue)
                //!request.isSecure() &&
                !"POST".equalsIgnoreCase(request.getMethod())) {
            if (securePagesWithPragma) {
                // Note: These cause problems with downloading office docs
                // from IE under SSL and may not be needed for newer Mozilla
                // clients.
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
            } else {
                response.setHeader("Cache-Control", "private");
            }
            response.setHeader("Expires", DATE_ONE);
        }


        Realm realm = this.context.getRealm();
        // Is this request URI subject to a security constraint?
        SecurityConstraint [] constraints
                = realm.findSecurityConstraints(request, this.context);

        if ((constraints == null)) {
            /* Still process the authenticator, to provide identity information to public resources
            if (log.isDebugEnabled())
                log.debug(" Not subject to any constraint");
            getNext().invoke(request, response);
            return;
            */
            constraints = new SecurityConstraint[0];
        }

        int i;
        // Enforce any user data constraint for this security constraint
        if (log.isDebugEnabled()) {
            log.debug(" Calling hasUserDataPermission()");
        }
        if (!realm.hasUserDataPermission(request, response,
                constraints)) {
            if (log.isDebugEnabled()) {
                log.debug(" Failed hasUserDataPermission() test");
            }
            /*
             * ASSERT: Authenticator already set the appropriate
             * HTTP status code, so we do not have to do anything special
             */
            return;
        }

        // Since authenticate modifies the response on failure,
        // we have to check for allow-from-all first.
        boolean authRequired = true;
        for(i=0; i < constraints.length && authRequired; i++) {
            if(!constraints[i].getAuthConstraint()) {
                authRequired = false;
            } else if(!constraints[i].getAllRoles()) {
                String [] roles = constraints[i].findAuthRoles();
                if(roles == null || roles.length == 0) {
                    authRequired = false;
                }
            }
        }


        if(authRequired) {
            if (log.isDebugEnabled()) {
                log.debug(" Calling authenticate()");
            }
            if (!authenticate(request, response, config)) {
                if (log.isDebugEnabled()) {
                    log.debug(" Failed authenticate() test");
                }
                /*
                 * ASSERT: Authenticator already set the appropriate
                 * HTTP status code, so we do not have to do anything
                 * special
                 */
                return;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(" Calling accessControl()");
        }
        if (!realm.hasResourcePermission(request, response,
                constraints ,
                this.context)) {
            if (log.isDebugEnabled()) {
                log.debug(" Failed accessControl() test");
            }
            /*
             * ASSERT: AccessControl method has already set the
             * appropriate HTTP status code, so we do not have to do
             * anything special
             */
            return;
        }

        // Any and all specified constraints have been satisfied
        if (log.isDebugEnabled()) {
            log.debug(" Successfully passed all security constraints");
        }
        getNext().invoke(request, response);

    }

    @Override
    protected boolean authenticate(Request request, HttpServletResponse response, LoginConfig config) throws IOException {
        boolean result = false;

        String authMethod = config.getAuthMethod();

        // have we already authenticated someone?
        Principal principal = request.getUserPrincipal();
        String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
        if (principal != null) {


            WebLogger.WEB_SECURITY_LOGGER.tracef("Already authenticated '%s'", principal.getName());
            // associate the session with any existing SSO session
            if (ssoId != null)
                associate(ssoId, request.getSessionInternal(true));

            // If this is an agent reserved URL, process it and giv the JASPI Module a chance to intercept it (SLO)

            String contextPath = this.context.getPath();
            String requestURI = request.getDecodedRequestURI();

            if (!_agent.isAgentReservedUri(contextPath, requestURI)) {
                return (true);
            }

        }

        if ("BASIC".equalsIgnoreCase(authMethod) || "FORM".equalsIgnoreCase(authMethod)) {
            // is there an SSO session against which we can try to reauthenticate?
            if (ssoId != null) {
                WebLogger.WEB_SECURITY_LOGGER.tracef("SSO Id %s set; attempting reauthentication", ssoId);
                /* Try to reauthenticate using data cached by SSO.  If this fails, either the original SSO logon was of
                   DIGEST or SSL (which we can't reauthenticate ourselves because there is no cached username and password),
                   or the realm denied the user's reauthentication for some reason. In either case we have to prompt the
                   user for a logon */
                if (reauthenticateFromSSO(ssoId, request))
                    return true;
            }
        }

        Realm realm = this.context.getRealm();
        // Is this request URI subject to a security constraint?
        SecurityConstraint[] constraints = realm.findSecurityConstraints(
                request, this.context);

        if (!jossoCookieExists(request) && principal == null
                && constraints != null && constraints.length > 0) {
            boolean authRequired = true;
            for (int i = 0; i < constraints.length && authRequired; i++) {
                if (!constraints[i].getAuthConstraint()) {
                    authRequired = false;
                } else if (!constraints[i].getAllRoles()) {
                    String[] roles = constraints[i].findAuthRoles();
                    if (roles == null || roles.length == 0) {
                        authRequired = false;
                    }
                }
            }

            if (authRequired) {
                forwardToLoginPage(request, request.getResponse(), config);
                return false;
            }
        }

        // TODO: change message info to operate on HttpServletResponse, to align with Servlet 3.0
        GenericMessageInfo messageInfo = new GenericMessageInfo();
        messageInfo.setRequestMessage(request);
        messageInfo.setResponseMessage(request.getResponse());

        // put bits of information needed by tomcat server auth modules
        messageInfo.getMap().put("CACHE", String.valueOf(cache));
        messageInfo.getMap().put("javax.security.auth.message.MessagePolicy.isMandatory", "true");

        JASPICallbackHandler cbh = new JASPICallbackHandler();
        ServerAuthenticationManager sam = getServerAuthenticationManager();
        String appContext = request.getLocalName() + " " + request.getContextPath();
        Subject clientSubject = new Subject();
        if (sam != null) {
            result = sam.isValid(messageInfo, clientSubject, messageLayer, appContext, cbh);
        }

        // the authentication process has been a success. We need to register the principal, username, password and roles
        // with the container
        if (result) {


            PasswordValidationCallback pvc = cbh.getPasswordValidationCallback();
            CallerPrincipalCallback cpc = cbh.getCallerPrincipalCallback();
            GroupPrincipalCallback gpc = cbh.getGroupPrincipalCallback();

            if (cpc == null) {
                // No principal found, should we force a login ?!
                return constraints == null;
            }


            // get the client principal from the callback.
            Principal clientPrincipal = cpc.getPrincipal();
            if (clientPrincipal == null) {
                clientPrincipal = new SimplePrincipal(cpc.getName());
            }

            // if the client principal is not a jboss generic principal, we need to build one before registering.
            if (!(clientPrincipal instanceof JBossGenericPrincipal))
                clientPrincipal = this.buildJBossPrincipal(clientSubject, clientPrincipal, gpc);

            String passwordString = (pvc != null && pvc.getPassword() != null) ? new String(pvc.getPassword()) : null;
            String passwordUsername = (pvc != null && pvc.getUsername() != null) ? pvc.getUsername() : null;
            this.register(request, response, clientPrincipal, authMethod, passwordUsername, passwordString);

            if (this.secureResponse)
                sam.secureResponse(messageInfo, new Subject(), messageLayer, appContext, cbh);
        }

        return result;
    }

    protected Principal buildJBossPrincipal(Subject subject, Principal principal, GroupPrincipalCallback gpc) {

        List<String> roles = new ArrayList<String>();
        // look for roles in the subject first.
        for (Principal p : subject.getPrincipals()) {
            if (p instanceof Group && p.getName().equals("Roles")) {
                Enumeration<? extends Principal> members = ((Group) p).members();
                while (members.hasMoreElements())
                    roles.add(members.nextElement().getName());
            }
        }

        // check if the group callback contains any roles.
        if (gpc != null && gpc.getGroups() != null) {
            for (String group : gpc.getGroups())
                roles.add(group);
        }

        // look for the roles declared in the deployment descriptor.
        JBossWebRealm realm = (JBossWebRealm) this.getContainer().getRealm();
        Set<String> descriptorRoles = realm.getPrincipalVersusRolesMap().get(principal.getName());
        if (descriptorRoles != null)
            roles.addAll(descriptorRoles);

        // build and return the JBossGenericPrincipal.
        return new JBossGenericPrincipal(realm, principal.getName(), null, roles, principal, null, null, null, subject);

    }


    /**
     * Called to forward to the login page.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param config Login configuration describing
     * 		how authentication should be performed
     */
    protected void forwardToLoginPage(Request request, Response response,
                                      LoginConfig config) {
        RequestDispatcher disp = this.context.getServletContext().getRequestDispatcher(config.getLoginPage());
        try {
            Lookup lookup = Lookup.getInstance();
            lookup.init("josso-agent-config.xml");
            HttpSSOAgent agent = (HttpSSOAgent) lookup.lookupSSOAgent();
            agent.setAttribute(request.getRequest(), response.getResponse(),
                    WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI,
                    getRequestURI(request));
            disp.forward(request.getRequest(), response.getResponse());
            response.finishResponse();
        } catch (Throwable t) {
            log.warn("Unexpected error forwarding to login page", t);
        }
    }

    /**
     * Returns request URI.
     *
     * @param request request
     * @return request URI.
     */
    protected String getRequestURI(Request request) {
        StringBuffer requestURI = new StringBuffer(request.getRequestURI());
        if (request.getQueryString() != null) {
            requestURI.append('?');
            requestURI.append(request.getQueryString());
        }
        return requestURI.toString();
    }

    /**
     * Checks if josso cookie exists.
     *
     * @param request request
     * @return true if josso cookie exists, false otherwise
     */
    protected boolean jossoCookieExists(Request request) {
        boolean jossoCookieExists = false;
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (org.josso.gateway.Constants.JOSSO_SINGLE_SIGN_ON_COOKIE.equals(cookie.getName())) {
                    if (cookie.getValue() != null && !cookie.getValue().equals("-")) {
                        jossoCookieExists = true;
                    }
                    break;
                }
            }
        }
        return jossoCookieExists;
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
        return _agent.getAttribute(hreq, org.josso.agent.Constants.JOSSO_SPLASH_RESOURCE_PARAMETER);
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


    protected boolean isJossoReservedUri(String contextPath, String uri) {
        return _agent.isAgentReservedUri(contextPath, uri);
    }


}
