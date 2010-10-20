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

import java.io.Serializable;


/**
 * Represents a contact.
 *
 * @author Ben Alex
 * @version $Id: Contact.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class Contact implements Serializable {
    //~ Instance fields ================================================================================================

    private Long id;
    private String email;
    private String name;

    //~ Constructors ===================================================================================================

    public Contact(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Contact() {}

    //~ Methods ========================================================================================================

    /**
     * DOCUMENT ME!
     *
     * @return Returns the email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString() + ": ");
        sb.append("Id: " + this.getId() + "; ");
        sb.append("Name: " + this.getName() + "; ");
        sb.append("Email: " + this.getEmail());

        return sb.toString();
    }
}
