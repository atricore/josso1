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

package org.josso.selfservices.password.lostpassword;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: Constants.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public interface Constants {

    // Next Generation of JOSSO will use a BPM engine ;)

    public static final String STEP_REQUEST_CHALLENGES ="requestChallenges";

    public static final String STEP_REQUEST_ADDITIONAL_CHALLENGES ="requestAdditionalChallenges";

    public static final String STEP_REQUEST_ADDITIONAL_CONFIRMATION_CHALLENGES ="requestAdditionalConfirmationChallenges";

    public static final String STEP_CONFIRM_PASSWORD ="confirmPassword";

    public static final String STEP_PASSWORD_RESETED="passwordReseted";

    public static final String STEP_AUTH_ERROR="authError";

    public static final String STEP_FATAL_ERROR="fatalError";

    public static final String ATTR_CHALLENGES="challenges";

    public static final String CHALLENGE_PWD_ASSERTION_ID="pwdId";


    public static final String EXT_URL_PROVIDER = "urlProvider";
}
