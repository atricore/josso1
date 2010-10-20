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

package org.josso.samples.ejb;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.security.Principal;

/**
 * EJB class of a Sample Partner Application EJB used for testing user
 * identity propagation from the web to the business tier.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: PartnerComponentEJB.java 974 2009-01-14 00:39:45Z sgonzalez $
 */

public class PartnerComponentEJB implements SessionBean {

//    private static final Log logger = LogFactory.getLog(PartnerComponentEJB.class);

    private SessionContext sessionContext;

    // ----------------------------------------------------- SessionBean Methods
    public void ejbCreate() throws CreateException {
        System.out.println("ejbCreate() called");
    }

    public void ejbActivate() {
        System.out.println("ejbActivate() called");
    }

    public void ejbPassivate() {
        System.out.println("ejbPassivate() called");
    }

    public void ejbRemove() {
        System.out.println("ejbRemove() called");
    }

    public void setSessionContext(SessionContext context) {
        sessionContext = context;
    }

    // ----------------------------------------------------- Component Methods

    /**
     * Sample Partner Application Component that dumps the security
     * context associated with the ejb method invocation.
     *
     * @param arg sample arg
     *
     * @throws java.rmi.RemoteException
     */
    public String echo(String arg) throws java.rmi.RemoteException {
        try {

            System.out.println("echo, arg=" + arg);
            Principal p = sessionContext.getCallerPrincipal();
            System.out.println("echo, callerPrincipal=" + p);
            boolean isCaller = sessionContext.isCallerInRole("role1");
            System.out.println("echo, isCallerInRole('role1')=" + isCaller);
            return arg;

        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();

            return e.getMessage();
        }
    }

    public String whoAmI() throws java.rmi.RemoteException {
        try  {

            Principal p = sessionContext.getCallerPrincipal();
            boolean isRole1 = sessionContext.isCallerInRole("role1");

            return "You're " + p.getName() + " and you do" + (isRole1 ? "" : " not") + " have role1";

        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();

            return e.getMessage();
        }
    }

    /**
     * Dummy ejb operation.
     *
     * @throws java.rmi.RemoteException
     */
    public void noop() throws java.rmi.RemoteException {
        System.out.println("noop");
    }
}