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

import org.springframework.security.acls.domain.BasePermission;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


/**
 * Validates {@link AddPermission}.
 *
 * @author Ben Alex
 * @version $Id: AddPermissionValidator.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class AddPermissionValidator implements Validator {
    //~ Methods ========================================================================================================

    public boolean supports(Class clazz) {
        return clazz.equals(AddPermission.class);
    }

    public void validate(Object obj, Errors errors) {
        AddPermission addPermission = (AddPermission) obj;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "permission", "err.permission", "Permission is required. *");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "recipient", "err.recipient", "Recipient is required. *");

        if (addPermission.getPermission() != null) {
            int permission = addPermission.getPermission().intValue();

            if ((permission != BasePermission.ADMINISTRATION.getMask())
                && (permission != BasePermission.READ.getMask()) && (permission != BasePermission.DELETE.getMask())) {
                errors.rejectValue("permission", "err.permission.invalid", "The indicated permission is invalid. *");
            }
        }

        if (addPermission.getRecipient() != null) {
            if (addPermission.getRecipient().length() > 100) {
                errors.rejectValue("recipient", "err.recipient.length",
                    "The recipient is too long (maximum 100 characters). *");
            }
        }
    }
}
