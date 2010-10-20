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
 * Default Authentication Assertion implementation
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version $Id: AuthenticationAssertionImpl.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class AuthenticationAssertionImpl implements AuthenticationAssertion {
    private static final int ASSERTION_MAX_AGE = 30;

    protected String id;
    protected String securityDomainName;
    protected boolean isValid = true;
    protected long creationTime;
    protected String ssoSessionId;

    public AuthenticationAssertionImpl(String id) {
        this.id = id;
    }

    public AuthenticationAssertionImpl(String id, String ssoSessionId) {
        this.id = id;
        this.ssoSessionId = ssoSessionId;
        creationTime = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getSecurityDomainName() {
        return securityDomainName;
    }

    public String getSSOSessionId() {
        return ssoSessionId;
    }

    public boolean isValid() {

        if (!isValid) {
            return isValid;
        }

        long timeNow = System.currentTimeMillis();
        int age = (int) ((timeNow - creationTime) / 1000L);

        if (age > ASSERTION_MAX_AGE) {
            expire();
        }

        return isValid;

    }

    public long getCreationTime() {
        return creationTime;
    }

    /**
     * This method expires an assertion.
     */
    public void expire() {

        isValid = false;

        // TBD: Notify interested session event listeners
        //fireAssertionEvent(AuthenticationAssertion.AUTHENTICATION_ASSERTION_DESTROYED_EVENT, null);
    }

    public void fireAssertionEvent(String type, Object data) {
        throw new java.lang.UnsupportedOperationException("Event handling for assertions not yet implemented");
    }

    public int hashCode() {
        return getId().hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AuthenticationAssertion))
            return false;

        AuthenticationAssertion aa = (AuthenticationAssertion) obj;

        return aa.getId().equals(getId());
    }

}
