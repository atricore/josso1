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

package org.josso.gateway.assertion;

/**
 * Authentication Assertion whose instances can be updated.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: MutableAuthenticationAssertion.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class MutableAuthenticationAssertion extends AuthenticationAssertionImpl {



    public MutableAuthenticationAssertion(String id) {
        super(id);
    }

    public MutableAuthenticationAssertion(String id, String ssoSessionId) {
        super(id, ssoSessionId);
    }

    public void setSecurityDomainName(String securityDomainName) {
        this.securityDomainName = securityDomainName;
    }

    public void setSSOSessionId(String ssoSessionId) {
        this.ssoSessionId = ssoSessionId;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;

    }

}
