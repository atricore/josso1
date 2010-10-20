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

package org.josso.jb5.agent;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.ServerAuthenticationManager;
import org.jboss.security.auth.message.GenericMessageInfo;
import org.jboss.web.tomcat.security.jaspi.TomcatJASPIAuthenticator;
import org.josso.agent.Lookup;
import org.josso.agent.http.HttpSSOAgent;
import org.josso.agent.http.WebAccessControlUtil;
import org.josso.gateway.Constants;
import org.josso.jaspi.agent.JASPICallbackHandler;

/**
 * JOSSO authenticator that does JSR-196 (JASPI) authentication.
 */
public class JOSSOJASPIAuthenticator extends TomcatJASPIAuthenticator {

	private static final Log log = LogFactory
			.getLog(JOSSOJASPIAuthenticator.class);

	private String messageLayer = "HttpServlet";
	
	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {

		if (log.isDebugEnabled())
			log.debug("Security checking request " + request.getMethod() + " "
					+ request.getRequestURI());
		LoginConfig config = this.context.getLoginConfig();

		// Have we got a cached authenticated Principal to record?
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
						log.debug("We have cached auth type "
								+ session.getAuthType() + " for principal "
								+ session.getPrincipal());
					request.setAuthType(session.getAuthType());
					request.setUserPrincipal(principal);
				}
			}
		}

		Realm realm = this.context.getRealm();
		// Is this request URI subject to a security constraint?
		SecurityConstraint[] constraints = realm.findSecurityConstraints(
				request, this.context);

		// Enforce any user data constraint for this security constraint
		if (log.isDebugEnabled()) {
			log.debug(" Calling hasUserDataPermission()");
		}
		if (!realm.hasUserDataPermission(request, response, constraints)) {
			if (log.isDebugEnabled()) {
				log.debug(" Failed hasUserDataPermission() test");
			}
			/*
			 * ASSERT: Authenticator already set the appropriate HTTP status
			 * code, so we do not have to do anything special
			 */
			return;
		}

		if (!authenticate(request, response, config)) {
			if (log.isDebugEnabled()) {
				log.debug(" Failed authenticate() test");
			}
			/*
			 * ASSERT: Authenticator already set the appropriate HTTP status
			 * code, so we do not have to do anything special
			 */
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug(" Calling accessControl()");
		}

		if (!realm.hasResourcePermission(request, response, constraints, this.context)) {
			if (log.isDebugEnabled()) {
				log.debug(" Failed accessControl() test");
			}
			/*
			 * ASSERT: AccessControl method has already set the appropriate HTTP
			 * status code, so we do not have to do anything special
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
	protected boolean authenticate(Request request, Response response,
			LoginConfig config) throws IOException {
		boolean result = false;

		String authMethod = config.getAuthMethod();

		// Have we already authenticated someone?
		Principal principal = request.getUserPrincipal();
		if (principal != null) {
			log.trace("Already authenticated '" + principal.getName() + "'");
			//return true;
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
				forwardToLoginPage(request, response, config);
				return false;
			}
		}
		
		GenericMessageInfo messageInfo = new GenericMessageInfo();
		messageInfo.setRequestMessage(request);
		messageInfo.setResponseMessage(response);

		// Put bits of information needed by tomcat server auth modules
		messageInfo.getMap().put("CACHE", cache);

		JASPICallbackHandler cbh = new JASPICallbackHandler();

		Subject subject = new Subject();
		ServerAuthenticationManager sam = getServerAuthenticationManager();
		if (sam != null) {
			result = sam.isValid(messageInfo, subject, messageLayer, cbh);
		}

		// The Authentication process has been a success. We need to register
		// the principal, username, password with the container
		if (result) {
			PasswordValidationCallback pvc = cbh.getPasswordValidationCallback();
			CallerPrincipalCallback cpcb = cbh.getCallerPrincipalCallback();
			if (pvc != null && cpcb != null) {
				this.register(request, response, cpcb.getPrincipal(),
						authMethod, pvc.getUsername(), new String(pvc
								.getPassword()));
				JBossSecurityAssociationActions.setPrincipalInfo(cpcb.getPrincipal(), 
						new String(pvc.getPassword()), subject);
			}
		}

		return result;
	}
	
	/**
	 * Register an authenticated Principal and authentication type in our
	 * request and session.
	 * 
	 * @param request
	 *            The servlet request we are processing
	 * @param response
	 *            The servlet response we are generating
	 * @param principal
	 *            The authenticated Principal to be registered
	 * @param authType
	 *            The authentication type to be registered
	 * @param username
	 *            Username used to authenticate (if any)
	 * @param password
	 *            Password used to authenticate (if any)
	 */
	protected void register(Request request, Response response,
			Principal principal, String authType, String username,
			String password) {

		if (log.isTraceEnabled()) {
			String name = (principal == null) ? "none" : principal.getName();
			log.trace("Authenticated '" + name + "' with type '" + authType
					+ "'");
		}

		// Cache the authentication information in our request
		request.setAuthType(authType);
		request.setUserPrincipal(principal);

		// Cache the authentication information in our session, if any
		Session session = request.getSessionInternal(false);
		if (session != null && cache) {
			session.setAuthType(authType);
			session.setPrincipal(principal);
		}
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
		RequestDispatcher disp = context.getServletContext()
				.getRequestDispatcher(config.getLoginPage());
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
                if (Constants.JOSSO_SINGLE_SIGN_ON_COOKIE.equals(cookie.getName())) {
                	if (cookie.getValue() != null && !cookie.getValue().equals("-")) {
                		jossoCookieExists = true;
                	}
                    break;
                }
            }
        }
        return jossoCookieExists;
	}
}
