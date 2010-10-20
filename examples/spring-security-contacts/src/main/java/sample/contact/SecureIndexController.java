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

import org.springframework.beans.factory.InitializingBean;

import org.springframework.util.Assert;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Controller for secure index page.
 *
 * @author Ben Alex
 * @version $Id: SecureIndexController.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class SecureIndexController implements Controller, InitializingBean {
    //~ Instance fields ================================================================================================

    private ContactManager contactManager;

    //~ Methods ========================================================================================================

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(contactManager, "A ContactManager implementation is required");
    }

    public ContactManager getContactManager() {
        return contactManager;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        List myContactsList = contactManager.getAll();
        Contact[] myContacts;

        if (myContactsList.size() == 0) {
            myContacts = null;
        } else {
            myContacts = (Contact[]) myContactsList.toArray(new Contact[] {});
        }

        Map model = new HashMap();
        model.put("contacts", myContacts);

        return new ModelAndView("index", "model", model);
    }

    public void setContactManager(ContactManager contact) {
        this.contactManager = contact;
    }
}
