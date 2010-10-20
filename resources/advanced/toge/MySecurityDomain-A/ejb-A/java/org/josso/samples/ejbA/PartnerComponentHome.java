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

package org.josso.samples.ejbA;

import javax.ejb.*;
import java.rmi.RemoteException;

/**
 * Home interface of a Sample Partner Application EJB for testing user
 * identity propagation from the web to the business tier.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: PartnerComponentHome.java 693 2008-10-28 17:40:58Z sgonzalez $
 */

public interface PartnerComponentHome extends EJBHome {

    public PartnerComponent create() throws RemoteException, CreateException;

}