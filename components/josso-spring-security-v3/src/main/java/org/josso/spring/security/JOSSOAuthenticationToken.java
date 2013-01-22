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

package org.josso.spring.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JOSSOAuthenticationToken extends AbstractAuthenticationToken {

    //~ Instance fields ================================================================================================

    private static final long serialVersionUID = 1L;
    private Object principal;

    private String jossoSessionId;

    //~ Constructors ===================================================================================================

    /**
     * Constructor.
     *
     * @param jossoSessionId to identify if this object made by an authorised client
     * @param principal      the principal (typically a <code>UserDetails</code>)
     * @param authorities    the authorities granted to the principal
     * @throws IllegalArgumentException if a <code>null</code> was passed
     */
    public JOSSOAuthenticationToken(String jossoSessionId, Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);

        if (principal == null || "".equals(principal) || jossoSessionId == null || "".equals(jossoSessionId)) {
            throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
        }


        this.principal = principal;
        this.jossoSessionId = jossoSessionId;
        setAuthenticated(true);
    }

    //~ Methods ========================================================================================================

    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        if (obj instanceof JOSSOAuthenticationToken) {
            JOSSOAuthenticationToken test = (JOSSOAuthenticationToken) obj;

            if (this.getJossoSessionId().equals(test.getJossoSessionId())) {
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Always returns an empty <code>String</code>
     *
     * @return an empty String
     */
    public Object getCredentials() {
        return "";
    }

    public Object getPrincipal() {
        return this.principal;
    }

    public String getJossoSessionId() {
        return jossoSessionId;
    }
}
