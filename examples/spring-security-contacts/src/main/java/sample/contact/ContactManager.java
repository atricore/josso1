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
package sample.contact;

import org.springframework.security.acls.Permission;
import org.springframework.security.acls.sid.Sid;

import java.util.List;


/**
 * Interface for the application's services layer.
 *
 * @author Ben Alex
 * @version $Id: ContactManager.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public interface ContactManager {
    //~ Methods ========================================================================================================

    public void addPermission(Contact contact, Sid recipient, Permission permission);

    public void create(Contact contact);

    public void delete(Contact contact);

    public void deletePermission(Contact contact, Sid recipient, Permission permission);

    public List getAll();

    public List getAllRecipients();

    public Contact getById(Long id);

    public Contact getRandomContact();
}
