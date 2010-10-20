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

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


/**
 * Controller for adding a new contact.
 *
 * @author Ben Alex
 * @version $Id: WebContactAddController.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class WebContactAddController extends SimpleFormController {
    //~ Instance fields ================================================================================================

    private ContactManager contactManager;

    //~ Methods ========================================================================================================

    protected Object formBackingObject(HttpServletRequest request)
        throws ServletException {
        WebContact wc = new WebContact();

        return wc;
    }

    public ContactManager getContactManager() {
        return contactManager;
    }

    public ModelAndView onSubmit(Object command) throws ServletException {
        String name = ((WebContact) command).getName();
        String email = ((WebContact) command).getEmail();

        Contact contact = new Contact(name, email);
        contactManager.create(contact);

        return new ModelAndView(new RedirectView(getSuccessView()));
    }

    public void setContactManager(ContactManager contactManager) {
        this.contactManager = contactManager;
    }
}
