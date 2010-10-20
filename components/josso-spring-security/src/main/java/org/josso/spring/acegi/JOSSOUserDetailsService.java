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

package org.josso.spring.acegi;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.GatewayServiceLocator;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.SSOIdentityManagerService;

/**
 * Date: Sep 28, 2007
 * Time: 10:23:22 AM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Gianluca Brigandi</a>
 */
public class JOSSOUserDetailsService implements UserDetailsService {

    private static final Log logger = LogFactory.getLog(JOSSOUserDetailsService.class);

    private GatewayServiceLocator _gsl;

    private SSOIdentityManagerService _im;

    /**
     * This implementation will retrieve user details from JOSSO services.
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, org.springframework.dao.DataAccessException {
        try {
            // TODO : Test ACEGI with multiple security domains !!!
            // TODO : Get requester (partner app id!)
            SSOUser user = getIdentityManager().findUser(null, null, username);
            SSORole[] roles = _im.findRolesBySSOSessionId(null, username);
            return toUserDetails(user, roles);
        } catch (NoSuchUserException e) {
            logger.error(e.getMessage(), e);
            throw new UsernameNotFoundException(e.getMessage(), e);
        } catch (SSOIdentityException e) {
            logger.error(e.getMessage(), e);
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }


    /**
     * This addapts JOSSO user informatio to ACEGI user details.
     * <p/>
     * Some SSO properties retrieved by JOSSO could be mapped to specific user detail information
     * like account disabled, by subclasses.
     */
    protected UserDetails toUserDetails(SSOUser user, SSORole[] roles) {
        GrantedAuthority[] authorities = new GrantedAuthority[roles.length];
        for (int i = 0; i < roles.length; i++) {
            SSORole role = roles[i];
            authorities[i] = new GrantedAuthorityImpl(role.getName());
        }

        UserDetails ud = new User(user.getName(), "NOT AVAILABLE UNDER JOSSO", true, true,
                true, true, authorities);

        return ud;
    }


    public GatewayServiceLocator getGatewayServiceLocator() {
        return _gsl;
    }

    public void setGatewayServiceLocator(GatewayServiceLocator gsl) {
        this._gsl = gsl;
    }


    public SSOIdentityManagerService getIdentityManager() {
        if (_im == null) {
            try {
                _im = _gsl.getSSOIdentityManager();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return _im;
    }

}
