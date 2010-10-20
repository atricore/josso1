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

import org.springframework.security.acls.AclService;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.security.acls.sid.Sid;

import org.springframework.beans.factory.InitializingBean;

import org.springframework.util.Assert;

import org.springframework.web.bind.RequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Controller for deleting an ACL permission.
 *
 * @author Ben Alex
 * @version $Id: DeletePermissionController.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class DeletePermissionController implements Controller, InitializingBean {
    //~ Instance fields ================================================================================================

    private AclService aclService;
    private ContactManager contactManager;

    //~ Methods ========================================================================================================

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(contactManager, "A ContactManager implementation is required");
        Assert.notNull(aclService, "An aclService implementation is required");
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        // <c:param name="sid" value="${acl.sid.principal}"/><c:param name="permission" value="${acl.permission.mask}"/></c:url>">Del</A>
        int contactId = RequestUtils.getRequiredIntParameter(request, "contactId");
        String sid = RequestUtils.getRequiredStringParameter(request, "sid");
        int mask = RequestUtils.getRequiredIntParameter(request, "permission");

        Contact contact = contactManager.getById(new Long(contactId));

        Sid sidObject = new PrincipalSid(sid);
        Permission permission = BasePermission.buildFromMask(mask);

        contactManager.deletePermission(contact, sidObject, permission);

        Map model = new HashMap();
        model.put("contact", contact);
        model.put("sid", sidObject);
        model.put("permission", permission);

        return new ModelAndView("deletePermission", "model", model);
    }

    public void setAclService(AclService aclService) {
        this.aclService = aclService;
    }

    public void setContactManager(ContactManager contact) {
        this.contactManager = contact;
    }
}
