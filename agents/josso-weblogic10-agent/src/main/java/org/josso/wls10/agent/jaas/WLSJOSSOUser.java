/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2008, Atricore, Inc.
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
 */

package org.josso.wls10.agent.jaas;

import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.service.BaseUser;
import weblogic.security.principal.WLSAbstractPrincipal;
import weblogic.security.spi.WLSUser;

/**
 * This principal extends Weblogic abstract principal, implementing also SSOUser interface.
 * WebLogic expects principals to implement WLUser and WLRole interfaces.
 *
 * Date: Nov 26, 2007
 * Time: 7:24:03 PM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class WLSJOSSOUser extends WLSAbstractPrincipal implements SSOUser, WLSUser {

    private SSOUser ssoUser;

    public WLSJOSSOUser(SSOUser ssoUser) {
        super();
        if (ssoUser == null)
            throw new NullPointerException("ssoUser cannot be null");
        this.ssoUser = ssoUser;
        super.setName(ssoUser.getName());
    }

    /**
     * @deprecated alwasy returns null
     */
    public String getSessionId() {
        return null;
    }

    public String getName() {
        return ssoUser.getName();
    }

    protected void setName(String newName) {

        // Keep name in sync
        if (ssoUser instanceof BaseUser)
            ((BaseUser)ssoUser).setName(newName);

        super.setName(newName);
    }

    public SSONameValuePair[] getProperties() {
        return this.ssoUser.getProperties();
    }


}
