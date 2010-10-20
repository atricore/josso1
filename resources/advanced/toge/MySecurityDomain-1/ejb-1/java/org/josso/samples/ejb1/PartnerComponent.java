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

package org.josso.samples.ejb1;

/**
 * Component Interface of a Sample Partner Application EJB
 * used for testing user identity propagation from the web
 * to the business tier.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: PartnerComponent.java 693 2008-10-28 17:40:58Z sgonzalez $
 */

public interface PartnerComponent extends javax.ejb.EJBObject {

    /**
     * Sample operation which returns the supplied parameter value.
     *
     * @param arg sample argument
     * @return the supplied parameter value
     * @throws java.rmi.RemoteException
     */
    String echo(String arg) throws java.rmi.RemoteException;

    /**
     * Sample operation which returns current user information.
     *
     * @return some information about current user.
     * @throws java.rmi.RemoteException
     */
    String whoAmI() throws java.rmi.RemoteException;

    /**
     * Dummy method.
     *
     * @throws java.rmi.RemoteException
     */
    void noop() throws java.rmi.RemoteException;

}
