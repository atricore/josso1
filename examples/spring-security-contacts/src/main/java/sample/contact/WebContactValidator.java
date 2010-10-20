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

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * Validates {@link WebContact}.
 *
 * @author Ben Alex
 * @version $Id: WebContactValidator.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class WebContactValidator implements Validator {
    //~ Methods ========================================================================================================

    public boolean supports(Class clazz) {
        return clazz.equals(WebContact.class);
    }

    public void validate(Object obj, Errors errors) {
        WebContact wc = (WebContact) obj;

        if ((wc.getName() == null) || (wc.getName().length() < 3) || (wc.getName().length() > 50)) {
            errors.rejectValue("name", "err.name", "Name 3-50 characters is required. *");
        }

        if ((wc.getEmail() == null) || (wc.getEmail().length() < 3) || (wc.getEmail().length() > 50)) {
            errors.rejectValue("email", "err.email", "Email 3-50 characters is required. *");
        }
    }
}
