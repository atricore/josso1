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
package org.josso.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.SecurityDomain;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.session.SSOSession;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.service.SSOSessionManager;

import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

/**
 * SSO Gateway Information bean
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOGatewayInfo.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class SSOSecurityDomainInfo {

    private static final Log logger = LogFactory.getLog(SSOSecurityDomainInfo.class);

    private SecurityDomain domain;

    private Properties props;

    public SSOSecurityDomainInfo(SecurityDomain domain) throws MBeanException, RuntimeOperationsException {
        super();
        this.domain = domain;
        props = new Properties();
        InputStream in = getClass().getResourceAsStream("/org/josso/josso.properties");
        try {
            props.load(in);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public String getVersion() {
        return props.getProperty("version");
    }

    public String getFullName() {
        return props.getProperty("fullname");
    }

    public String getName() {
        return props.getProperty("name");
    }

    public Long getSessionCount() {
        try {
            return new Long(domain.getSessionManager().getSessionCount());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return new Long(0);
    }

    public void invalidateAll() {
        try {
            domain.getSessionManager().invalidateAll();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void invalidateUserSessions(String username) {
        try {
            SSOSessionManager sm = domain.getSessionManager();
            Collection sessions = sm.getUserSessions(username);
            for (Iterator iterator = sessions.iterator(); iterator.hasNext();) {
                SSOSession session = (SSOSession) iterator.next();
                try {
                    sm.invalidate(session.getId());
                } catch (NoSuchSessionException e) {
                    logger.warn("Alrady invalidated : " + session.getId());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void invalidateSession(String sessionId) {
        try {
            domain.getSessionManager().invalidate(sessionId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean userExists(String username) {
        try {
            domain.getIdentityManager().userExists(username);
            return true;
        } catch (NoSuchUserException e) {
            return false;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public void checkValidSessions() {
        try {
            domain.getSessionManager().checkValidSessions();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public Collection listUserSessions(String username) {
        try {
            return domain.getSessionManager().getUserSessions(username);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return new ArrayList(0);

    }

    public Collection listSessions() {

        try {
            return domain.getSessionManager().getSessions();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return new ArrayList(0);
    }


}
