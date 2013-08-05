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

import org.josso.gateway.identity.SSOUser;

import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class for JSR-196 server auth modules.
 */
public abstract class JOSSOServerAuthModule implements ServerAuthModule {

	protected CallbackHandler callbackHandler;
	
	@SuppressWarnings("unchecked")
	protected Map options;
	
	@SuppressWarnings("unchecked")
	public Class[] getSupportedMessageTypes() {
		return new Class[] { HttpServletRequest.class, HttpServletResponse.class };
	}

	@SuppressWarnings("unchecked")
	public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy,
			CallbackHandler handler, Map options) throws AuthException {
		this.callbackHandler = handler;
		this.options = options;
	}

	public void cleanSubject(MessageInfo messageInfo, Subject subject)
			throws AuthException {
		HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
		Principal principal = request.getUserPrincipal();
	    if (subject != null && principal != null) {
	    	subject.getPrincipals().remove(principal);
	    }
	}

	public abstract AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException;

	public abstract AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException;
	
	/**
	 * Register with the CallbackHandler.
	 * 
	 * @param userPrincipal user principal
	 * @param username username
	 * @param password password
	 * @param roles user roles
	 */
	protected void registerWithCallbackHandler(Principal userPrincipal, String username, String password, String[] groups) {
		if (this.callbackHandler instanceof JASPICallbackHandler) {

			JASPICallbackHandler cbh = (JASPICallbackHandler) callbackHandler;

			char[] pass = (password == null) ? "".toCharArray() : password.toCharArray();
			PasswordValidationCallback passwordValidationCallback = 
				new PasswordValidationCallback(null, username, pass);
			cbh.setPasswordValidationCallback(passwordValidationCallback);
			cbh.setCallerPrincipalCallback(new CallerPrincipalCallback(null, userPrincipal));
            cbh.setGroupPrincipalCallback(new GroupPrincipalCallback(null, groups));

      	} else {
      		throw new RuntimeException("Unsupported Callback handler "
      				+ this.callbackHandler.getClass().getCanonicalName());
      	}
	}


}
